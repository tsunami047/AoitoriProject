package io.aoitori043.aoitoriproject.database.orm.cache.semaphore;

public interface LockSemaphore {

    enum LockType{
        JAVA_UTIL,
        SET,
        REDIS
    }

    boolean acquireWriteLock(String resourceId);
    void releaseWriteLock(String resourceId);
}
