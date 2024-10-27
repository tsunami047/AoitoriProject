package io.aoitori043.syncdistribute.rmi.service;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-27  19:56
 * @Description: ?
 */
public interface MessageService extends Remote {
    <T extends Serializable> void sendMessage(String channel, T message) throws RemoteException;
    void registerChannel(String channel, ChannelListener listener) throws RemoteException;
    Set<String> getChannels() throws RemoteException;
    void unregisterChannel(String channel, ChannelListener listener) throws RemoteException;
}