package io.aoitori043.aoitoriproject.utils.lock;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.orm.impl.CacheImplUtil;
import io.aoitori043.aoitoriproject.database.orm.semaphore.LockSemaphore;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreLock {

//    private static final long DEFAULT_TIMEOUT = 2500; // 默认超时时间 2500 毫秒
    private static final ConcurrentHashMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    public static void lock(String resourceId,int timeout,CacheImplUtil.Lock lock){
        try {
            acquireWriteLock(resourceId,timeout);
            lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + resourceId + " 写锁错误："+e.getMessage());
        } finally {
            releaseWriteLock(resourceId);
        }
    }

    public static <T> T submit(String resourceId,int timeout,CacheImplUtil.SubmitLock<T> lock) {
        try {
            acquireWriteLock(resourceId,timeout);
            return lock.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取 " + resourceId + " 写锁错误："+e.getMessage());
        } finally {
            releaseWriteLock(resourceId);
        }
        return null;
    }

    public static boolean acquireWriteLock(String resourceId,int timeout) {
        try {
            Semaphore sem = semaphoreMap.computeIfAbsent(resourceId, k -> new Semaphore(1));
            return sem.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            AoitoriProject.plugin.getLogger().warning("SemaphoreLock: "+ resourceId+" 锁获取超时。");
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized void releaseWriteLock(String resourceId) {
        Semaphore sem = semaphoreMap.get(resourceId);
        if (sem != null && sem.availablePermits() == 0) {
            sem.release();
        }
    }
}
