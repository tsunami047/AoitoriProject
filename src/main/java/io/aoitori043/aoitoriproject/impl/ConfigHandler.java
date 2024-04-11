package io.aoitori043.aoitoriproject.impl;


import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.config.impl.BasicConfigImpl;
import io.aoitori043.aoitoriproject.database.DatabaseConfig;
import org.bukkit.plugin.java.JavaPlugin;


public class ConfigHandler extends BasicConfigImpl {

    public static void load(){
        instance = new ConfigHandler();
        database = new DatabaseConfig();
    }

    public static ConfigHandler instance;
    public static DatabaseConfig database;

    @Override
    public JavaPlugin getPlugin() {
        return AoitoriProject.plugin;
    }


    @Override
    public void loadConfig(){
        System.out.println("配置加载完毕.");
    }


}
