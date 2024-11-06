package io.aoitori043.syncdistribute.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-05  23:05
 * @Description: ?
 */
public interface PlayerUUIDService extends Remote {

    String getUniqueID(String playerName) throws RemoteException;
    String getPlayerName(String uuid) throws RemoteException;
}
