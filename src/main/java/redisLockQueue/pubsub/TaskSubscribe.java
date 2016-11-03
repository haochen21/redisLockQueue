package redisLockQueue.pubsub;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.task.Task;
import redisLockQueue.task.TaskNode;
import redisLockQueue.task.TaskThread;
import redisLockQueue.util.TaskJson;

/**
 * 监听终端任务队列，当有任务删除时，获取队列里最早的一个任务，与自身任务做比对
 * 
 *
 */
public class TaskSubscribe extends JedisPubSub {

	private Task task;

	private TerminalJedisPool terminalJedisPool;

	private TaskNode taskNode;

	// 终端任务队列最大存活时间，超过这个时间没有更新，终端任务队列删除，以防止系统异常造成的影响
	private static final int queueSurviveTime = 60;

	private final static Logger logger = LoggerFactory.getLogger(TaskSubscribe.class);

	public TaskSubscribe(Task task, TerminalJedisPool terminalJedisPool, TaskNode taskNode) {
		super();
		this.task = task;
		this.terminalJedisPool = terminalJedisPool;
		this.taskNode = taskNode;
	}

	@Override
	public void onMessage(String channel, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(this.task + ",onMessage---channel:" + channel + ",message:" + message);
		}
		if (message.equals("done")) {
			JedisPool pool = terminalJedisPool.getJedisPool();
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				// 获取第所有工作任务
				List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, -1);
				if (tasks != null && tasks.size() > 0) {
					Task firstTask = TaskJson.JSON.getTask(tasks.get(0));
					if (logger.isDebugEnabled()) {
						logger.debug(this.task + ",first task is:" + firstTask);
					}
					if (firstTask.equals(task)) {
						// 上一个任务如果正常完成，当前任务以及随后任务的过期时间需要调整，可以防止异常情况下过期时间过长
						taskNode.updateTtl(task);
						excuteTask(jedis);
					} else {
						// 如果当前等候执行任务不在工作队列，可能其它机器已经完成任务
						boolean exist = false;
						for (String taskJson : tasks) {
							Task queueTask = TaskJson.JSON.getTask(taskJson);
							if (queueTask.equals(task)) {
								exist = true;
								break;
							}
						}
						if (!exist) {
							logger.info("dosen't exist in queue........." + this.task);
							unsubscribe(task.getExpriedChannelName(), task.getChannelName());
						}
					}
				} else {
					// 工作队列不存在，结束监听
					logger.info("queue is null......" + this.task);
					unsubscribe(task.getExpriedChannelName(), task.getChannelName());
				}
			} catch (Exception ex) {
				logger.info(this.task + ",finish subscribe error........", ex);
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		} else if (message.equals("expired")) {
			// 工作任务队列超时，结束监听
			logger.info("queue is expired......" + this.task);
			unsubscribe(task.getExpriedChannelName(), task.getChannelName());
		}
	}

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		if (logger.isDebugEnabled()) {
			logger.debug(this.task + ",Subscribe---channel:" + channel + ",subscribedChannels:" + subscribedChannels);
		}
		// 任务开始时，注册这个消息监听器
		if (channel.equals(task.getChannelName())) {
			JedisPool pool = terminalJedisPool.getJedisPool();
			Jedis jedis = null;
			try {
				jedis = pool.getResource();
				// 获取经一个工作任务
				List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, 0);
				if (tasks != null && tasks.size() > 0) {
					Task firstTask = TaskJson.JSON.getTask(tasks.get(0));
					if (firstTask.equals(task)) {
						excuteTask(jedis);
					}
				}
			} finally {
				if (jedis != null) {
					jedis.close();
				}
			}
		}
	}

	@Override
	public void unsubscribe(String... channels) {
		if (logger.isDebugEnabled()) {
			logger.debug(this.task + ",unsubscribe---channel:" + Arrays.toString(channels));
		}
		super.unsubscribe(channels);
	}

	private FutureTask<Boolean> createTaskThread() {
		TaskThread taskThread = new TaskThread(task);
		FutureTask<Boolean> future = new FutureTask<Boolean>(taskThread);
		return future;
	}

	private void excuteTask(Jedis jedis) {
		FutureTask<Boolean> future = null;
		try {
			logger.info(this.task + " is the first task,running...............");
			// 每次执行新任务，更新工作队列的最大存活时间
			jedis.expire(task.getTaskQueueName(), queueSurviveTime);
			// 工作处理线程
			future = createTaskThread();
			new Thread(future).start();
			future.get(Task.TTL, TimeUnit.SECONDS);
			finishTask(jedis);
		} catch (Exception ex) {
			logger.info("task handle error........", ex);
			if (future != null) {
				future.cancel(true);
				finishTask(jedis);
			}
		}

	}

	private void finishTask(Jedis jedis) {
		// 工作任务执行完成
		// 获取工作队列里的第一个任务
		List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, 0);
		if (tasks.size() > 0) {
			Task firstTask = TaskJson.JSON.getTask(tasks.get(0));
			if (firstTask.equals(task)) {
				// 从队列里删除第一个任务
				jedis.lpop(task.getTaskQueueName());
				logger.debug("finish," + task + ",delete first from queue");
			} else {
				logger.debug("task thread done,first task don't match, task:" + task);
			}
		}
		// 取消消息订阅，结束父线程
		unsubscribe(task.getExpriedChannelName(), task.getChannelName());
		
		logger.info("finish task thread:" + task);
	}
}
