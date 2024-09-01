package io.aoitori043.aoitoriproject.utils.lock;

import io.aoitori043.aoitoriproject.database.orm.impl.CacheImplUtil;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-25  20:26
 * @Description: ?
 */
public class GeneralLock {

    private static final int retryInterval = 10;


    public static void lock(String resourceId, int timeout, CacheImplUtil.Lock lock){
        Jedis jedisResource = RedisCore.getJedisResource();
        try {
            if (!acquireWriteLock(jedisResource,resourceId,timeout)) {
                System.out.println("处理写锁内容 " + resourceId + " 超时。");
            }
            lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("处理写锁内容 " + resourceId + " 错误："+e.getMessage());
        } finally {
            if (!releaseWriteLock(jedisResource,resourceId)) {
                System.out.println("释放锁 " + resourceId + " 错误");
            }
            jedisResource.close();
        }
    }

    public static <T> T submit(String resourceId,int timeout,CacheImplUtil.SubmitLock<T> lock) {
        Jedis jedisResource = RedisCore.getJedisResource();
        try {
            if (!acquireWriteLock(jedisResource,resourceId,timeout)) {
                System.out.println("处理写锁内容 " + resourceId + " 超时。");
            }
            return lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + resourceId + " 写锁错误："+e.getMessage());
        } finally {
            if (!releaseWriteLock(jedisResource,resourceId)) {
                System.out.println("释放锁 " + resourceId + " 错误");
            }
            jedisResource.close();
        }
        return null;
    }

    public static final String DISTRIBUTED_LOCK_INDEX = "lock:";
    
    public static final String lockUuid = UUID.randomUUID().toString();

    public static boolean acquireWriteLock(Jedis jedis,String requestId,int expireTime) {
        long endTime = System.currentTimeMillis() + expireTime+50;
        int cloneInterval = retryInterval;
        SetParams params = new SetParams();
        params.nx().ex(expireTime);
        while(System.currentTimeMillis() < endTime) {
            if (jedis.set(DISTRIBUTED_LOCK_INDEX+requestId, lockUuid, params).equals("OK")) {
                return true;
            }
            try {
                Thread.sleep(cloneInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            cloneInterval+=5;
        }
        jedis.del(DISTRIBUTED_LOCK_INDEX + requestId);
        return false;
    }

    public static boolean releaseWriteLock(Jedis jedis,String requestId) {
        try {
            if (lockUuid.equals(jedis.get(DISTRIBUTED_LOCK_INDEX + requestId))) {
                jedis.del(DISTRIBUTED_LOCK_INDEX + requestId);
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}
