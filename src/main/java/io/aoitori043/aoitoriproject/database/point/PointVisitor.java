package io.aoitori043.aoitoriproject.database.point;

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
        return persistentDataAccess.getAsInt(dataName);
    }

    public boolean getAsBoolean(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsBoolean(dataName);
    }

    public long getAsLong(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsLong(dataName);
    }

    public <T> T getAsObject(String playerName,String dataName,Class<T> clazz){
        return (T)get(playerName,dataName);
    }

    public String get(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.get(dataName);
    }

    public double getAsDoubleForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsDouble(dataName);
    }

    public boolean getAsBooleanForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsBoolean(dataName);
    }

    public int getAsIntForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsInt(dataName);
    }

    public long getAsLongForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.getAsLong( dataName);
    }

    public Object getForce(String playerName,String dataName){
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        return persistentDataAccess.get( dataName);
    }

}
