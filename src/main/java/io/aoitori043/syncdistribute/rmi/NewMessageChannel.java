package io.aoitori043.syncdistribute.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-28  00:02
 * @Description: ?
 */
public class NewMessageChannel extends MessageChannelListener{
    protected NewMessageChannel() throws RemoteException {
        super();
    }

    @Override
    public <T extends Serializable> void onMessageReceived(T t) throws RemoteException {
        System.out.println("new"+t.toString());
    }
}
