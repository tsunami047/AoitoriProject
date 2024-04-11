package io.aoitori043.aoitoriproject.database.orm.cache.semaphore;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;

import java.util.Set;

public class FastSemaphore implements LockSemaphore{

    @Getter
    public static FastSemaphore instance = new FastSemaphore();

    private static final long DEFAULT_TIMEOUT = 2500; // 默认超时时间 2500 毫秒

    private static final Set<String> semaphore = new ConcurrentSet<>();

    public synchronized boolean acquireWriteLock(String resourceId){
        if(resourceId == null || resourceId.isEmpty()) {
            throw new IllegalArgumentException("resourceId cannot be null or empty");
        }
        long elapsedTime = System.currentTimeMillis()+DEFAULT_TIMEOUT;
        while (semaphore.contains(resourceId)) {
            if(System.currentTimeMillis()>elapsedTime){
                semaphore.remove(resourceId);
                throw new IllegalArgumentException("acquire lock timeout");
//                return false;
            }
        }
        semaphore.add(resourceId);
        return true;
    }

    public synchronized void releaseWriteLock(String resourceId) {
        if(resourceId == null || resourceId.isEmpty()){
            throw new IllegalArgumentException("resourceId cannot be null or empty");
        }
        if (!semaphore.remove(resourceId)) {
            throw new IllegalArgumentException("resourceId lock is not exist");
        }
    }
}
