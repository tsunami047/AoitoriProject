package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Optional;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-25  00:35
 * @Description: ?
 */
public abstract class BasicConfigImpl extends EmptyConfigImpl{


    @InjectYaml(path = "config")
    public YamlConfiguration basicConfig;
    public String pluginPrefix;

    public BasicConfigImpl() {
        double version = basicConfig.getDouble("version");
        Optional.of(version).filter(v -> v == 1.0).orElseThrow(() -> new IllegalArgumentException("Invalid version: " + version));
        pluginPrefix = basicConfig.getString("message.prefix").replaceAll("&", "§");
    }




}
