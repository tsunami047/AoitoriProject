package io.aoitori043.syncdistribute.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PlayerResourceLock extends Remote {
    void resourceLock(Integer serverId,String playerName, String resourceId) throws RemoteException;
    void releaseResourceLock(Integer serverId,String playerName, String resourceId) throws RemoteException;
}
