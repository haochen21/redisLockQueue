package redisLockQueue.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.util.TaskJson;

/**
 * 工作任务开始后，在redis里创建(TTL)主键
 * 
 * @author Administrator
 *
 */
public class TaskNode {

	@Autowired
	private TerminalJedisPool terminalJedisPool;

	private final static Logger logger = LoggerFactory.getLogger(TaskNode.class);

	public TaskNode() {

	}

	/**
	 * 
	 * @param mac
	 *            终端mac地址
	 * @param taskUid
	 *            任务唯一标识符
	 * @param ttl
	 *            任务最大执行时间
	 * @return true: 创建成功 false: 创建失败，任务已经存在
	 */
	public boolean createTaskNode(Task task) {
		JedisPool pool = terminalJedisPool.getJedisPool();
		try (Jedis jedis = pool.getResource()) {
			Long result = jedis.setnx(task.getKeyName(), TaskJson.JSON.getJson(task));
			if (result == 1) {
				// 从队列中获取所有任务，找到任务结点对应的任务，改变这个任务结点及以后所有任务结点的过期时间
				List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, -1);
				boolean setTtl = false;
				for (int i = 0; i < tasks.size(); i++) {
					Task queueTask = TaskJson.JSON.getTask(tasks.get(i));
					if (setTtl) {
						jedis.expire(queueTask.getKeyName(), Task.TTL * (i + 1));
						logger.info(queueTask.getInfo() + ",time slamp is:" + Task.TTL * (i + 1));
					} else if (queueTask.equals(task)) {
						setTtl = true;
						jedis.expire(queueTask.getKeyName(), Task.TTL * (i + 1));
						logger.info(queueTask.getInfo() + ",time slamp is:" + Task.TTL * (i + 1));
					}
				}
				logger.info("create task node success......" + task);
				return true;
			} else {
				logger.info("create task node fail......" + task);
				return false;
			}
		}
	}

	public boolean deleteTaskNode(Task task) {
		JedisPool pool = terminalJedisPool.getJedisPool();
		try (Jedis jedis = pool.getResource()) {
			Long result = jedis.del(task.getKeyName());
			if (result > 0) {
				// 设置下一个结点最长启动时间，防止另一台机器要执行的任务死掉，等候时间过长
				List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, 0);
				if (tasks.size() > 0) {
					Task nextTask = TaskJson.JSON.getTask(tasks.get(0));
					jedis.expire(nextTask.getKeyName(), 3);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("delete task node success......" + task);
				}
				return true;
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("delete task node fail......" + task);
				}
				return false;
			}
		}
	}

	public boolean updateTtl(Task task) {
		JedisPool pool = terminalJedisPool.getJedisPool();
		try (Jedis jedis = pool.getResource()) {
			List<String> tasks = jedis.lrange(task.getTaskQueueName(), 0, -1);
			if (tasks != null && tasks.size() > 0) {
				boolean setTtl = false;
				for (int i = 0; i < tasks.size(); i++) {
					Task queueTask = TaskJson.JSON.getTask(tasks.get(i));
					if (setTtl) {
						jedis.expire(queueTask.getKeyName(), Task.TTL * (i + 1));
						logger.info(queueTask.getInfo() + ",time slamp is:" + Task.TTL * (i + 1));
					} else if (queueTask.equals(task)) {
						setTtl = true;
						jedis.expire(queueTask.getKeyName(), Task.TTL * (i + 1));
						logger.info(queueTask.getInfo() + ",time slamp is:" + Task.TTL * (i + 1));
					}
				}
			}
			return true;
		}
	}
}
