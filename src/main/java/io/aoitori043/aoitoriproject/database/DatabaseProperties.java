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

    public static String bc$serverId;
    public static String bc$host;
    public static int bc$port;
    public static int bc$heartBeatPort;

    @GetFoldMapping
    public static Cache cache;

    @GetFoldMapping
    public static HikariConfigMapping hikariMySQLProperties;

    @GetFoldMapping
    public static JedisPoolMapping redisPoolProperties;

}
