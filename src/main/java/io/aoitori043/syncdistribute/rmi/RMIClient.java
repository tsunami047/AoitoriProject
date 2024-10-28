package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.syncdistribute.rmi.service.DistributedLock;
import io.aoitori043.syncdistribute.rmi.service.MessageService;
import io.aoitori043.syncdistribute.rmi.service.OnlineService;
import io.aoitori043.syncdistribute.rmi.service.PlayerDataService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {

    public static OnlineService onlineService;
    public static PlayerDataService playerDataService;
    public static MessageService messageService;
    public static DistributedLock distributedLock;

    public static boolean isOnline(String playerName) {
        try {
            return onlineService.isOnline(playerName);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized void start(){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1900);
            onlineService = (OnlineService) registry.lookup("online");
            playerDataService = (PlayerDataService) registry.lookup("player_data");
            messageService = (MessageService)registry.lookup("message");
            distributedLock = (DistributedLock)registry.lookup("lock");
            RMIClient.messageService.registerChannel("AoitoriMarket",new MessageChannelListener());
            RMIClient.messageService.registerChannel("Aoitori",new NewMessageChannel());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
