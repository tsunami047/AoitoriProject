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

import static io.aoitori043.aoitoriproject.config.InvalidUtil.performNullCheck;
import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.createInstance;
import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.isStaticField;
import static io.aoitori043.aoitoriproject.config.loader.YamlMapping.printlnError;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-28  21:26
 * @Description: ?
 */
public abstract class MapperInjection extends AutoConfigPrinter {

    public static void runAnnotatedMethodByField(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Run.class)) {
                Run annotation = method.getAnnotation(Run.class);
                if (annotation.after().equals(fieldName)) {
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
    }

    public static void runAnnotatedMethods(Object obj) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Run.class)) {
                Run annotation = method.getAnnotation(Run.class);
                if (!annotation.after().equals("%%%default")) {
                    continue;
                }
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

    public static void injectYaml(JavaPlugin plugin, Object object) {
        for (Field field : object.getClass().getFields()) {
            String path = null;
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(InjectYaml.class)) {
                    InjectYaml annotation = field.getAnnotation(InjectYaml.class);
                    path = annotation.path();
                    if (annotation.multiple()) {
                        FileLoader.extractFolder(plugin, path);
                        AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
                        FileLoader.processFiles(plugin, path, file -> {
                            if (file.getName().endsWith(".yml")) {
                                String fileName = file.getName().replace(".yml", "");
                                YamlConfiguration yaml = NotInvalidSignConfigLoader.loadConfiguration(file);
                                map.put(fileName, yaml);
                            }
                        });
                        field.set(isStaticField(field) ? null : object, map);
                    } else {
                        YamlConfiguration yamlConfiguration = FileLoader.releaseAndLoadFile(plugin, path + ".yml");
                        field.set(isStaticField(field) ? null : object, yamlConfiguration);
                    }
                }
            } catch (Exception e) {
                System.out.println("问题可能出自：" + path);
                e.printStackTrace();
            }
        }
    }

    public static void injectFilePath(Object object, String path) {
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(FilePath.class)) {
                    field.set(object, path);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Type getDeepestGenericType(Field field) {
        Type type = field.getGenericType();
        return getDeepestGenericType(type);
    }

    private static Type getDeepestGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type lastTypeArg = typeArguments[typeArguments.length - 1];
                return getDeepestGenericType(lastTypeArg);
            }
        }
        return type;
    }

    private static Type[] getGenericsTypes(Field field) {
        Type type = field.getGenericType();
        Type[] typeArguments;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeArguments = parameterizedType.getActualTypeArguments();
        } else {
            throw new IllegalArgumentException("泛型参数缺少");
        }
        return typeArguments;
    }

    public abstract JavaPlugin getPlugin();

    public void injectMapper(Object parent) {
        Class<? extends MapperInjection> aClass = this.getClass();
        for (Field field : aClass.getFields()) {
            try {
                if (!injectMappers(parent,field)) {
                    injectMapper(parent,field);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private boolean injectMappers(Object parent,Field field) throws IllegalAccessException {
        InjectMappers fieldAnnotation = field.getAnnotation(InjectMappers.class);
        if (fieldAnnotation != null) {
            if (Map.class.isAssignableFrom(field.getType())) {
                if (fieldAnnotation.dependent()) {
                    AbstractMap<Object, AbstractMap<Object, Object>> map = (AbstractMap<Object, AbstractMap<Object, Object>>) ReflectASMUtil.createInstance(field.getType());
                    field.set(isStaticField(field) ? null : this, map);
                    Type deepestGenericType = getDeepestGenericType(field);
                    String dir = fieldAnnotation.dir();
                    FileLoader.extractFolder(getPlugin(), dir);
                    FileLoader.processFiles(getPlugin(), dir, file -> {
                        String yamlName = file.getName().replace(".yml", "");
                        YamlConfiguration yaml = NotInvalidSignConfigLoader.loadNotInvalidSignConfig(file);
                        AbstractMap<Object, Object> tempMap = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
                        for (String key : yaml.getKeys(false)) {
                            try {
                                Object instance = createInstance((Class<?>) deepestGenericType);
                                injectFilePath(instance, file.getPath());
                                ConfigMapping.loadFromConfig(parent,instance, key, yaml.getConfigurationSection(key));
                                runAnnotatedMethods(instance);
                                try {
                                    performNullCheck(instance);
                                } catch (Exception e) {
                                    printlnError(instance);
                                    e.printStackTrace();
                                }
                                tempMap.put(key, instance);
                            } catch (Exception e) {
                                System.out.println("-------------------------------------------");
                                System.out.println("以下问题出自：" + file.getAbsolutePath());
                                e.printStackTrace();
                                System.out.println("--------------------------------------------");
                            }
                        }
                        map.put(yamlName, tempMap);
                    });
                    return true;
                }
                AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
                field.set(isStaticField(field) ? null : this, map);
                String dir = fieldAnnotation.dir();
                Type[] typeArguments = getGenericsTypes(field);
                FileLoader.extractFolder(getPlugin(), dir);
                FileLoader.processFiles(getPlugin(), dir, file -> {
                    String yamlName = file.getName().replace(".yml", "");
                    YamlConfiguration yaml = NotInvalidSignConfigLoader.loadNotInvalidSignConfig(file);
                    if (fieldAnnotation.singe()) {
                        try {
                            Object instance = createInstance((Class<?>) typeArguments[1]);
                            injectFilePath(instance, file.getPath());
                            YamlMapping.loadFromConfig(parent,instance, yaml, yamlName);
                            runAnnotatedMethods(instance);
                            try {
                                performNullCheck(instance);
                            } catch (Exception e) {
                                printlnError(instance);
                                e.printStackTrace();
                            }
                            map.put(yamlName, instance);
                        } catch (Exception e) {
                            System.out.println("-------------------------------------------");
                            System.out.println("以下问题出自：" + file.getAbsolutePath());
                            e.printStackTrace();
                            System.out.println("--------------------------------------------");
                        }
                    } else {
                        for (String key : yaml.getKeys(false)) {
                            try {
                                Object instance = createInstance((Class<?>) typeArguments[1]);
                                injectFilePath(instance, file.getPath() + "_" + key);
                                ConfigurationSection section = yaml.getConfigurationSection(key);
                                ConfigMapping.loadFromConfig(parent,instance, key, section);
                                runAnnotatedMethods(instance);
                                try {
                                    performNullCheck(instance);
                                } catch (Exception e) {
                                    printlnError(instance);
                                    e.printStackTrace();
                                }
                                map.put(key, instance);
                            } catch (Exception e) {
                                System.out.println("-------------------------------------------");
                                System.out.println("以下问题出自：" + file.getAbsolutePath());
                                e.printStackTrace();
                                System.out.println("--------------------------------------------");
                            }
                        }
                    }
                });
            }
        }
        return false;
    }

    private void injectMapper(Object parent,Field field) {
        InjectMapper annotation = field.getAnnotation(InjectMapper.class);
        if (annotation == null) {
            return;
        }
        YamlConfiguration yaml = FileLoader.releaseAndLoadFile(getPlugin(), annotation.path() + ".yml");
        if (annotation.singe()) {
        try {
            Object instance = createInstance((Class<?>) field.getType());
            YamlMapping.loadFromConfig(parent,instance, yaml, annotation.path());
            field.set(isStaticField(field) ? null : this, instance);
            injectFilePath(isStaticField(field), annotation.path());
            runAnnotatedMethods(instance);
            try {
                performNullCheck(instance);
            } catch (Exception e) {
                printlnError(instance);
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("-------------------------------------------");
            System.out.println("以下问题出自：" + annotation.path() + ".yml");
            e.printStackTrace();
            System.out.println("--------------------------------------------");
        }
        }else{
            try {
            AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
            field.set(isStaticField(field) ? null : this, map);
            Type deepestGenericType = getDeepestGenericType(field);
            for (String key : yaml.getKeys(false)) {
                Object instance = createInstance((Class<?>)deepestGenericType);
                injectFilePath(instance, key);
                ConfigurationSection section = yaml.getConfigurationSection(key);
                ConfigMapping.loadFromConfig(parent,instance, key, section);
                runAnnotatedMethods(instance);
                try {
                    performNullCheck(instance);
                } catch (Exception e) {
                    printlnError(instance);
                    e.printStackTrace();
                }
                map.put(key, instance);
            }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
