package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.config.impl.BasicDatabaseMapper;
import io.aoitori043.aoitoriproject.database.DatabaseInjection;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.syncdistribute.rmi.heartbeat.NodeServer;
import io.aoitori043.syncdistribute.rmi.service.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {

    public static DistributedLock distributedLock;
    public static PlayerResourceLock playerResourceLock;
    public static ClassLoadingService classLoadingService;
    public static PlayerUUIDService playerUUIDService;

    public static synchronized void start(){
        try {
            Registry registry = LocateRegistry.getRegistry(DatabaseProperties.bc$host, DatabaseProperties.bc$port);
            AoitoriProject.onlineService = (OnlineService) registry.lookup("online");
            AoitoriProject.playerDataService = (PlayerDataService) registry.lookup("player_data");
            AoitoriProject.messageService = (MessageService)registry.lookup("message");
            distributedLock = (DistributedLock)registry.lookup("lock");
            AoitoriProject.distributedLock = new io.aoitori043.aoitoriproject.utils.lock.DistributedLock(distributedLock);
            playerResourceLock = (PlayerResourceLock)registry.lookup("player_resource");
            classLoadingService = (ClassLoadingService)registry.lookup("classloader");
            playerUUIDService = (PlayerUUIDService)registry.lookup("uuid");
            AoitoriProject.playerResourceLock = new io.aoitori043.aoitoriproject.utils.lock.PlayerResourceLock(RMIClient.playerResourceLock);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
