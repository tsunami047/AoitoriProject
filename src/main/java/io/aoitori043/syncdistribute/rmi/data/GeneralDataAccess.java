package io.aoitori043.syncdistribute.rmi.data;

import io.aoitori043.aoitoriproject.utils.RLUCache;
import io.aoitori043.syncdistribute.rmi.RMIClient;

import java.util.HashMap;
import java.util.UUID;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-05  23:18
 * @Description: ?
 */
public class GeneralDataAccess {

    public static RLUCache<String, UUID> playerUUIDMap = new RLUCache<>(200);
    public static RLUCache<UUID,String> uniqueIDPlayerMap = new RLUCache<>(200);

    public static UUID getPlayerUUID(String playerName){
        try {
            UUID uuid = playerUUIDMap.get(playerName);
            if (uuid == null) {
                 uuid = UUID.fromString(RMIClient.playerUUIDService.getUniqueID(playerName));
                 playerUUIDMap.put(playerName,uuid);
                return uuid;
            }else {
                return uuid;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getPlayerName(UUID uuid){
        try {
            String playerName = uniqueIDPlayerMap.get(uuid);
            if (playerName == null) {
                playerName = RMIClient.playerUUIDService.getPlayerName(uuid.toString());
                uniqueIDPlayerMap.put(uuid,playerName);
                return playerName;
            }else {
                return playerName;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
