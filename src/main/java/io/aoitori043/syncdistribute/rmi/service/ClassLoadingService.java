package io.aoitori043.syncdistribute.rmi.service;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-04  00:57
 * @Description: ?
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClassLoadingService extends Remote {
    void loadClass(String className, byte[] classBytes) throws RemoteException;
}