package redisLockQueue.pubsub;

import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redisLockQueue.pool.TerminalJedisPool;

public class SendMessage {

	@Autowired
	private TerminalJedisPool terminalJedisPool;

	public SendMessage() {

	}

	public void sendMessage(String channel, String message) {
		JedisPool pool = terminalJedisPool.getJedisPool();
		try (Jedis jedis = pool.getResource()) {
			jedis.publish(channel, message);
		}
	}
}
