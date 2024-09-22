package io.aoitori043.aoitoriproject.database.point;

import io.aoitori043.aoitoriproject.database.point.redis.RedisDataCache;

import java.util.HashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-22  15:32
 * @Description: ?
 */
public class PointManager extends PointVisitor{

    public static HashMap<String, DataAccess> map = new HashMap<>();

    public void bind(DataAccess d) {
        map.put(d.getVarName(),d);
    }

    public void unbind(ExpirableDataAccess d) {
        map.remove(d.varName);
    }

    public <T> void set(String playerName, String dataName, T value) {
        RedisDataCache.set(playerName, dataName,String.valueOf(value));
//        DataOperateDistribute.dataOperateDistribute.set(playerName,dataName,String.valueOf(value));
    }

}
