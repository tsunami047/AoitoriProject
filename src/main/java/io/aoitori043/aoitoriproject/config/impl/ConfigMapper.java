package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.FileLoader;
import io.aoitori043.aoitoriproject.config.InjectDirectory;
import io.aoitori043.aoitoriproject.config.InjectFile;
import io.aoitori043.aoitoriproject.config.Run;
import io.aoitori043.aoitoriproject.config.loader.NotInvalidSignConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.config.impl.ConfigMapping.createInstance;
import static io.aoitori043.aoitoriproject.config.impl.ConfigMapping.isStaticField;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:26
 * @Description: ?
 */
public abstract class ConfigMapper extends AutoConfigPrint{

    protected static void runAnnotatedMethods(Object obj) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Run.class)) {
                method.setAccessible(true);
                try {
                    method.invoke(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract JavaPlugin getPlugin();


    void fillInData() {
        Class<? extends ConfigMapper> aClass = this.getClass();
        for (Field field : aClass.getFields()) {
            try {
                InjectDirectory fieldAnnotation = field.getAnnotation(InjectDirectory.class);
                if (fieldAnnotation != null) {
                    if (field.getType() == ConcurrentHashMap.class) {
                        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
                        field.set(isStaticField(field) ? null : this, map);
                        String dir = fieldAnnotation.dir();
                        FileLoader.extractFolder(getPlugin(), dir);
                        FileLoader.processFiles(getPlugin(), dir, file -> {
                            YamlConfiguration yaml = NotInvalidSignConfig.loadNotInvalidSignConfig(file);
                            try {
                                for (String key : yaml.getKeys(false)) {
                                    Object instance = createInstance(fieldAnnotation.mapper());
                                    ConfigurationSection configsection = yaml.getConfigurationSection(key);
                                    ConfigMapping.loadFromConfig(instance, key, configsection);
                                    map.put(key, instance);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                InjectFile annotation = field.getAnnotation(InjectFile.class);
                if (annotation != null) {
//                    java.io.InjectFile file = new java.io.InjectFile(getPlugin().getDataFolder(), annotation.path() + ".yml");
                    YamlConfiguration yaml = FileLoader.releaseAndLoadFile(getPlugin(),annotation.path() + ".yml");
                    try {
                        Object instance = createInstance(annotation.mapper());
                        YamlMapping.loadFromConfig(instance, yaml);
                        field.set(isStaticField(field) ? null : this, instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
