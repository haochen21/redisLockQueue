package redisLockQueue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import redisLockQueue.pool.TerminalJedisPool;
import redisLockQueue.pubsub.SendMessage;
import redisLockQueue.task.TaskExpiredMonitor;
import redisLockQueue.task.TaskNode;

@Configuration
@PropertySource({ "classpath:/redis.properties" })
public class ServiceConfig {

	@Autowired
	private Environment env;

	@Bean
	public TerminalJedisPool createTerminalJedisPool() {
		int maxTotal = Integer.valueOf(env.getRequiredProperty("redis.pool.maxTotal"));
		int maxIdle = Integer.valueOf(env.getRequiredProperty("redis.pool.maxIdle"));
		long maxWaitMillis = Long.valueOf(env.getRequiredProperty("redis.pool.maxWaitMillis"));
		boolean testOnBorrow = Boolean.valueOf(env.getRequiredProperty("redis.pool.testOnBorrow"));
		boolean onreturn = Boolean.valueOf(env.getRequiredProperty("redis.pool.testOnReturn"));
		String ip = env.getRequiredProperty("redis.ip");
		int port = Integer.valueOf(env.getRequiredProperty("redis.port"));
		TerminalJedisPool pool = new TerminalJedisPool(maxTotal, maxIdle, maxWaitMillis, testOnBorrow, onreturn, ip,
				port);
		return pool;
	}

	@Bean(initMethod = "monitor")
	public TaskExpiredMonitor createTaskExpiredMonitor() {
		TaskExpiredMonitor monitor = new TaskExpiredMonitor();
		return monitor;
	}

	@Bean
	public TaskNode taskNode() {
		TaskNode taskNode = new TaskNode();
		return taskNode;
	}

	@Bean
	public SendMessage sendMessage() {
		SendMessage sendMessage = new SendMessage();
		return sendMessage;
	}
}
