package io.aoitori043.aoitoriproject.database.point;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import io.aoitori043.aoitoriproject.thread.AoitoriScheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-06-16  01:41
 * @Description: ?
 */
public class RedisDataCache implements Listener {

    public static final String VARIABLE_INDEX = "variable:";

    public static ConcurrentHashMap<String,ConcurrentHashMap<String,Object>> cache = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        try (Jedis jedisResource = RedisCore.getJedisResource()) {
            Map<String, String> redisMap = jedisResource.hgetAll(e.getPlayer().getName());
            ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
            for (Map.Entry<String, String> entry : redisMap.entrySet()) {
                DataAccess dataAccess = PointManager.map.get(entry.getKey());
                DataType varType = dataAccess.getVarType();
                switch (varType) {
                    case OBJECT_DATA:{
                        ObjectDataAccess access = (ObjectDataAccess) dataAccess;
                        map.put(entry.getKey(),access.deserialize(entry.getValue()));
                        break;
                    }
                    default:{
                        map.put(entry.getKey(),entry.getValue());
                        break;
                    }
                }
            }
            cache.put(e.getPlayer().getName(), map);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <T> void setExpireData(String playerName,ExpirableDataAccess expirableDataAccess,T value){
        String varName = expirableDataAccess.getVarName();
        RedisDataCache.setExecute(playerName, varName,value);
        RedisDataCache.setExecute(playerName,"date$"+varName,expirableDataAccess.getLoadedTimestamp());
    }

    public static String redefaultExpireData(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        ExpirableDataAccess access = (ExpirableDataAccess) dataAccess;
        String varName = access.getVarName();
        RedisDataCache.setExecute(playerName, varName,access.getInitValue());
        RedisDataCache.setExecute(playerName,"date$"+varName,access.getLoadedTimestamp());
        return access.getInitValue();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e){
        cache.remove(e.getPlayer().getName());
    }

    public static String getFromRedis(String playerName,String dataName){
        try (Jedis jedisResource = RedisCore.getJedisResource()) {
            return jedisResource.hget(playerName, dataName);
        }
    }

    public static Object getForce(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        ConcurrentHashMap<String, Object> map = cache.get(playerName);
        Object data;
        if (map == null){
            data = getFromRedis(playerName,dataName);
        }else{
            data = map.get(dataName);
        }
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).initValue;
                set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = getLong(playerName, "date$" + dataName,true);
                if (System.currentTimeMillis() > expireDate) {
                    return redefaultExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public static Object get(String playerName,String dataName){
        DataAccess dataAccess = PointManager.map.get(dataName);
        Object data = cache.get(playerName).get(dataName);
        if(dataAccess!=null){
            if (data == null && dataAccess instanceof InitDataAccess){
                String initValue = ((InitDataAccess) dataAccess).initValue;
                set(playerName,dataName, initValue);
                return initValue;
            }
            if(dataAccess instanceof ExpirableDataAccess){
                long expireDate = getLong(playerName, "date$" + dataName,false);
                if (System.currentTimeMillis() > expireDate) {
                    return redefaultExpireData(playerName, dataName);
                }
            }
        }
        return data;
    }

    public static double getDouble(String playerName,String dataName,boolean force){
        Object s;
        if(force){
            s = getForce(playerName,dataName).toString();
        }else{
            s = get(playerName, dataName).toString();
        }
        return s==null?0:Double.parseDouble(s.toString());
    }

    public static long getLong(String playerName,String dataName,boolean force){
        Object s;
        if(force){
            s = getForce(playerName,dataName);
        }else{
            s = get(playerName, dataName);
        }
        return s==null?0:Long.parseLong(s.toString());
    }

    public static boolean getBoolean(String playerName,String dataName,boolean force){
        Object s = force?getForce(playerName,dataName):get(playerName,dataName);
        if(s == null){
            return false;
        }
        return Boolean.parseBoolean(s.toString());
    }

    public static int getInt(String playerName,String dataName,boolean force){
        Object s = force?getForce(playerName,dataName):get(playerName,dataName);
        if(s == null){
            return 0;
        }
        int i;
        try {
            i = Integer.parseInt(s.toString());
        }catch (Exception e){
            return Double.valueOf(s.toString()).intValue();
        }
        return i;
    }

    public static <T> void set(String playerName,String dataName,T value){
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

    private static <T> void setObjectExecute(ObjectDataAccess<T> dataAccess,String playerName, String dataName, T value) {
        try {
            cache.get(playerName).put(dataName, value);
            AoitoriScheduler.singleExecute("data", () -> {
                redisSet(playerName, dataName, dataAccess.serialize(value));
            });
        }catch (NullPointerException e){
            AoitoriProject.plugin.getLogger().info ("离线玩家数据修改保护： "+playerName+" "+dataName+" "+value);
        }
    }

    private static <T> void setExecute(String playerName, String dataName, T value) {
        try {
            cache.get(playerName).put(dataName, String.valueOf(value));
            AoitoriScheduler.singleExecute("data", () -> {
                redisSet(playerName, dataName, value);
            });
        }catch (NullPointerException e){
            AoitoriProject.plugin.getLogger().info ("离线玩家数据修改保护： "+playerName+" "+dataName+" "+value);
        }
    }

    public static boolean isExist(String playerName,String dataName){
        return cache.get(playerName).containsKey(dataName);
    }

    public static void delAll(String dataName){
        Jedis jedis = RedisCore.getJedisResource();
        try {
            String cursor = "0"; // 初始游标
            ScanParams scanParams = new ScanParams().match("*"); // 匹配所有键
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                for (String playerName : scanResult.getResult()) {
                    ConcurrentHashMap<String, Object> stringStringConcurrentHashMap = cache.get(playerName);
                    if (stringStringConcurrentHashMap != null) {
                        stringStringConcurrentHashMap.remove(dataName);
                    }
                    Map<String, String> hashData = jedis.hgetAll(playerName);
                    if (hashData.containsKey(dataName)) {
                        jedis.hdel(playerName, dataName);
                    }
                }
                cursor = scanResult.getCursor();
            } while (!cursor.equals("0"));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }

    public static void del(String playerName,String dataName){
        cache.get(playerName).remove(dataName);
        AoitoriScheduler.singleExecute("data",()->{
            redisDel(playerName,dataName);
        });
    }

    private static void redisDel(String playerName,String dataName){
        String mapKey = VARIABLE_INDEX + playerName;
        Jedis jedis = RedisCore.getJedisResource();
        try {
            jedis.hdel(mapKey, dataName);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }

    private static <T> void redisSet(String playerName,String dataName,T value){
        String mapKey = VARIABLE_INDEX + playerName;
        Jedis jedis = RedisCore.getJedisResource();
        try {
            if (value instanceof Integer) {
                jedis.hset(mapKey, dataName, Integer.toString((Integer) value));
            } else if (value instanceof Double) {
                jedis.hset(mapKey, dataName, Double.toString((Double) value));
            } else {
                jedis.hset(mapKey, dataName, String.valueOf(value));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }
}
