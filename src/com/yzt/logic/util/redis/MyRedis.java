package com.yzt.logic.util.redis;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;


import com.yzt.logic.util.Cnst;

public class MyRedis {

	private static RedisClient client;
	
	public static final Log logger = LogFactory.getLog(MyRedis.class);

	public static void initRedis() {
		client = new RedisClient(null);
		initCurrentProjectRedis();
		logger.info("redis 初始化完成");
	}

	public synchronized static RedisClient getRedisClient() {
		return client;
	}

	private static void initCurrentProjectRedis() {
		Jedis jedis = client.getJedis();
		if (jedis == null) {
			try {
				throw new Exception("redis初始化失败");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			Set<String> allKeys = jedis.keys(Cnst.PROJECT_PREFIX);
			if (allKeys != null && allKeys.size() > 0) {
				for (String key : allKeys) {
					jedis.del(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			client.returnBrokenJedis(jedis);
		} finally {
			if (jedis != null) {
				client.returnJedis(jedis);
			}
		}
	}

}
