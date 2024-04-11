package io.aoitori043.aoitoriproject.database;

import io.aoitori043.aoitoriproject.config.impl.BasicDatabaseImpl;
import org.bukkit.plugin.java.JavaPlugin;

import static io.aoitori043.aoitoriproject.PluginProvider.getJavaPlugin;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:33
 * @Description: ?
 */
public class DatabaseConfig extends BasicDatabaseImpl {
    @Override
    public JavaPlugin getPlugin() {
        return getJavaPlugin();
    }
    @Override
    public void loadConfig() {
        super.loadConfig();
    }
}
