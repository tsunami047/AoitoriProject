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
import static io.aoitori043.aoitoriproject.config.loader.YamlMapping.printlnError;
import static io.aoitori043.aoitoriproject.utils.ReflectionUtil.getPrivateAndSuperField;

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

    public static HashMap<Enum<?>,String> getEnumMappingName(Class clazz){
        HashMap<Enum<?>,String> map = new HashMap<>();
        Enum[] enumConstants = (Enum[]) clazz.getEnumConstants();
        for (Enum enumConstant : enumConstants) {
            try {
                String mappingName = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                if (mappingName!=null) {
                    map.put(enumConstant, mappingName);
                }else{
                    map.put(enumConstant, enumConstant.name());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return map;
    }

    public static void getValue(Object object, ConfigurationSection section, Field field, String propertyName, String parentName) throws IllegalAccessException {
        field.setAccessible(true);
        Object fieldSetObj = isStaticField(field) ? null : object;
        if (isStaticField(field) && field.getName().equals("config")) {
            field.set(null, object);
            return;
        }
        propertyName = propertyName.replace("$", ".");
        if (section != null && section.get(propertyName) != null) {
            if(field.getType().isEnum()){
                Enum[] enumConstants = (Enum[]) field.getType().getEnumConstants();
                String value = section.getString(propertyName);
                for (Enum enumConstant : enumConstants) {
                    if(value.equals(enumConstant.name().replace("_","")) || value.equals(enumConstant.name())){
                        field.set(fieldSetObj, enumConstant);
                        return;
                    }
                }
                for (Enum enumConstant : enumConstants) {
                    try {
                        String mappingName = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                        if (mappingName!=null && mappingName.equals(value)) {
                            field.set(fieldSetObj, enumConstant);
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }else
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
                printlnError(object);
                e.printStackTrace();
            }
        } else {
            if (field.getType() == List.class) {
                field.set(fieldSetObj, new ArrayList<>());
                return;
            }
        }
        if (mappingInject(fieldSetObj, section, field, propertyName)) {
            return;
        }
        if (Map.class.isAssignableFrom(field.getType())) {
            executeMapTypeMapping(fieldSetObj,section, propertyName, field);
        }


    }

    private static AbstractMap<Object, Object> executeMapTypeMapping(Object instance,ConfigurationSection section, String propertyName, Field field) throws IllegalAccessException {
        AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
        Type type = field.getGenericType();
        Type[] typeArguments;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeArguments = parameterizedType.getActualTypeArguments();
        } else {
            throw new IllegalArgumentException("泛型参数缺少");
        }
        field.set(instance, map);

        // 判断第二个泛型参数的类型
        if (typeArguments.length >= 2) {
            if(typeArguments[0].getClass().isEnum()){
                HashMap<Enum<?>, String> enumMappingName = getEnumMappingName(typeArguments[0].getClass());
                injectMapping(section, propertyName, typeArguments, map,enumMappingName);
            }else{
                injectMapping(section, propertyName, typeArguments, map);
            }
        }
        return map;
    }

    private static void injectMapping(ConfigurationSection section, String propertyName, Type[] typeArguments, AbstractMap<Object, Object> map,HashMap<Enum<?>, String> enumMappingName) {
        Type typeArgument = typeArguments[1];
        if (typeArgument.getTypeName().equals("java.util.List<java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        List<String> stringList = listSection.getStringList(configKey);
                        map.put(key, stringList);
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.lang.String")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        map.put(key, listSection.getString(configKey));
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.util.List<java.lang.String>>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        ConfigurationSection mapSection = listSection.getConfigurationSection(configKey);
                        if(mapSection!=null) {
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, List<String>> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getStringList(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.util.Map<java.lang.String, java.util.List<java.lang.String>>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        ConfigurationSection mapSection = listSection.getConfigurationSection(configKey);
                        if(mapSection!=null) {
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, List<String>> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getStringList(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        }else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        ConfigurationSection mapSection = listSection.getConfigurationSection(configKey);
                        if(mapSection!=null) {
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getString(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        }
    }


    private static void injectMapping(ConfigurationSection section, String propertyName, Type[] typeArguments, AbstractMap<Object, Object> map) {
        Type typeArgument = typeArguments[1];
        if (typeArgument.getTypeName().equals("java.util.List<java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        List<String> stringList = listSection.getStringList(key);
                        map.put(key, stringList);
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.lang.String")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        map.put(key, listSection.getString(key));
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.util.List<java.lang.String>>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        ConfigurationSection mapSection = listSection.getConfigurationSection(key);
                        if(mapSection!=null) {
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, List<String>> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getStringList(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        } else if (typeArgument.getTypeName().equals("java.util.Map<java.lang.String, java.util.List<java.lang.String>>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        ConfigurationSection mapSection = listSection.getConfigurationSection(key);
                        if(mapSection!=null) {
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, List<String>> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getStringList(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        }else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        ConfigurationSection mapSection = listSection.getConfigurationSection(key);
                        if(mapSection!=null){
                            Set<String> keys1 = mapSection.getKeys(false);
                            LinkedHashMap<String, String> map1 = new LinkedHashMap<>();
                            for (String s : keys1) {
                                map1.put(s, mapSection.getString(s));
                            }
                            map.put(key, map1);
                        }
                    }
                }
            }
        }
    }


    public static boolean mappingInject(Object object, ConfigurationSection section, Field field, String propertyName) throws IllegalAccessException {
        return injectMapping(object, section, field, propertyName) |
                injectFoldMapping(object, section, field, propertyName) |
                injectFlatMapping(object, section, field,propertyName);
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


    private static boolean injectFlatMapping(Object object, ConfigurationSection section,Field field,String propertyName) {
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
            } else {
                throw new IllegalArgumentException("泛型参数缺少");
            }
            //(Class<?>)
            Map<Object, Object> map = (Map) createInstance(clazz);
            field.set(object, map);
            if (getFlatMapping.stringKeys().length != 0) {
                for (String key : getFlatMapping.stringKeys()) {
                    ConfigurationSection subSection = null;
                    if (section != null) {
                        if(getFlatMapping.nested()){
                            subSection = section.getConfigurationSection(propertyName+"."+key.replace("$", "."));
                        }else{
                            subSection = section.getConfigurationSection(key.replace("$", "."));
                        }
                    }
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, key, object);
                    ConfigMapping.loadFromConfig(instance, null, subSection);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(key, instance);
                }
            } else {
                Enum[] enumConstants = ((Class<? extends Enum>) typeArguments[0]).getEnumConstants();
                for (Enum enumConstant : enumConstants) {
                    String name;
                    if(getPrivateAndSuperField(enumConstant, "mappingName") == null){
                        name = enumConstant.name();
                    }else{
                        name = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                    }
                    ConfigurationSection subSection = null;
                    if (section != null) {
                        if(getFlatMapping.nested()) {
                            subSection = (ConfigurationSection) getElementIgnoreCase(section, propertyName+"."+name.replace("_", ""));
                        }else{
                            subSection = (ConfigurationSection) getElementIgnoreCase(section, name.replace("_", ""));
                        }
                    }
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, enumConstant, object);
                    ConfigMapping.loadFromConfig(instance, null, subSection);
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
            ConfigurationSection foldSection = null;
            if (section != null) {
                foldSection = section.getConfigurationSection(propertyName);
            }
            Object instance = createInstance((Class<?>) field.getType());
            injectDefaultValue(instance, propertyName, object);
            field.set(object, instance);
            if (foldSection == null) {
                ConfigMapping.loadFromConfig(instance, null, null);
            } else {
                ConfigMapping.loadFromConfig(instance, null, section.getConfigurationSection(propertyName));
            }
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
            } else {
                throw new IllegalArgumentException("泛型参数缺少");
            }
            Map<String, Object> map = (Map) createInstance(field.getType());
            field.set(object, map);
            if (section != null) {
                ConfigurationSection mapperSection = section.getConfigurationSection(propertyName);
                if (mapperSection == null) return true;
                Set<String> keys = mapperSection.getKeys(false);
                for (String key : keys) {
                    ConfigurationSection configurationSection = mapperSection.getConfigurationSection(key);
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, key, object);
                    ConfigMapping.loadFromConfig(instance, null, configurationSection);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(key, instance);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
