package io.aoitori043.aoitoriproject.database.orm.cache.semaphore;

import io.aoitori043.aoitoriproject.database.orm.cache.impl.CacheImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-08  20:08
 * @Description: ?
 */
public class LockUtil {
    public static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static LockSemaphore getLockSemaphore(LockSemaphore.LockType lockType){
        switch (lockType) {
            case JAVA_UTIL:return FastSemaphore.getInstance();
            case SET:return CacheSemaphore.getInstance();
            case REDIS:return RedisSemaphore.getInstance();
        }
        return null;
    }


    //阻塞直到拿到锁为止
    public static void asyncLock(@NotNull String aggregateRootKey, CacheImplUtil.Lock lock) {
        asyncLock(LockSemaphore.LockType.SET, aggregateRootKey, lock);
    }

    public static <T> T syncLockSubmit(@NotNull String aggregateRootKey, CacheImplUtil.SubmitLock<T> lock) {
        return syncLockSubmit(LockSemaphore.LockType.SET, aggregateRootKey, lock);
    }

    public static void syncLock(@NotNull String aggregateRootKey, CacheImplUtil.Lock lock) {
        syncLock(LockSemaphore.LockType.SET, aggregateRootKey, lock);
    }

    public static void syncLock(LockSemaphore.LockType lockType,@NotNull String aggregateRootKey, CacheImplUtil.Lock lock) {
        try {
            getLockSemaphore(lockType).acquireWriteLock(aggregateRootKey);
            lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + aggregateRootKey + " 写锁错误："+e.getMessage());
        } finally {
            getLockSemaphore(lockType).releaseWriteLock(aggregateRootKey);
        }
    }

    public static <T> T syncLockSubmit(LockSemaphore.LockType lockType,@NotNull String aggregateRootKey, CacheImplUtil.SubmitLock<T> lock) {
        try {
            getLockSemaphore(lockType).acquireWriteLock(aggregateRootKey);
            return lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + aggregateRootKey + " 写锁错误："+e.getMessage());
        } finally {
            getLockSemaphore(lockType).releaseWriteLock(aggregateRootKey);
        }
        return null;
    }

    public static void asyncLock(LockSemaphore.LockType lockType,@NotNull String aggregateRootKey, CacheImplUtil.Lock lock) {
        try {
            getLockSemaphore(lockType).acquireWriteLock(aggregateRootKey);
            executorService.execute(() -> {
                try {
                    lock.run();
                } finally {
                    getLockSemaphore(lockType).releaseWriteLock(aggregateRootKey);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + aggregateRootKey + " 写锁失败:" + e.getMessage());
        }
    }
}
