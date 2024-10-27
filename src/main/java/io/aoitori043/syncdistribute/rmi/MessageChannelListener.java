package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.syncdistribute.rmi.service.ChannelListener;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-27  21:51
 * @Description: ?
 */
public class MessageChannelListener extends UnicastRemoteObject implements ChannelListener, Serializable {

    private static final long serialVersionUID = 1L;

    public MessageChannelListener() throws RemoteException {
        super();
    }

    @Override
    public <T extends Serializable> void onMessageReceived(T t) throws RemoteException {
        System.out.println(t.toString());
    }
}
