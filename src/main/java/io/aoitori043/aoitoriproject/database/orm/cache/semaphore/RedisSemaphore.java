package io.aoitori043.aoitoriproject.database.orm.cache.semaphore;

import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.Collections;

public class RedisSemaphore implements LockSemaphore{

    @Getter
    public static RedisSemaphore instance = new RedisSemaphore();
    public RedisCore redisCore = RedisCore.mainRedis;

    private static final String RELEASE_SUCCESS = "1";
    protected long expireSecond = 3;


    public boolean acquireWriteLock(String lockKey) {
        try(Jedis connection = redisCore.getConnection()) {
            long deathTime = System.currentTimeMillis()+expireSecond*1000;
            while(connection.setnx(lockKey, DatabaseProperties.cache.zeromq$serverId)!=1){
                Thread.sleep(10);
                if(System.currentTimeMillis()>deathTime){
                    return false;
                }
            }
            connection.expire(lockKey, expireSecond);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void releaseWriteLock(String lockKey) {
        try(Jedis connection = redisCore.getConnection()) {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = connection.eval(script, Collections.singletonList(lockKey), Collections.singletonList(DatabaseProperties.cache.zeromq$serverId));
//            RELEASE_SUCCESS.equals(result);
        }
    }


}
