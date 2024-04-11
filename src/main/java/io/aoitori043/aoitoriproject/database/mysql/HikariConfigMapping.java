package io.aoitori043.aoitoriproject.database.mysql;

import io.aoitori043.aoitoriproject.config.ConfigProperties;
import io.aoitori043.aoitoriproject.config.NotNullProperty;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:15
 * @Description: ?
 */
@ConfigProperties
public class HikariConfigMapping {
    public boolean enable;
    // 数据库的JDBC URL，默认为null
    @NotNullProperty
    public String jdbcUrl;
    // 数据库的用户名，默认为null
    @NotNullProperty
    public String username;
    // 数据库的密码，默认为null
    @NotNullProperty
    public String password;
    // 获取连接的超时时间（毫秒），默认为5秒
    public long connectionTimeout = 5_000;
    // 连接空闲超时时间（毫秒），空闲的连接在多久后会被关闭
    public long idleTimeout = 600_00;
    // 连接最大生命周期（毫秒），默认为30分钟
    public long maxLifetime = 1_800_000;
    // 连接池中保持的最小空闲连接数，默认为10
    public int minimumIdle = 1;
    // 连接池中允许的最大连接数，默认为10
    public int maximumPoolSize = 10;
}
