package redisLockQueue.pubsub;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.task.Task;
import redisLockQueue.task.TaskHandle;
import redisLockQueue.task.TaskNode;
import redisLockQueue.util.TaskJson;
import redisLockQueue.util.TaskThreadCache;

/**
 * 监听终端任务超时消息 从终端任务队列里删除超时任务 其它任务线程将监听终端任务队列值删除事件
 */
public class TaskExpiredSubscribe extends JedisPubSub {

	private TerminalJedisPool terminalJedisPool;

	private SendMessage sendMessage;

	private TaskNode taskNode;

	private final static Logger logger = LoggerFactory.getLogger(TaskExpiredSubscribe.class);

	public TaskExpiredSubscribe(TerminalJedisPool terminalJedisPool, SendMessage sendMessage, TaskNode taskNode) {
		super();
		this.terminalJedisPool = terminalJedisPool;
		this.sendMessage = sendMessage;
		this.taskNode = taskNode;
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug("onPMessage---pattern:" + pattern + ",channel:" + channel + ",message:" + message);
		}
		if (message.equals("expired")) {
			int beginIndex = channel.indexOf("task:");
			if (beginIndex > 0) {
				String tempStr = channel.substring(beginIndex + 5);
				String[] tempArray = tempStr.split(":");
				String mac = tempArray[0];
				String taskUid = tempArray[1];
				logger.info("expired task----mac: " + mac + ",taskUid:" + taskUid);
				JedisPool pool = terminalJedisPool.getJedisPool();
				try (Jedis jedis = pool.getResource()) {
					String queueName = mac + ":queue";
					// 获取第所有工作任务
					List<String> tasks = jedis.lrange(queueName, 0, -1);
					// 如果当前工作任务不在工作队列中，创建工作线程
					for (String taskJson : tasks) {
						Task queueTask = TaskJson.JSON.getTask(taskJson);
						if (queueTask.getUid().equals(taskUid) && !TaskThreadCache.CACHE.exist(queueTask.getUid())) {
							logger.info("restart task----- " + queueTask);
							TaskHandle handle = new TaskHandle(terminalJedisPool, taskNode, queueTask, sendMessage,
									false);
							handle.start();
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.info("PSubscribe---pattern:" + pattern + ",subscribedChannels:" + subscribedChannels);
	}

}
