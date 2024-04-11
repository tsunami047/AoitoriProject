package io.aoitori043.aoitoriproject.database;

import io.aoitori043.aoitoriproject.PluginProvider;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.redis.RedisCore;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:08
 * @Description: ?
 */
public class DatabaseCenter {

    public static void init(){
        HikariConnectionPool.init(PluginProvider.getJavaPlugin());
        RedisCore.mainRedis = RedisCore.init(PluginProvider.getJavaPlugin(),-1);
//        JimmerClientImpl.init();
        io.aoitori043.aoitoriproject.CanaryClientImpl.init();
    }
}
