package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.Debug;
import org.bukkit.plugin.java.JavaPlugin;

import static io.aoitori043.aoitoriproject.config.loader.NotInvalidSignConfig.fillInYamlConfiguration;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  22:00
 * @Description: ?
 */
public abstract class EmptyConfigImpl extends ConfigMapper{

    public abstract JavaPlugin getPlugin();

    public EmptyConfigImpl() {
        try {
            fillInYamlConfiguration(getPlugin(),this);
            fillInData();
            Debug annotation = getClass().getAnnotation(Debug.class);
            if (annotation != null) {
                this.printToConsole();
            }
            loadConfig();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadConfig() {

    }
}
