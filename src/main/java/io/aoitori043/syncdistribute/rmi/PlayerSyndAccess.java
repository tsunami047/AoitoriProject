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
        return new PersistentDataAccess(playerName);
    }

    public static PersistentDataAccess getPersistentDataAccess(String playerName){
        return persistentMap.get(playerName);
//        return persistentMap.computeIfAbsent(playerName, k -> {
//            return new PersistentDataAccess(playerName);
//        });
    }


}
