package com.ecspider.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webmaple.worker.config.JedisSPI;
import redis.clients.jedis.JedisPool;

/**
 * @author lyifee
 * on 2021/2/20
 */
@Component
public class JedisSPIImpl implements JedisSPI {
    @Autowired
    private JedisPool jedisPool;

    @Override
    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
