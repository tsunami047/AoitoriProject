package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.GetFlatMapping;
import io.aoitori043.aoitoriproject.config.GetFoldMapping;
import io.aoitori043.aoitoriproject.config.GetMapping;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.aoitori043.aoitoriproject.config.impl.ConfigMapping.isStaticField;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-27  20:09
 * @Description: ?
 */
public class YamlFieldMapper {

    public static <T> T createInstance(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    public static void getValue(Object object, ConfigurationSection section, Field field, String propertyName,String parentName) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldSetObj = isStaticField(field) ? null : object;
        if (isStaticField(field) && field.getName().equals("config")) {
            field.set(null, object);
            return;
        }
        propertyName = propertyName.replace("$", ".");
        if (section.get(propertyName) != null) {
            if (field.getType() == String.class) {
                if(!section.getString(propertyName).equals("null")){
                    field.set(fieldSetObj, section.getString(propertyName));
                }
            } else if (field.getType() == int.class) {
                field.set(fieldSetObj, section.getInt(propertyName));
                return;
            } else if (field.getType() == double.class || field.getType() == float.class) {
                field.set(fieldSetObj, section.getDouble(propertyName));
                return;
            } else if (field.getType() == boolean.class) {
                field.set(fieldSetObj, section.getBoolean(propertyName));
                return;
            } else if (field.getType() == List.class) {
                field.set(fieldSetObj, new ArrayList<>(section.getStringList(propertyName)));
                return;
            }
        }else if(parentName!=null && field.getName().equals("index") && object != null){
            try{
                field.setAccessible(true);
                field.set(object, parentName);
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        handleAnnotation(object, section, field, propertyName);
    }


    public static void handleAnnotation(Object object, ConfigurationSection section, Field field, String propertyName) throws IllegalAccessException {
        handleReferToAnnotation(object, section, field, propertyName);
        handleFoldAnnotation(object, section, field, propertyName);
        handleFlatReferToAnnotation(object, section, field);
    }

    public static Object getObject(Object object, Field field) {
        try {
            return field.get(isStaticField(field) ? null : object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }


    public static void fillDefaultValue(Object instance, Object indexData, Object parentData) {
        for (Field declaredField : instance.getClass().getDeclaredFields()) {
            try {
                if (declaredField.getName().equals("index")) {
                    declaredField.setAccessible(true);
                    declaredField.set(instance, indexData);
                }else
                if (declaredField.getName().equals("parent")) {
                    declaredField.setAccessible(true);
                    declaredField.set(instance, parentData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object getElementIgnoreCase(ConfigurationSection section,String configKey){
        Map<String, Object> values = section.getValues(true);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            if(configKey.equalsIgnoreCase(key)){
                return entry.getValue();
            }
        }
        return null;
    }


    private static void handleFlatReferToAnnotation(Object object, ConfigurationSection section, Field field) {
        try {
        GetFlatMapping getFlatMapping = field.getAnnotation(GetFlatMapping.class);
        if (getFlatMapping == null || !Map.class.isAssignableFrom(field.getType())) {
            return;
        }
            Map<Object, Object> map = (Map) createInstance(field.getType());
            field.set(object, map);
            if (getFlatMapping.stringKeys().length != 0) {
                for (String key : getFlatMapping.stringKeys()) {
                    ConfigurationSection subSection = section.getConfigurationSection(key.replace("$", "."));
                    if (subSection == null) return;
                    Object instance = createInstance(getFlatMapping.mapper());
                    ConfigMapping.loadFromConfig(instance,null, subSection);
                    fillDefaultValue(instance, key, object);
                    ConfigMapper.runAnnotatedMethods(instance);
                    map.put(key, instance);
                }
            } else {
                Enum[] enumConstants = getFlatMapping.enumKeys().getEnumConstants();
                for (Enum enumConstant : enumConstants) {
                    String name = enumConstant.name();
                    ConfigurationSection subSection = (ConfigurationSection) getElementIgnoreCase(section,name.replace("_", ""));
                    if (subSection == null) return;
                    Object instance = createInstance(getFlatMapping.mapper());
                    ConfigMapping.loadFromConfig(instance,null, subSection);
                    fillDefaultValue(instance, enumConstant, object);
                    ConfigMapper.runAnnotatedMethods(instance);
                    map.put(enumConstant, instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean handleFoldAnnotation(Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
        GetFoldMapping getFoldMapping = field.getAnnotation(GetFoldMapping.class);
        if (getFoldMapping == null) {
            return false;
        }
            ConfigurationSection referToSection = section.getConfigurationSection(propertyName);
            Object instance = createInstance(getFoldMapping.mapper());
            if (referToSection == null) {
                return true;
            }
            ConfigMapping.loadFromConfig(instance, null,section.getConfigurationSection(propertyName));
            fillDefaultValue(instance, propertyName, object);
            ConfigMapper.runAnnotatedMethods(instance);
            field.set(object, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean handleReferToAnnotation(Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
        GetMapping getMapping = field.getAnnotation(GetMapping.class);
        if (getMapping == null || !Map.class.isAssignableFrom(field.getType())) {
            return false;
        }
            ConfigurationSection referToSection = section.getConfigurationSection(propertyName);
            if (referToSection == null) return true;
            Set<String> keys = referToSection.getKeys(false);
            Map<String, Object> map = (Map) createInstance(field.getType());
            for (String key : keys) {
                ConfigurationSection configurationSection = referToSection.getConfigurationSection(key);
                Object instance = createInstance(getMapping.mapper());
                ConfigMapping.loadFromConfig(instance,null, configurationSection);
                fillDefaultValue(instance, key, object);
                ConfigMapper.runAnnotatedMethods(instance);
                map.put(key, instance);
            }
            field.set(object, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
