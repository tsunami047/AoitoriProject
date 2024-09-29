package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-30  00:21
 * @Description: ?
 */
public class PlayerSyndAccess {

    public static ConcurrentHashMap<String, PersistentDataAccess> persistentMap = new ConcurrentHashMap<>();


}
