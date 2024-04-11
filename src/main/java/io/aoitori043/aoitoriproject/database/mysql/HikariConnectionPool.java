package io.aoitori043.aoitoriproject.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;


/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  20:10
 * @Description: ?
 */
public class HikariConnectionPool {

    public static HikariDataSource dataSource;
    public static JavaPlugin javaPlugin;


    public static java.sql.Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new RuntimeException("mysql在没有连接成功的情况下使用。");
        }
        return dataSource.getConnection();
    }

    public static void init(JavaPlugin javaPlugin) {
        HikariConfigMapping hikariMySQLProperties = DatabaseProperties.hikariMySQLProperties;
        if(hikariMySQLProperties == null || !hikariMySQLProperties.enable){
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                javaPlugin.getLogger().warning("mysql加载失败！找不到jdbc驱动包："+ex.getMessage());
                e.printStackTrace();
                }
        }
        HikariConfig config = getHikariConfig(hikariMySQLProperties);
        dataSource = new HikariDataSource(config);
        javaPlugin.getLogger().info("hikarimysql 连接成功.");

    }

    @NotNull
    private static HikariConfig getHikariConfig(HikariConfigMapping hikariMySQLConfig) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(hikariMySQLConfig.jdbcUrl);
        config.setUsername(hikariMySQLConfig.username);
        config.setPassword(hikariMySQLConfig.password);
        config.setConnectionTimeout(hikariMySQLConfig.connectionTimeout);
        config.setIdleTimeout(hikariMySQLConfig.idleTimeout);
        config.setMaxLifetime(hikariMySQLConfig.maxLifetime);
        config.setMinimumIdle(hikariMySQLConfig.minimumIdle);
        config.setMaximumPoolSize(hikariMySQLConfig.maximumPoolSize);
        return config;
    }
}
