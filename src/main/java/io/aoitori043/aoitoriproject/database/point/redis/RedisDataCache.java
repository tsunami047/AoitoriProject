package io.aoitori043.aoitoriproject.database.point.redis;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.point.*;
import io.aoitori043.aoitoriproject.database.point.DataType;
import io.aoitori043.aoitoriproject.database.point.ExpirableDataAccess;
import io.aoitori043.aoitoriproject.database.point.InitDataAccess;
import io.aoitori043.aoitoriproject.database.point.ObjectDataAccess;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import io.aoitori043.aoitoriproject.impl.ConfigHandler;
import io.aoitori043.aoitoriproject.thread.AoitoriScheduler;
import kilim.Task;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-06-16  01:41
 * @Description: ?
 */
public class RedisDataCache implements Listener {

    public static final String VARIABLE_INDEX = "AoitoriProject:Variable:";

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        ConcurrentHashMap<String, String> allData = getAllData(e.getPlayer().getName());
        if (allData == null){
            throw new RuntimeException("无法获取玩家变量从Redis："+e.getPlayer().getName());
        }
        DataOperateDistribute.cache.put(e.getPlayer().getName(),allData);
    }

    private static ConcurrentHashMap<String, String> getAllData(String name) {
        try (Jedis jedisResource = RedisCore.getJedisResource()) {
            Map<String, String> redisMap = jedisResource.hgetAll(name);
            ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : redisMap.entrySet()) {
                DataAccess dataAccess = PointManager.map.get(entry.getKey());
                DataType varType = dataAccess.getVarType();
                switch (varType) {
                    case OBJECT_DATA:{
                        ObjectDataAccess access = (ObjectDataAccess) dataAccess;
//                        map.put(entry.getKey(),access.deserialize(entry.getValue()));
                        break;
                    }
                    default:{
                        map.put(entry.getKey(),entry.getValue());
                        break;
                    }
                }
            }
            return map;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e){
        AoitoriProject.kilimScheduler.singleExecute("data",()->{
            Task.sleep(ConfigHandler.delayRemovePlayerProfileTime);
            Player player = e.getPlayer();
            if (!player.isOnline()) {
                DataOperateDistribute.cache.remove(e.getPlayer().getName());
            }
        });
    }

    public static <T> void setExpireData(String playerName, ExpirableDataAccess expirableDataAccess, T value){
        String varName = expirableDataAccess.getVarName();
        RedisDataCache.setExecute(playerName, varName,value);
        RedisDataCache.setExecute(playerName,"date$"+varName,expirableDataAccess.getLoadedTimestamp());
    }

    public static String restoreExpireData(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        ExpirableDataAccess access = (ExpirableDataAccess) dataAccess;
        String varName = access.getVarName();
        RedisDataCache.setExecute(playerName, varName,access.getInitValue());
        RedisDataCache.setExecute(playerName,"date$"+varName,access.getLoadedTimestamp());
        return access.getInitValue();
    }

    public static String getFromRedis(String playerName,String dataName){
        try (Jedis jedisResource = RedisCore.getJedisResource()) {
            return jedisResource.hget(playerName, dataName);
        }
    }

    public static Object getForce(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        ConcurrentHashMap<String, String> map = DataOperateDistribute.cache.get(playerName);
        Object data;
        if (map == null){
            data = getFromRedis(playerName,dataName);
        }else{
            data = map.get(dataName);
        }
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).getInitValue();
                set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = getLong(playerName, "date$" + dataName,true);
                if (System.currentTimeMillis() > expireDate) {
                    return restoreExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public static Object get(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        Object data = DataOperateDistribute.cache.get(playerName).get(dataName);
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).getInitValue();
                set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = getLong(playerName, "date$" + dataName,false);
                if (System.currentTimeMillis() > expireDate) {
                    return restoreExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public static double getDouble(String playerName, String dataName, boolean force) {
        Object s = force ? getForce(playerName, dataName) : get(playerName, dataName);
        return s == null ? 0 : Double.parseDouble(s.toString());
    }

    public static long getLong(String playerName, String dataName, boolean force) {
        Object s = force ? getForce(playerName, dataName) : get(playerName, dataName);
        return s == null ? 0 : Long.parseLong(s.toString());
    }

    public static boolean getBoolean(String playerName, String dataName, boolean force) {
        Object s = force ? getForce(playerName, dataName) : get(playerName, dataName);
        if (s == null) {
            return false;
        }
        return Boolean.parseBoolean(s.toString());
    }

    public static int getInt(String playerName, String dataName, boolean force) {
        Object s = force ? getForce(playerName, dataName) : get(playerName, dataName);
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            return Double.valueOf(s.toString()).intValue();
        }
    }

    public static void set(String playerName,String dataName,String value){
        try {
            DataAccess dataAccess = PointManager.map.get(dataName);
            if(dataAccess == null){
                setExecute(playerName, dataName, value);
                return;
            }
            switch (dataAccess.getVarType()) {
                case EXPIRED_DATA:{
                    setExpireData(playerName, (ExpirableDataAccess) dataAccess,value);
                    break;
                }
                case OBJECT_DATA:{
                    setObjectExecute((ObjectDataAccess)dataAccess,playerName,dataName,value);
                    break;
                }
                default:{
                    setExecute(playerName, dataName, value);
                    break;
                }
            }
        }catch (NullPointerException e){
            AoitoriProject.plugin.getLogger().info ("离线玩家数据修改保护： "+playerName+" "+dataName+" "+value);
        }
    }

    private static void setObjectExecute(ObjectDataAccess dataAccess,String playerName, String dataName, String value) {
        try {
            DataOperateDistribute.cache.get(playerName).put(dataName, value);
            AoitoriScheduler.singleExecute("data", () -> {
                writeDataToRedis(playerName, dataName, dataAccess.serialize(value));
            });
        }catch (NullPointerException e){
            AoitoriProject.plugin.getLogger().info ("离线玩家数据修改保护： "+playerName+" "+dataName+" "+value);
        }
    }

    private static <T> void setExecute(String playerName, String dataName, T value) {
        try {
            DataOperateDistribute.cache.get(playerName).put(dataName, String.valueOf(value));
            AoitoriScheduler.singleExecute("data", () -> {
                writeDataToRedis(playerName, dataName, value);
            });
        }catch (NullPointerException e){
            AoitoriProject.plugin.getLogger().info ("离线玩家数据修改保护： "+playerName+" "+dataName+" "+value);
        }
    }

    public static boolean isExist(String playerName,String dataName){
        return DataOperateDistribute.cache.get(playerName).containsKey(dataName);
    }


    public static void del(String playerName,String dataName){
        DataOperateDistribute.cache.get(playerName).remove(dataName);
        AoitoriScheduler.singleExecute("data",()->{
            delRedisDataEntry(playerName,dataName);
        });
    }

    private static void delRedisDataEntry(String playerName, String dataName){
        String mapKey = VARIABLE_INDEX + playerName;
        try (Jedis jedis = RedisCore.getJedisResource()) {
            jedis.hdel(mapKey, dataName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T> void writeDataToRedis(String playerName, String dataName, T value){
        String mapKey = VARIABLE_INDEX + playerName;
        try (Jedis jedis = RedisCore.getJedisResource()) {
            if (value instanceof Integer) {
                jedis.hset(mapKey, dataName, Integer.toString((Integer) value));
            } else if (value instanceof Double) {
                jedis.hset(mapKey, dataName, Double.toString((Double) value));
            } else {
                jedis.hset(mapKey, dataName, String.valueOf(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    public static void delAll(String dataName){
//        try (Jedis jedis = RedisCore.getJedisResource()) {
//            String cursor = "0"; // 初始游标
//            ScanParams scanParams = new ScanParams().match("*"); // 匹配所有键
//            do {
//                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
//                for (String playerName : scanResult.getResult()) {
//                    ConcurrentHashMap<String, Object> stringStringConcurrentHashMap = cache.getData(playerName);
//                    if (stringStringConcurrentHashMap != null) {
//                        stringStringConcurrentHashMap.remove(dataName);
//                    }
//                    Map<String, String> hashData = jedis.hgetAll(playerName);
//                    if (hashData.containsKey(dataName)) {
//                        jedis.hdel(playerName, dataName);
//                    }
//                }
//                cursor = scanResult.getCursor();
//            } while (!cursor.equals("0"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
