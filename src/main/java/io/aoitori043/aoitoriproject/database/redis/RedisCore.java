package io.aoitori043.aoitoriproject.database.redis;

import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:47
 * @Description: ?
 */
public class RedisCore {

    public static RedisCore mainRedis;

    public JedisPool jedisPool;

    public static JedisPool getJedisPool(){
        return mainRedis.jedisPool;
    }


    public Jedis getConnection() {
        if (jedisPool == null) {
            throw new RuntimeException("redis在没有连接成功的情况下使用。");
        }
        return jedisPool.getResource();
    }

    public static RedisCore init(JavaPlugin javaPlugin){
        return init(javaPlugin,-1);
    }

    public static RedisCore init(JavaPlugin javaPlugin,int databaseIndex) {
        try {
            RedisCore redisCore = new RedisCore();
            JedisPoolMapping jedisPoolConfigMapping = DatabaseProperties.redisPoolProperties;
            if (jedisPoolConfigMapping == null || !jedisPoolConfigMapping.enable) {
                return null;
            }
            if (databaseIndex == -1) {
                databaseIndex = jedisPoolConfigMapping.databaseIndex;
            }
            GenericObjectPoolConfig<Jedis> jedisPoolConfig = getJedisPoolConfig(jedisPoolConfigMapping);
            if (jedisPoolConfigMapping.user != null && jedisPoolConfigMapping.password != null) {
                redisCore.jedisPool = new JedisPool(
                        jedisPoolConfig,
                        jedisPoolConfigMapping.host,
                        jedisPoolConfigMapping.port,
                        jedisPoolConfigMapping.timeout,
                        jedisPoolConfigMapping.user,
                        jedisPoolConfigMapping.password,
                        databaseIndex
                );
            } else if (jedisPoolConfigMapping.password != null) {
                redisCore.jedisPool = new JedisPool(
                        jedisPoolConfig,
                        jedisPoolConfigMapping.host,
                        jedisPoolConfigMapping.port,
                        jedisPoolConfigMapping.timeout,
                        jedisPoolConfigMapping.password,
                        databaseIndex
                );
            } else if (jedisPoolConfigMapping.user != null) {
                redisCore.jedisPool = new JedisPool(
                        jedisPoolConfig,
                        jedisPoolConfigMapping.host,
                        jedisPoolConfigMapping.port,
                        jedisPoolConfigMapping.timeout,
                        jedisPoolConfigMapping.user,
                        null,
                        databaseIndex
                );
            } else {
                redisCore.jedisPool = new JedisPool(
                        jedisPoolConfig,
                        jedisPoolConfigMapping.host,
                        jedisPoolConfigMapping.port,
                        jedisPoolConfigMapping.timeout,
                        null,
                        databaseIndex
                );
            }
            javaPlugin.getLogger().info(databaseIndex + " 库 redis 连接成功.");
            return redisCore;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static GenericObjectPoolConfig<Jedis> getJedisPoolConfig(JedisPoolMapping jedisPoolMapping) {
        GenericObjectPoolConfig<Jedis> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(jedisPoolMapping.maxTotal);
        config.setMaxIdle(jedisPoolMapping.maxIdle);
        config.setMinIdle(jedisPoolMapping.minIdle);
        config.setTestOnBorrow(jedisPoolMapping.testOnBorrow);
        config.setTestOnReturn(jedisPoolMapping.testOnReturn);
        config.setTestWhileIdle(jedisPoolMapping.testWhileIdle);
        config.setTimeBetweenEvictionRunsMillis(jedisPoolMapping.timeBetweenEvictionRunsMillis);
        config.setMinEvictableIdleTimeMillis(jedisPoolMapping.minEvictableIdleTimeMillis);
        return config;
    }
}
