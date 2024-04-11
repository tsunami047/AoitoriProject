package io.aoitori043.aoitoriproject.database.redis;

import io.aoitori043.aoitoriproject.config.ConfigProperties;
import io.aoitori043.aoitoriproject.config.NotNullProperty;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:51
 * @Description: ?
 */
@ConfigProperties
public class JedisPoolMapping {

    // 是否启用 Jedis 连接池，默认为 true
    public boolean enable = true;
    public int databaseIndex = 0;

    // Redis 服务器的主机名，默认为 localhost
    @NotNullProperty
    public String host = "localhost";

    // Redis 服务器的端口号，默认为 6379
    public int port = 6379;
    public String user;

    // 连接 Redis 服务器的密码，默认为 null
    public String password;

    // 连接超时时间（毫秒），默认为 2000 毫秒
    public int timeout = 2000;

    // 连接池最大连接数，默认为 8
    public int maxTotal = 8;

    // 连接池最大空闲连接数，默认为 8
    public int maxIdle = 8;

    // 连接池最小空闲连接数，默认为 0
    public int minIdle = 0;

    // 从连接池中获取连接时是否进行连接有效性检查，默认为 false
    public boolean testOnBorrow = false;

    // 归还连接到连接池时是否进行连接有效性检查，默认为 false
    public boolean testOnReturn = false;

    // 在空闲连接回收器线程运行时，是否对连接进行有效性检查，默认为 false
    public boolean testWhileIdle = false;

    // 空闲连接回收器线程运行的周期时间（毫秒），默认为 -1，表示不启用空闲连接回收器
    public long timeBetweenEvictionRunsMillis = -1;

    // 连接在连接池中保持空闲的最小时间（毫秒），超过这个时间的空闲连接将被回收，默认为 1800000 毫秒（30分钟）
    public long minEvictableIdleTimeMillis = 1800000;
}

