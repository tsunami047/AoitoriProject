package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.Debug;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  22:00
 * @Description: ?
 */
public abstract class EmptyMapper extends MapperInjection {

    public abstract JavaPlugin getPlugin();

    public EmptyMapper() {
        try {
            injectYaml(getPlugin(),this);
            injectMapper();
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
