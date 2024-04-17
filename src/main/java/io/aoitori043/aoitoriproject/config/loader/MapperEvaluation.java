package io.aoitori043.aoitoriproject.config.loader;

import io.aoitori043.aoitoriproject.config.GetFlatMapping;
import io.aoitori043.aoitoriproject.config.GetFoldMapping;
import io.aoitori043.aoitoriproject.config.GetMapping;
import io.aoitori043.aoitoriproject.config.impl.MapperInjection;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.*;
import java.util.*;

import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.isStaticField;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-27  20:09
 * @Description: ?
 */
public class MapperEvaluation {

    public static <T> T createInstance(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    public static void getValue(Object object, ConfigurationSection section, Field field, String propertyName, String parentName) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldSetObj = isStaticField(field) ? null : object;
        if (isStaticField(field) && field.getName().equals("config")) {
            field.set(null, object);
            return;
        }
        propertyName = propertyName.replace("$", ".");
        if (section.get(propertyName) != null) {
            if (field.getType() == String.class) {
                if (!section.getString(propertyName).equals("null")) {
                    field.set(fieldSetObj, section.getString(propertyName));
                }
            } else if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(fieldSetObj, section.getInt(propertyName));
                return;
            } else if (field.getType() == double.class || field.getType() == float.class || field.getType() == Double.class || field.getType() == Float.class) {
                field.set(fieldSetObj, section.getDouble(propertyName));
                return;
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(fieldSetObj, section.getBoolean(propertyName));
                return;
            } else if (field.getType() == List.class) {
                field.set(fieldSetObj, new ArrayList<>(section.getStringList(propertyName)));
                return;
            }
        } else if (parentName != null && field.getName().equals("index") && object != null) {
            try {
                field.setAccessible(true);
                field.set(object, parentName);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            if (field.getType() == List.class) {
                field.set(fieldSetObj, new ArrayList<>());
                return;
            }
        }
        if (mappingInject(fieldSetObj, section, field, propertyName)) {
            return;
        }
        if (Map.class.isAssignableFrom(field.getType())) {
            AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
            Type type = field.getGenericType();
            Type[] typeArguments;
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                typeArguments = parameterizedType.getActualTypeArguments();
            }else{
                throw new IllegalArgumentException("泛型参数缺少");
            }
            field.set(fieldSetObj, map);
            if(typeArguments[1] == List.class){
                ConfigurationSection listSection = section.getConfigurationSection(field.getName());
                if(listSection !=null){
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        List<String> stringList = listSection.getStringList(key);
                        map.put(key,stringList);
                    }
                }
            }else if(typeArguments[1] == String.class){
                ConfigurationSection listSection = section.getConfigurationSection(field.getName());
                if(listSection!=null){
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        map.put(key,listSection.getString(key));
                    }
                }
            }
        }


    }


    public static boolean mappingInject(Object object, ConfigurationSection section, Field field, String propertyName) throws IllegalAccessException {
        return injectMapping(object, section, field, propertyName) |
        injectFoldMapping (object, section, field, propertyName) |
        injectFlatMapping(object, section, field);
    }

    public static Object getObject(Object object, Field field) {
        try {
            return field.get(isStaticField(field) ? null : object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }


    public static void injectDefaultValue(Object instance, Object indexData, Object parentData) {
        for (Field declaredField : instance.getClass().getDeclaredFields()) {
            try {
                if (declaredField.getName().equals("index")) {
                    declaredField.setAccessible(true);
                    declaredField.set(instance, indexData);
                } else if (declaredField.getName().equals("parent")) {
                    declaredField.setAccessible(true);
                    declaredField.set(instance, parentData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getElementIgnoreCase(ConfigurationSection section, String configKey) {
        Map<String, Object> values = section.getValues(true);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            if (configKey.equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }


    private static boolean injectFlatMapping(Object object, ConfigurationSection section, Field field) {
        try {
            GetFlatMapping getFlatMapping = field.getAnnotation(GetFlatMapping.class);
            Class<?> clazz = field.getType();
            if (getFlatMapping == null || !Map.class.isAssignableFrom(clazz)) {
                return false;
            }

            Type type = field.getGenericType();
            Type[] typeArguments;
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                typeArguments = parameterizedType.getActualTypeArguments();
            }else{
                throw new IllegalArgumentException("泛型参数缺少");
            }
            //(Class<?>)
            Map<Object, Object> map = (Map) createInstance(clazz);
            field.set(object, map);
            if (getFlatMapping.stringKeys().length != 0) {
                for (String key : getFlatMapping.stringKeys()) {
                    ConfigurationSection subSection = section.getConfigurationSection(key.replace("$", "."));
                    if (subSection == null) return true;
                    Object instance = createInstance((Class<?>)typeArguments[1]);
                    ConfigMapping.loadFromConfig(instance, null, subSection);
                    injectDefaultValue(instance, key, object);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(key, instance);
                }
            } else {
                Enum[] enumConstants = ((Class<? extends Enum>)typeArguments[0]).getEnumConstants();
                for (Enum enumConstant : enumConstants) {
                    String name = enumConstant.name();
                    ConfigurationSection subSection = (ConfigurationSection) getElementIgnoreCase(section, name.replace("_", ""));
                    if (subSection == null) return true;
                    Object instance = createInstance((Class<?>)typeArguments[1]);
                    ConfigMapping.loadFromConfig(instance, null, subSection);
                    injectDefaultValue(instance, enumConstant, object);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(enumConstant, instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean injectFoldMapping(Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
            GetFoldMapping getFoldMapping = field.getAnnotation(GetFoldMapping.class);
            if (getFoldMapping == null) {
                return false;
            }
            ConfigurationSection foldSection = section.getConfigurationSection(propertyName);
            Object instance = createInstance((Class<?>)field.getType());
            field.set(object, instance);
            if (foldSection == null) {
                return true;
            }
            ConfigMapping.loadFromConfig(instance, null, section.getConfigurationSection(propertyName));
            injectDefaultValue(instance, propertyName, object);
            MapperInjection.runAnnotatedMethods(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean injectMapping(Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
            GetMapping getMapping = field.getAnnotation(GetMapping.class);
            if (getMapping == null || !Map.class.isAssignableFrom(field.getType())) {
                return false;
            }
            Type type = field.getGenericType();
            Type[] typeArguments;
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                typeArguments = parameterizedType.getActualTypeArguments();
            }else{
                throw new IllegalArgumentException("泛型参数缺少");
            }
            Map<String, Object> map = (Map) createInstance(field.getType());
            field.set(object, map);
            ConfigurationSection mapperSection = section.getConfigurationSection(propertyName);
            if (mapperSection == null) return true;
            Set<String> keys = mapperSection.getKeys(false);
            for (String key : keys) {
                ConfigurationSection configurationSection = mapperSection.getConfigurationSection(key);
                Object instance = createInstance((Class<?>)typeArguments[1]);
                ConfigMapping.loadFromConfig(instance, null, configurationSection);
                injectDefaultValue(instance, key, object);
                MapperInjection.runAnnotatedMethods(instance);
                map.put(key, instance);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
