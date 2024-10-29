package io.aoitori043.aoitoriproject.utils.lock;

import io.aoitori043.aoitoriproject.AoitoriProject;

import java.rmi.RemoteException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DistributedLock {

    io.aoitori043.syncdistribute.rmi.service.DistributedLock distributedLock;

    public DistributedLock(io.aoitori043.syncdistribute.rmi.service.DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    public synchronized <T> T submit(Supplier<T> supplier, String resourceId, int timeout) {
        try {
            try {
                this.distributedLock.acquireLock(resourceId, timeout);
                return supplier.get();
            } finally {
                this.distributedLock.releaseLock(resourceId);
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void lock(Runnable runnable,String resourceId,int timeout){
        try {
            try {
                this.distributedLock.acquireLock(resourceId, timeout);
                runnable.run();
            } finally {
                this.distributedLock.releaseLock(resourceId);
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public synchronized boolean acquireLock(String resourceId,int timeout) {
        try {
            return this.distributedLock.acquireLock(resourceId, timeout);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void releaseLock(String resourceId) {
        try {
            this.distributedLock.releaseLock(resourceId);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
}
