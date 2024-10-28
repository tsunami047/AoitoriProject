package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.syncdistribute.rmi.service.DistributedLock;
import io.aoitori043.syncdistribute.rmi.service.MessageService;
import io.aoitori043.syncdistribute.rmi.service.OnlineService;
import io.aoitori043.syncdistribute.rmi.service.PlayerDataService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {



    public static synchronized void start(){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1900);
            AoitoriProject.onlineService = (OnlineService) registry.lookup("online");
            AoitoriProject.playerDataService = (PlayerDataService) registry.lookup("player_data");
            AoitoriProject.messageService = (MessageService)registry.lookup("message");
            AoitoriProject.distributedLock = (DistributedLock)registry.lookup("lock");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
