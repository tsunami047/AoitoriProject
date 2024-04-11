package io.aoitori043.aoitoriproject.database.orm.cache;

import io.aoitori043.aoitoriproject.PluginProvider;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.*;

public class RedisCacheImpl {

    public static RedisCore cacheRedisPool;

    public Jedis getJedisConnection(){
        if(cacheRedisPool == null){
            cacheRedisPool = RedisCore.init(PluginProvider.getJavaPlugin(),DatabaseProperties.cache.redis$databaseIndex);
        }
        return cacheRedisPool.getConnection();
    }

    protected long expireSecond;


    public RedisCacheImpl() {
    }

    public RedisCacheImpl(Duration duration) {
        expireSecond = duration.getSeconds();
    }


    public List<String> getList(String key){
        try (Jedis jedis = getJedisConnection()) {
            return jedis.lrange(key, 0, -1);
        }
    }

    public void flushDB(){
        try (Jedis jedis = getJedisConnection()) {
            jedis.flushDB();
        }
    }

    public void pushUnduplicateList(String key,String value){
        try (Jedis jedis = getJedisConnection()) {
            List<String> list = getList(key);
            if(list.contains(value)){
                return;
            }
            jedis.lpush(key,value);
        }
    }

    public void del(String key){
        try (Jedis jedis = getJedisConnection()) {
            jedis.del(key);
        }
    }

    public void delListElement(String key,String element){
        try (Jedis jedis = getJedisConnection()) {
            jedis.lrem(key, 1, element);
        }
    }

    public void setList(String key, List<String> list){
        try (Jedis jedis = getJedisConnection()) {
            jedis.rpush(key,list.toArray(new String[0]));
            jedis.expire(key, expireSecond);
        }
    }

    public void putMap(String key, Map<String,String> map){
        try (Jedis jedis = getJedisConnection()) {
            jedis.hmset(key,map);
            jedis.expire(key, expireSecond);
        }
    }

    public Map<String, String> getMap(String key){
        try (Jedis jedis = getJedisConnection()) {
            return jedis.hgetAll(key);
        }
    }


}