package redisLockQueue.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.pubsub.SendMessage;
import redisLockQueue.pubsub.TaskSubscribe;
import redisLockQueue.util.TaskJson;
import redisLockQueue.util.TaskThreadCache;

public class TaskHandle extends Thread {

	private TerminalJedisPool terminalJedisPool;

	private TaskNode taskNode;

	private Task task;

	private SendMessage sendMessage;

	// true:正常情况运行 false：获取异常 消息运行
	private Boolean status;

	private final static Logger logger = LoggerFactory.getLogger(TaskHandle.class);

	public TaskHandle() {

	}

	public TaskHandle(TerminalJedisPool terminalJedisPool, TaskNode taskNode, Task task, SendMessage sendMessage,
			Boolean status) {
		super();
		this.terminalJedisPool = terminalJedisPool;
		this.taskNode = taskNode;
		this.task = task;
		this.sendMessage = sendMessage;
		this.status = status;
	}

	/**
	 * 把任务加入redis的list 获取第一个任务 如果任务相等，执行任务 如果任务相等，执行监听程序
	 */
	@Override
	public void run() {
		JedisPool pool = terminalJedisPool.getJedisPool();
		try (Jedis jedis = pool.getResource()) {
			if (status) {
				// 把工作加入到工作队列中（最右边），获取当前队列元素数量
				Long queueSize = jedis.rpush(task.getTaskQueueName(), TaskJson.JSON.getJson(task));
				logger.info("rpush queue," + task + ",size is:" + queueSize);
			}

			// 创建正在运行的任务结点
			boolean createResult = taskNode.createTaskNode(task);
			// 如果创建失败，可能其它机器已经在执行当前工作任务，停止线程执行
			if (!createResult) {
				logger.info(this.task + ",create node fail,thread is over.....");
				return;
			}

			int allTaskNumber = TaskThreadCache.CACHE.add(task.getUid());
			if (logger.isDebugEnabled()) {
				logger.debug("all task size is:" + allTaskNumber);
			}
			TaskSubscribe taskSubScribe = new TaskSubscribe(task, terminalJedisPool, taskNode);
			// 阻塞当前线程调用
			jedis.subscribe(taskSubScribe, task.getExpriedChannelName(), task.getChannelName());

			// 当unsubscribe()方法被调用时，才执行以下代码
			// 删除任务结点
			taskNode.deleteTaskNode(task);

			allTaskNumber = TaskThreadCache.CACHE.remove(task.getUid());
			if (logger.isDebugEnabled()) {
				logger.debug("all task size is:" + allTaskNumber);
			}
			// 发出完成消息
			sendMessage.sendMessage(task.getChannelName(), "done");

			logger.info(this.task + " thread is over.....");
		}
	}
}
