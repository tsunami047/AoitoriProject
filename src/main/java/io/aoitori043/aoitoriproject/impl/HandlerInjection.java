package io.aoitori043.aoitoriproject.impl;


import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.config.impl.BasicMapper;
import io.aoitori043.aoitoriproject.database.DatabaseInjection;
import org.bukkit.plugin.java.JavaPlugin;


public class HandlerInjection extends BasicMapper {

    public static void load(){
        instance = new HandlerInjection();
        database = new DatabaseInjection();
    }

    public static HandlerInjection instance;
    public static DatabaseInjection database;

    @Override
    public JavaPlugin getPlugin() {
        return AoitoriProject.plugin;
    }


    @Override
    public void loadConfig(){
        System.out.println("配置加载完毕.");
    }


}
