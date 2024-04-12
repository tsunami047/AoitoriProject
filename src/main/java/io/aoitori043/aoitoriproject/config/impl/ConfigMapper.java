package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.FileLoader;
import io.aoitori043.aoitoriproject.config.InjectMappers;
import io.aoitori043.aoitoriproject.config.InjectMapper;
import io.aoitori043.aoitoriproject.config.Run;
import io.aoitori043.aoitoriproject.config.loader.NotInvalidSignConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
                InjectMappers fieldAnnotation = field.getAnnotation(InjectMappers.class);
                if (fieldAnnotation != null) {
                    if (field.getType() == ConcurrentHashMap.class) {
                        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
                        field.set(isStaticField(field) ? null : this, map);
                        String dir = fieldAnnotation.dir();

                        Type type = field.getGenericType();
                        Type[] typeArguments;
                        if (type instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            typeArguments = parameterizedType.getActualTypeArguments();
                        }else{
                            throw new IllegalArgumentException("泛型参数缺少");
                        }

                        FileLoader.extractFolder(getPlugin(), dir);
                        FileLoader.processFiles(getPlugin(), dir, file -> {
                            YamlConfiguration yaml = NotInvalidSignConfig.loadNotInvalidSignConfig(file);
                            try {
                                for (String key : yaml.getKeys(false)) {
                                    Object instance = createInstance((Class<?>)typeArguments[1]);
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
                InjectMapper annotation = field.getAnnotation(InjectMapper.class);
                if (annotation != null) {
//                    java.io.InjectFile file = new java.io.InjectFile(getPlugin().getDataFolder(), annotation.path() + ".yml");
                    YamlConfiguration yaml = FileLoader.releaseAndLoadFile(getPlugin(),annotation.path() + ".yml");
                    try {
                        Type type = field.getGenericType();
                        Type[] typeArguments;
                        if (type instanceof ParameterizedType) {
                            ParameterizedType parameterizedType = (ParameterizedType) type;
                            typeArguments = parameterizedType.getActualTypeArguments();
                        }else{
                            throw new IllegalArgumentException("泛型参数缺少");
                        }
                        Object instance = createInstance((Class<?>)typeArguments[1]);
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
