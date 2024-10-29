package io.aoitori043.aoitoriproject.utils.lock;

import io.aoitori043.aoitoriproject.AoitoriProject;

import java.rmi.RemoteException;

public class PlayerResourceLock {

    public io.aoitori043.syncdistribute.rmi.service.PlayerResourceLock playerResourceLock;

    public PlayerResourceLock(io.aoitori043.syncdistribute.rmi.service.PlayerResourceLock playerResourceLock) {
        this.playerResourceLock = playerResourceLock;
    }

    public void resourceLock(String playerName, String resourceId){
        try {
            playerResourceLock.resourceLock(AoitoriProject.port, playerName, resourceId);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
    public void releaseResourceLock(String playerName, String resourceId){
        try {
            playerResourceLock.releaseResourceLock(AoitoriProject.port, playerName, resourceId);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
