package io.aoitori043.syncdistribute.rmi.service;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-27  19:59
 * @Description: ?
 */
public interface ChannelListener extends Remote {
    <T extends Serializable> void onMessageReceived(T message) throws RemoteException;
}