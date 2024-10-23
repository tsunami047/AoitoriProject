package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-30  00:21
 * @Description: ?
 */
//TODO 完善直接取功能
public class PlayerSyndAccess {

    public static ConcurrentHashMap<String, PersistentDataAccess> persistentMap = new ConcurrentHashMap<>();

    public static PersistentDataAccess createPersistentDataAccess(String playerName){
        PersistentDataAccess persistentDataAccess = new PersistentDataAccess(playerName);
        persistentMap.put(playerName,persistentDataAccess);
        return persistentDataAccess;
    }

    public static void remove(String playerName){
        persistentMap.remove(playerName);
    }

    public static PersistentDataAccess getPersistentDataAccess(String playerName){
        return persistentMap.get(playerName);
    }

    public static PersistentDataAccess getOfflinePersistentDataAccess(String playerName){
        return new PersistentDataAccess(playerName);
    }


}
