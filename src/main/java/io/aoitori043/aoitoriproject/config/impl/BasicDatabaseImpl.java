package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.InjectFile;
import io.aoitori043.aoitoriproject.config.InjectYaml;
import io.aoitori043.aoitoriproject.database.DatabaseProperties;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:20
 * @Description: ?
 */
public abstract class BasicDatabaseImpl extends EmptyConfigImpl {

    @InjectYaml(path = "database")
    public YamlConfiguration databaseConfig;
    @InjectFile(path = "database", mapper = DatabaseProperties.class)
    public static DatabaseProperties databaseConfigMapping;

    public void loadConfig() {
        super.loadConfig();
    }

}
