package io.aoitori043.aoitoriproject.database.point.mysql;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-09-21  00:16
 * @Description: ?
 */
public interface DatabaseAccess {

    public String getEntry(String playerName, String dataName);
    void insertEntry(String playerName, String dataName, String dataValue);

    void deleteEntry(String playerName, String dataName);

    void updateEntry(String playerName, String dataName, String newDataValue);

    ConcurrentHashMap<String, String> getVariablesByPlayerName(String playerName);
}
