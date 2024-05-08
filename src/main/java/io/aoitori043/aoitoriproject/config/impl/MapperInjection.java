package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.*;
import io.aoitori043.aoitoriproject.config.loader.ConfigMapping;
import io.aoitori043.aoitoriproject.config.loader.NotInvalidSignConfigLoader;
import io.aoitori043.aoitoriproject.config.loader.YamlMapping;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Map;

import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.createInstance;
import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.isStaticField;
import static io.aoitori043.aoitoriproject.config.loader.YamlMapping.printlnError;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:26
 * @Description: ?
 */
public abstract class MapperInjection extends AutoConfigPrinter {

    public static void runAnnotatedMethods(Object obj) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Run.class)) {
                method.setAccessible(true);
                try {
                    method.invoke(obj);
                } catch (Exception e) {
                    printlnError(obj);
                    e.printStackTrace();
                }
            }
        }
    }

    public static void injectYaml(JavaPlugin plugin, Object object){
        for (Field field : object.getClass().getFields()) {
            String path = null;
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(InjectYaml.class)) {
                    InjectYaml annotation = field.getAnnotation(InjectYaml.class);
                     path = annotation.path();
                    YamlConfiguration yamlConfiguration = FileLoader.releaseAndLoadFile(plugin,path + ".yml");
                    field.set(isStaticField(field)?null:object,yamlConfiguration);
                }
            }catch (Exception e){
                System.out.println("问题可能出自："+path);
                e.printStackTrace();
            }
        }
    }

    public abstract JavaPlugin getPlugin();

    public void injectMapper() {
        Class<? extends MapperInjection> aClass = this.getClass();
        for (Field field : aClass.getFields()) {
            try {
                InjectMappers fieldAnnotation = field.getAnnotation(InjectMappers.class);
                if (fieldAnnotation != null) {
                    if (Map.class.isAssignableFrom(field.getType())){
                        AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
                        field.set(isStaticField(field) ? null : this, map);
                        String dir = fieldAnnotation.dir();
                        Type[] typeArguments = getGenericsTypes(field);
                        FileLoader.extractFolder(getPlugin(), dir);
                        FileLoader.processFiles(getPlugin(), dir, file -> {
                            String yamlName = file.getName().replace(".yml", "");
                            YamlConfiguration yaml = NotInvalidSignConfigLoader.loadNotInvalidSignConfig(file);
                            if(fieldAnnotation.singe()){
                                try {
                                    Object instance = createInstance((Class<?>)typeArguments[1]);
                                    YamlMapping.loadFromConfig(instance,yaml,yamlName);
                                    runAnnotatedMethods(instance);
                                    map.put(yamlName, instance);
                                } catch (Exception e) {
                                    System.out.println("-------------------------------------------");
                                    System.out.println("以下问题出自："+file.getAbsolutePath());
                                    e.printStackTrace();
                                    System.out.println("--------------------------------------------");
                                }
                            }else{
                                for (String key : yaml.getKeys(false)) {
                                    try {
                                        Object instance = createInstance((Class<?>) typeArguments[1]);
                                        ConfigurationSection section = yaml.getConfigurationSection(key);
                                        ConfigMapping.loadFromConfig(instance, key, section);
                                        runAnnotatedMethods(instance);
                                        map.put(key, instance);
                                    }catch (Exception e){
                                        System.out.println("-------------------------------------------");
                                        System.out.println("以下问题出自："+file.getAbsolutePath());
                                        e.printStackTrace();
                                        System.out.println("--------------------------------------------");
                                    }
                                }
                            }
                        });
                    }
                }
                InjectMapper annotation = field.getAnnotation(InjectMapper.class);
                if (annotation != null) {
                    YamlConfiguration yaml = FileLoader.releaseAndLoadFile(getPlugin(),annotation.path() + ".yml");
                    try {
                        Object instance = createInstance((Class<?>)field.getType());
                        YamlMapping.loadFromConfig(instance, yaml,annotation.path());
                        field.set(isStaticField(field) ? null : this, instance);
                    } catch (Exception e) {
                        System.out.println("-------------------------------------------");
                        System.out.println("以下问题出自："+annotation.path() + ".yml");
                        e.printStackTrace();
                        System.out.println("--------------------------------------------");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static Type[] getGenericsTypes(Field field) {
        Type type = field.getGenericType();
        Type[] typeArguments;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeArguments = parameterizedType.getActualTypeArguments();
        }else{
            throw new IllegalArgumentException("泛型参数缺少");
        }
        return typeArguments;
    }
}
