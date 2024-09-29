package io.aoitori043.aoitoriproject.database.point;

import io.aoitori043.aoitoriproject.database.point.redis.RedisDataCache;
import io.aoitori043.aoitoriproject.script.TemporaryDataManager;
import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-22  15:29
 * @Description: ?
 */
public class PointVisitor {

    public double getAsDouble(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsDouble(dataName);
    }

    public int getAsInt(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getInt(playerName, dataName,false);
    }

    public boolean getAsBoolean(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getBoolean(playerName, dataName,false);
    }

    public long getAsLong(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getLong(playerName, dataName,false);
    }

    public <T> T getAsObject(String playerName,String dataName,Class<T> clazz){
        return (T)get(playerName,dataName);
    }

    public Object get(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.get(playerName, dataName);
    }

    public double getAsDoubleForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getDouble(playerName, dataName,true);
    }

    public boolean getAsBooleanForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getBoolean(playerName, dataName,true);
    }

    public int getAsIntForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getInt(playerName, dataName,true);
    }

    public long getAsLongForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getLong(playerName, dataName,true);
    }

    public Object getForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return RedisDataCache.getForce(playerName, dataName);
    }

}
