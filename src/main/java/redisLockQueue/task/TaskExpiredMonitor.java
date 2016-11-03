package redisLockQueue.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.pubsub.SendMessage;
import redisLockQueue.pubsub.TaskExpiredSubscribe;

public class TaskExpiredMonitor {

	@Autowired
	private TerminalJedisPool terminalJedisPool;

	@Autowired
	private SendMessage sendMessage;

	@Autowired
	private TaskNode taskNode;

	private static final String channel = "__keyspace@0__:terminal:task:*";

	private final static Logger logger = LoggerFactory.getLogger(TaskExpiredMonitor.class);

	public TaskExpiredMonitor() {

	}

	public void monitor() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				JedisPool pool = terminalJedisPool.getJedisPool();
				try (Jedis jedis = pool.getResource()) {
					logger.info("start task expired monitor.......");
					jedis.psubscribe(new TaskExpiredSubscribe(terminalJedisPool, sendMessage, taskNode), channel);
				}
			}
		}).start();

	}
}
