package redisLockQueue.pool;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class TerminalJedisPool {

	private int maxTotal;

	private int maxIdle;

	private long maxWaitMillis;

	private boolean testOnBorrow;

	private boolean onreturn;

	private String ip;

	private int port;

	// jedis池
	private JedisPool pool;

	public TerminalJedisPool() {

	}

	public TerminalJedisPool(int maxTotal, int maxIdle, long maxWaitMillis, boolean testOnBorrow, boolean onreturn,
			String ip, int port) {
		super();
		this.maxTotal = maxTotal;
		this.maxIdle = maxIdle;
		this.maxWaitMillis = maxWaitMillis;
		this.testOnBorrow = testOnBorrow;
		this.onreturn = onreturn;
		this.ip = ip;
		this.port = port;

		this.init();
	}

	public void init() {
		// 创建jedis池配置实例
		JedisPoolConfig config = new JedisPoolConfig();

		// 设置池配置项值
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		config.setTestOnBorrow(testOnBorrow);
		config.setTestOnReturn(onreturn);

		// 根据配置实例化jedis池
		pool = new JedisPool(config, ip, port);
	}

	public JedisPool getJedisPool() {
		return this.pool;
	}
}
