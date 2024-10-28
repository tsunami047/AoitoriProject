package io.aoitori043.syncdistribute.rmi.service;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-29  00:22
 * @Description: ?
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DistributedLock extends Remote {
    boolean acquireLock(String resourceId,int timeout) throws RemoteException;
    void releaseLock(String lockName) throws RemoteException;
}
