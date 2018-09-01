package com.yzt.logic.util.redis;

import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.yzt.logic.util.ProjectInfoPropertyUtil;

public class RedisClient {
	private JedisPool pool;
	@SuppressWarnings("unused")
	private Map<String, String> params;

	public RedisClient(Map<String, String> params) {
		this.params = params;
		this.init();
	}

	private void init() {
		String redisHost = ProjectInfoPropertyUtil.getProperty("redis.host", "localhost");
		int redisPort = Integer.valueOf(ProjectInfoPropertyUtil.getProperty("redis.port", "8998"));
		int maxActive = 3000;
		int maxIdle = 200;
		int maxWait = 100000;
		boolean testOnBorrow = true;
		String password = ProjectInfoPropertyUtil.getProperty("redis.password", null);

		JedisPoolConfig conf = new JedisPoolConfig();
		conf.setMaxIdle(maxIdle);
		conf.setMaxActive(maxActive);
		conf.setMaxWait(maxWait);
		conf.setTestOnBorrow(testOnBorrow);
		this.pool = new JedisPool(conf, redisHost, redisPort, 1000000, password);
	}

	public Jedis getJedis() {
		return (Jedis) this.pool.getResource();
	}

	public void returnJedis(Jedis jedis) {
		if (jedis != null) {
			this.pool.returnResource(jedis);
			// jedis.close();
		}

	}

	public void returnBrokenJedis(Jedis jedis) {
		if (jedis != null) {
			this.pool.returnBrokenResource(jedis);
			// jedis.close();
		}

	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
