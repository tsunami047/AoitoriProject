package io.aoitori043.aoitoriproject.database.point;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.point.mysql.MySQLExecutor;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import kilim.Task;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-20  22:27
 * @Description: ?
 */
public class DataOperateDistribute {


    public static DataOperateDistribute dataOperateDistribute = new DataOperateDistribute();
    public MySQLExecutor absoluteAccessObject = new MySQLExecutor();

    public static ConcurrentHashMap<String, ConcurrentHashMap<String,String>> cache = new ConcurrentHashMap<>();

    public void loadPlayerData(String playerName){
        ConcurrentHashMap<String, String> data = absoluteAccessObject.getVariablesByPlayerName(playerName);
        cache.put(playerName,data);
    }

    public void unloadPlayerData(String playerName){
        cache.remove(playerName);
    }

    public static class DataListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event){
            String name = event.getPlayer().getName();
            dataOperateDistribute.loadPlayerData(name);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event){
            String name = event.getPlayer().getName();
            AoitoriProject.kilimScheduler.singleExecute("ap_data",()->{
                Task.sleep(ConfigHandler.delayRemovePlayerProfileTime);
                if (!AoitoriProject.isPlayerOnline(name)) {
                    dataOperateDistribute.unloadPlayerData(name);
                }
            });
        }
    }

    private String checkDataObject(ConcurrentHashMap<String, String> playerMap,String playerName,String dataName,String data){
        DataAccess dataAccess = PointManager.map.get(dataName);
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).getInitValue();
                this.set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = Long.parseLong(getData(playerName, "date$" + dataName));
                if (System.currentTimeMillis() > expireDate) {
                    return restoreExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public String get(String playerName, String dataName){
        String data = getData(playerName, dataName);
        DataAccess dataAccess = PointManager.map.get(dataName);
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).getInitValue();
                this.set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = Long.parseLong(getData(playerName, "date$" + dataName));
                if (System.currentTimeMillis() > expireDate) {
                    return restoreExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public String getData(String playerName, String dataName){
        ConcurrentHashMap<String, String> playerMap = cache.get(playerName);
        if (playerMap == null){
            return absoluteAccessObject.getEntry(playerName, dataName);
        }else {
            return playerMap.get(playerName);
        }
    }

    private String restoreExpireData(String playerName, String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        ExpirableDataAccess access = (ExpirableDataAccess) dataAccess;
        String varName = access.getVarName();
        this.set(playerName, varName,access.getInitValue());
        this.set(playerName,"date$"+varName,String.valueOf(access.getLoadedTimestamp()));
        return access.getInitValue();
    }

    public void set(String playerName, String dataName, String value) {
        Map<String, String> playerMap = cache.get(playerName);
        set(playerMap,playerName,dataName,value);
    }

    public void set(Map<String,String> playerCache,String playerName, String dataName, String value) {
        if (playerCache.containsKey(dataName)) {
            playerCache.put(dataName,value);
            absoluteAccessObject.updateEntry(playerName,dataName,value);
        }else{
            absoluteAccessObject.insertEntry(playerName,dataName,value);
        }
    }


}
