package io.aoitori043.aoitoriproject.database;

import io.aoitori043.aoitoriproject.config.ConfigProperties;
import io.aoitori043.aoitoriproject.config.GetFoldMapping;
import io.aoitori043.aoitoriproject.database.mysql.HikariConfigMapping;
import io.aoitori043.aoitoriproject.database.redis.JedisPoolMapping;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:18
 * @Description: ?
 */

@ConfigProperties
public class DatabaseProperties {

    public static class Cache{
        public String zeromq$serverId;
        public int zeromq$serverPort;
        public int redis$databaseIndex;
        public String kafka$groupId;
        public String kafka$topic;
        public String kafka$host;
        public int kafka$port;
    }

    @GetFoldMapping(mapper = Cache.class)
    public static Cache cache;

    @GetFoldMapping(mapper = HikariConfigMapping.class)
    public static HikariConfigMapping hikariMySQLProperties;

    @GetFoldMapping(mapper = JedisPoolMapping.class)
    public static JedisPoolMapping redisPoolProperties;

}
