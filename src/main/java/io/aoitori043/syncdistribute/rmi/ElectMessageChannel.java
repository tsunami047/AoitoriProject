package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.aoitoriproject.AoitoriProject;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-28  00:02
 * @Description: ?
 */
public class ElectMessageChannel extends MessageChannelListener{
    public ElectMessageChannel() throws RemoteException {
        super();
    }

    @Override
    public <T extends Serializable> void onMessageReceived(T t) throws RemoteException {
        Integer port = (Integer) t;
        AoitoriProject.nodeLeaderService.setLeaderPort(port);
        if (AoitoriProject.port == port) {
            AoitoriProject.nodeLeaderService.execute();
        }
    }
}
