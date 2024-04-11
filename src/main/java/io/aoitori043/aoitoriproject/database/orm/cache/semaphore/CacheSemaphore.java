package io.aoitori043.aoitoriproject.database.orm.cache.semaphore;

import lombok.Getter;

import java.util.concurrent.*;

public class CacheSemaphore implements LockSemaphore{

    @Getter
    public static CacheSemaphore instance = new CacheSemaphore();

    private static final long DEFAULT_TIMEOUT = 2500; // 默认超时时间 2500 毫秒

    private static final ConcurrentHashMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    @Deprecated
    public boolean acquireWriteLockRemote(String resourceId){
        try {
            return semaphoreMap.computeIfAbsent(resourceId, k -> new Semaphore(1)).tryAcquire(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    public void releaseWriteLockRemote(String resourceId) {
        Semaphore sem = semaphoreMap.get(resourceId);
        if (sem != null && sem.availablePermits() == 0) {
            sem.release();
        }
    }

    public boolean acquireWriteLock(String resourceId){
        try {
            Semaphore sem = semaphoreMap.computeIfAbsent(resourceId, k -> new Semaphore(1));
            return sem.tryAcquire(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
//            JVMCacheSync.lockAggregateAllServer(resourceId);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void releaseWriteLock(String resourceId) {
            Semaphore sem = semaphoreMap.get(resourceId);
            if (sem != null && sem.availablePermits() == 0) {
                sem.release();
            }
//        JVMCacheSync.unlockAggregateAllServer(resourceId);
    }
}
