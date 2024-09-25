package io.aoitori043.aoitoriproject.database.point.redis;

import io.aoitori043.aoitoriproject.database.point.mysql.DatabaseAccess;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-21  00:17
 * @Description: ?
 */
public class RedisExecutor implements DatabaseAccess {

    public static final String VARIABLE_INDEX = "AoitoriProject:Variable:";

    @Override
    public ConcurrentHashMap<String, String> getVariablesByPlayerName(String name) {
        try (Jedis jedisResource = RedisCore.getJedisResource()) {
            Map<String, String> redisMap = jedisResource.hgetAll(name);
            return new ConcurrentHashMap<>(redisMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getEntry(String playerName, String dataName) {
        return "";
    }

    @Override
    public void insertEntry(String playerName, String dataName, String dataValue) {
        try (Jedis jedis = RedisCore.getJedisResource()) {
            jedis.hset(VARIABLE_INDEX + playerName, dataName, dataValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert redis data： "+playerName+" "+dataName +" "+dataValue);
        }
    }

    @Override
    public void deleteEntry(String playerName, String dataName) {
        try (Jedis jedis = RedisCore.getJedisResource()) {
            jedis.hdel(VARIABLE_INDEX + playerName, dataName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete redis data： "+playerName+" "+dataName +" ");
        }
    }

    @Override
    public void updateEntry(String playerName, String dataName, String newDataValue) {
        try (Jedis jedis = RedisCore.getJedisResource()) {
            jedis.hset(VARIABLE_INDEX + playerName, dataName, newDataValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update redis data： "+playerName+" "+dataName +" "+newDataValue);
        }
    }

}
