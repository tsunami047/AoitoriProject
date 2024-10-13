package io.aoitori043.aoitoriproject.impl;


import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.config.ConfigProperties;
import io.aoitori043.aoitoriproject.config.InjectMapper;
import io.aoitori043.aoitoriproject.config.NonConfigProperty;
import io.aoitori043.aoitoriproject.config.impl.BasicMapper;
import io.aoitori043.aoitoriproject.database.DatabaseInjection;
import io.aoitori043.aoitoriproject.impl.mapper.PointMapper;
import io.aoitori043.aoitoriproject.utils.ResourceUtil;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileReader;

@ConfigProperties
public class ConfigHandler extends BasicMapper {

    public static void load(){
        instance = new ConfigHandler();
        database = new DatabaseInjection();
    }

    public static ConfigHandler instance;
    public static DatabaseInjection database;
    @InjectMapper(path = "point")
    public static PointMapper pointMapper;

    @Override
    public JavaPlugin getPlugin() {
        return AoitoriProject.plugin;
    }


    @Override
    public void loadConfig(){
        loadJavaScript();
        AoitoriProject.plugin.getLogger().info("配置加载完毕.");
    }

    public static int delayRemovePlayerProfileTime;

    @NonConfigProperty
    public static ScriptEngine engine;

    public static void loadJavaScript() {
        ResourceUtil.saveResourceBetter(AoitoriProject.plugin,"jexl");
        File file = new File(AoitoriProject.plugin.getDataFolder(), "jexl");
        File[] files = file.listFiles();
        if(files == null){
            return;
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
        for (File listFile : files) {
            try{
                engine.eval(new FileReader(listFile));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
