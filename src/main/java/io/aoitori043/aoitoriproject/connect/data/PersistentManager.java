package io.aoitori043.aoitoriproject.connect.data;

import io.aoitori043.aoitoriproject.database.point.DataAccess;

import java.util.concurrent.ConcurrentHashMap;

public class PersistentManager {

    public static ConcurrentHashMap<String, DataAccess> dataAccessMap = new ConcurrentHashMap<>();


}
