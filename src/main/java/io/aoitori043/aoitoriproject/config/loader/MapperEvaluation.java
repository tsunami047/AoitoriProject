package io.aoitori043.aoitoriproject.config.loader;

import io.aoitori043.aoitoriproject.config.*;
import io.aoitori043.aoitoriproject.config.impl.MapperInjection;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import lombok.ToString;
import lombok.var;
import org.bukkit.configuration.ConfigurationSection;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.util.*;

import static io.aoitori043.aoitoriproject.config.impl.MapperInjection.runAnnotatedMethodByField;
import static io.aoitori043.aoitoriproject.config.loader.ConfigMapping.*;
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

    public static HashMap<Enum<?>, String> getEnumMappingName(Class clazz) {
        HashMap<Enum<?>, String> map = new HashMap<>();
        Enum[] enumConstants = (Enum[]) clazz.getEnumConstants();
        for (Enum enumConstant : enumConstants) {
            try {
                String mappingName = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                if (mappingName != null) {
                    map.put(enumConstant, mappingName);
                } else {
                    map.put(enumConstant, enumConstant.name());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static void getValue(Object parent,Object object, ConfigurationSection section, Field field, String propertyName, String parentName) throws IllegalAccessException {
        try {
            if (field.isAnnotationPresent(InjectYaml.class)) {
                return;
            }
            field.setAccessible(true);
            Object fieldSetObj = isStaticField(field) ? null : object;
            if (field.isAnnotationPresent(Inject.class)) {
                Inject annotation = field.getAnnotation(Inject.class);
                switch (annotation.type()) {
                    case PARENT_OBJECT: {
                        field.set(fieldSetObj, parent);
                        return;
                    }
                    case PARENT_NAME: {
                        field.set(fieldSetObj, parentName);
                        return;
                    }
                    case YAML: {
                        field.set(fieldSetObj, section);
                        return;
                    }
                    case CONFIG: {
                        field.set(null, object);
                        return;
                    }
                }
            }
            if (field.getName().equals("yaml")) {
                field.set(fieldSetObj, section);
                return;
            }
            if (isStaticField(field) && field.getName().equals("config")) {
                field.set(null, object);
                return;
            }
            propertyName = propertyName.replace("$", ".");
            if (section != null && section.get(propertyName) != null) {
                if (field.getType().isEnum()) {
                    Enum[] enumConstants = (Enum[]) field.getType().getEnumConstants();
                    String value = section.getString(propertyName);
                    boolean hasMatch = false;
                    for (Enum enumConstant : enumConstants) {
                        if (value.equalsIgnoreCase(enumConstant.name().replace("_", "")) || value.equalsIgnoreCase(enumConstant.name())) {
                            field.set(fieldSetObj, enumConstant);
                            hasMatch = true;
                            break;
                        }
                    }
                    if (!hasMatch) {
                        for (Enum enumConstant : enumConstants) {
                            try {
                                String mappingName = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                                if (mappingName != null && mappingName.equalsIgnoreCase(value)) {
                                    field.set(fieldSetObj, enumConstant);
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println(parentName + " " + propertyName + " 不能是 " + value);
                    }
                } else if (field.getType() == String.class) {
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
                } else if (field.getType() == long.class || field.getType() == Long.class) {
                    field.set(fieldSetObj, section.getLong(propertyName));
                    return;
                } else if (field.getType() == List.class) {
                    field.set(fieldSetObj, new ArrayList<>(section.getStringList(propertyName)));
                    return;
                }
            } else if (parentName != null && field.getName().equals("index") && object != null) {
                try {
                    field.setAccessible(true);
                    if (field.get(fieldSetObj)!=null) return;
                    field.set(fieldSetObj, parentName);
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
            if (field.getType().isEnum()) {
                return;
            }
            if (mappingInject(parent, fieldSetObj, section, field, propertyName)) {
                return;
            }
            if (Map.class.isAssignableFrom(field.getType())) {
                executeMapTypeMapping(fieldSetObj, section, propertyName, field);
            }
        }finally {
            runAnnotatedMethodByField(object,field.getName());
        }


    }


    private static AbstractMap<Object, Object> executeMapTypeMapping(Object instance, ConfigurationSection section, String propertyName, Field field) throws IllegalAccessException {
        Object o = field.get(instance);
        //防止之前被注入了map
        boolean pass = false;
        try{
            var o1 = (LinkedHashMap) o;
            if (o1.isEmpty()) {
                pass = true;
            }
        }catch (Exception e){}
        if(!pass && o !=null && !field.isAnnotationPresent(ConfigProperty.class)){
            return null;
        }
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

        if (section == null || section.get(propertyName) == null) return map;
        // 判断第二个泛型参数的类型
        if (typeArguments.length >= 2) {
            if (((Class)typeArguments[0]).isEnum()) {
                HashMap<Enum<?>, String> enumMappingName = getEnumMappingName((Class) typeArguments[0]);
                injectMapping(section, propertyName, typeArguments, map, enumMappingName);
            } else {
                injectMapping(section, propertyName, typeArguments, map);
            }
        }
        return map;
    }

//    private static AbstractMap<Object, Object> executeMapTypeMapping_dg(Object instance, ConfigurationSection section, String propertyName, Type type,AbstractMap<Object, Object> map) {
//        if (type instanceof ParameterizedType) {
//            ParameterizedType parameterizedType = (ParameterizedType) type;
//            Type[] typeArguments = parameterizedType.getActualTypeArguments();
//            //public static HashMap<String, HashMap<String, List<String>>> gemLevelQualityMapping = new HashMap<>();
//            //public static LinkedHashMap<Integer,EquipmentMapper.Quality>
//            if(List.class.isAssignableFrom(((ParameterizedTypeImpl) typeArguments[1]).getRawType())){
//
//            }
//        }else{
//            if (type == String.class) {
//                Type typeArgument = typeArguments[1];
//                System.out.println(((ParameterizedTypeImpl) typeArgument).getOwnerType());
//                System.out.println(Map.class.isAssignableFrom(((ParameterizedTypeImpl) typeArgument).getRawType()));
//
//            } else if (type == Integer.class) {
//
//            } else if (type == Double.class) {
//
//            } else if (type == Float.class) {
//
//            } else if (((Class)type).isEnum()) {
//                HashMap<Enum<?>, String> enumMappingName = getEnumMappingName(typeArguments[0].getClass());
//            }
//        }
//
//        AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
//        Type type = field.getGenericType();
//        Type[] typeArguments;
//        if (type instanceof ParameterizedType) {
//            ParameterizedType parameterizedType = (ParameterizedType) type;
//            typeArguments = parameterizedType.getActualTypeArguments();
//        } else {
//            throw new IllegalArgumentException("泛型参数缺少");
//        }
//        field.set(instance, map);
//
//        // 判断第二个泛型参数的类型
//        if (typeArguments.length >= 2) {
//            if (typeArguments[0].getClass().isEnum()) {
//                HashMap<Enum<?>, String> enumMappingName = getEnumMappingName(typeArguments[0].getClass());
//                injectMapping(section, propertyName, typeArguments, map, enumMappingName);
//            } else {
//                injectMapping(section, propertyName, typeArguments, map);
//            }
//        }
//        return map;
//    }


    private static void extractToMap(ConfigurationSection section, String propertyName, Type[] typeArguments, AbstractMap<Object, Object> map) {
        Type typeArgument = typeArguments[1];
        if (typeArgument instanceof ParameterizedTypeImpl) {
            ConfigurationSection listSection = section.getConfigurationSection(propertyName);
            if (listSection != null) {
                Class<?> rawType = ((ParameterizedTypeImpl) typeArgument).getRawType();
                if (Map.class.isAssignableFrom(rawType)) {
                    AbstractMap<Object, Object> tempMap = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(rawType);
                    for (String key : listSection.getKeys(false)) {
                        ConfigurationSection subSection = listSection.getConfigurationSection(key);
                        extractToMap(subSection, key, typeArguments, tempMap);
                    }
                } else if (List.class.isAssignableFrom(rawType)) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        List<String> stringList = listSection.getStringList(key);
                        map.put(key, stringList);
                    }
                }
            }
        }
    }

    /*
    List
    String
    枚举
    这种直接开始获取

    如果是map，就再嵌套一次
     */
    public static void mai1n(String[] args) throws Exception {
        Class<Test> testClass = Test.class;
        Field field = testClass.getDeclaredField("gemLevelQualityMapping");
        System.out.println(field);
        AbstractMap<Object, Object> map = (AbstractMap<Object, Object>) ReflectASMUtil.createInstance(field.getType());
        Type type = field.getGenericType();
        Type[] typeArguments;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeArguments = parameterizedType.getActualTypeArguments();
        } else {
            throw new IllegalArgumentException("泛型参数缺少");
        }
        if (typeArguments[0] instanceof ParameterizedTypeImpl) {
            ParameterizedType parameterizedType = (ParameterizedTypeImpl) typeArguments[0];
            typeArguments = parameterizedType.getActualTypeArguments();
            throw new IllegalArgumentException("map-key 不能含有泛型");
        } else {
            Class mapKeyType = (Class) typeArguments[0];
            if (mapKeyType == String.class) {
                Type typeArgument = typeArguments[1];
                System.out.println(((ParameterizedTypeImpl) typeArgument).getOwnerType());
                System.out.println(Map.class.isAssignableFrom(((ParameterizedTypeImpl) typeArgument).getRawType()));
//                while (typeArguments.length >= 2 && typeArgument instanceof ParameterizedTypeImpl) {
//                    System.out.println(typeArgument);
//                    ParameterizedType parameterizedType = (ParameterizedTypeImpl) typeArgument;
//                    typeArguments = parameterizedType.getActualTypeArguments();
//                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
//                    System.out.println(actualTypeArguments[0] + " 0");
//                    System.out.println(actualTypeArguments[1] + " 1");
//                }

            } else if (mapKeyType == Integer.class) {

            } else if (mapKeyType == Double.class) {

            } else if (mapKeyType == Float.class) {

            } else if (mapKeyType.isEnum()) {
                HashMap<Enum<?>, String> enumMappingName = getEnumMappingName(typeArguments[0].getClass());
            }
        }

    }

    private static void injectMapping(ConfigurationSection section, String propertyName, Type[] typeArguments, AbstractMap<Object, Object> map, HashMap<Enum<?>, String> enumMappingName) {
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
                        if (mapSection != null) {
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
                        if (mapSection != null) {
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
        } else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    for (Map.Entry<Enum<?>, String> entry : enumMappingName.entrySet()) {
                        Enum<?> key = entry.getKey();
                        String configKey = entry.getValue();
                        ConfigurationSection mapSection = listSection.getConfigurationSection(configKey);
                        if (mapSection != null) {
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
                        if (mapSection != null) {
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
                        if (mapSection != null) {
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
        } else if (typeArgument.getTypeName().equals("java.util.LinkedHashMap<java.lang.String, java.lang.String>")) {
            if (section != null) {
                ConfigurationSection listSection = section.getConfigurationSection(propertyName);
                if (listSection != null) {
                    Set<String> keys = listSection.getKeys(false);
                    for (String key : keys) {
                        ConfigurationSection mapSection = listSection.getConfigurationSection(key);
                        if (mapSection != null) {
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

    public static boolean mappingInject(Object parent,Object object, ConfigurationSection section, Field field, String propertyName) throws IllegalAccessException {
        return injectMapping(parent,object, section, field, propertyName) | injectFoldMapping(parent,object, section, field, propertyName) | injectFlatMapping(parent,object, section, field, propertyName) | injectClassifyMapping(parent,object, section, field, propertyName);
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
        for (Field declaredField : ConfigMapping.getAllFields(instance.getClass())) {
            try {
                Inject annotation = declaredField.getAnnotation(Inject.class);
                declaredField.setAccessible(true);
                if (annotation !=null) {
                    switch (annotation.type()) {
                        case INDEX: {
                            declaredField.set(instance, indexData);
                            continue;
                        }
                        case PARENT: {
                            declaredField.set(instance, parentData);
                            continue;
                        }
                    }
                }
                if (declaredField.getName().equals("index")) {
                    declaredField.set(instance, indexData);
                } else if (declaredField.getName().equals("parent")) {
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

    private static boolean injectFlatMapping(Object parent,Object object, ConfigurationSection section, Field field, String propertyName) {
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
                        if (getFlatMapping.nested()) {
                            subSection = section.getConfigurationSection(propertyName + "." + key.replace("$", "."));
                        } else {
                            subSection = section.getConfigurationSection(key.replace("$", "."));
                        }
                    }
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, key, object);
                    ConfigMapping.loadFromConfig(object,instance, null, subSection);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(key, instance);
                }
            } else {
                Enum[] enumConstants = ((Class<? extends Enum>) typeArguments[0]).getEnumConstants();
                for (Enum enumConstant : enumConstants) {
                    String name;
                    if (getPrivateAndSuperField(enumConstant, "mappingName") == null) {
                        name = enumConstant.name();
                    } else {
                        name = (String) getPrivateAndSuperField(enumConstant, "mappingName");
                    }
                    ConfigurationSection subSection = null;
                    if (section != null) {
                        if (getFlatMapping.nested()) {
                            subSection = (ConfigurationSection) getElementIgnoreCase(section, propertyName + "." + name.replace("_", ""));
                        } else {
                            subSection = (ConfigurationSection) getElementIgnoreCase(section, name.replace("_", ""));
                        }
                    }
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, enumConstant, object);
                    ConfigMapping.loadFromConfig(object,instance, null, subSection);
                    MapperInjection.runAnnotatedMethods(instance);
                    map.put(enumConstant, instance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean injectFoldMapping(Object parent,Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
            GetFoldMapping getFoldMapping = field.getAnnotation(GetFoldMapping.class);
            if (!isInnerClass(field.getType()) && getFoldMapping == null) {
                return false;
            }
            Object o = field.get(object);
            if(o!=null){
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
                ConfigMapping.loadFromConfig(object,instance, null, null);
            } else {
                ConfigMapping.loadFromConfig(object,instance, null, section.getConfigurationSection(propertyName));
            }
            MapperInjection.runAnnotatedMethods(instance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    public static String getIndexByReflection(Object object){
        try {
            Field index = object.getClass().getDeclaredField("index");
            index.setAccessible(true);
            return (String) index.get(object);
        }catch (NoSuchFieldException e){
            for (Field declaredField : getAllFields(object.getClass())) {
                Inject annotation = declaredField.getAnnotation(Inject.class);
                if (annotation!=null && annotation.type() == Inject.InjectType.INDEX){
                    declaredField.setAccessible(true);
                    try {
                        return (String) declaredField.get(object);
                    }catch (Exception e2){
                        return null;
                    }
                }
            }
        }catch (Exception ignore){}
        return null;
    }

    private static boolean injectClassifyMapping(Object parent,Object object, ConfigurationSection section, Field field, String propertyName) {
        try {
            GetClassifyMapping getClassifyMapping = field.getAnnotation(GetClassifyMapping.class);
            if (getClassifyMapping == null || !Map.class.isAssignableFrom(field.getType())) {
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
            String anchor = getClassifyMapping.anchor();
            String indexByReflection = getIndexByReflection(object);
            if (section != null) {
                ConfigurationSection mapperSection = section.getConfigurationSection(propertyName);
                if (mapperSection == null) return true;
                Set<String> keys = mapperSection.getKeys(false);
                for (String key : keys) {
                    ConfigurationSection configurationSection = mapperSection.getConfigurationSection(key);
                    if (configurationSection == null) {
                        continue;
                    }
                    Class designateClass = null;
                    String sectionType = configurationSection.getString(anchor);
                    GetClassifyMapping.ClassDesignation[] appoint = getClassifyMapping.appoint();
                    for (GetClassifyMapping.ClassDesignation classDesignation : appoint) {
                        if (classDesignation.key().equals(sectionType)) {
                            designateClass = classDesignation.value();
                        }
                    }
                    if (designateClass == null) continue;
                    Object instance = createInstance(designateClass);
                    injectDefaultValue(instance, key, object);
                    ConfigMapping.loadFromConfig(object,instance, indexByReflection, configurationSection);
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

    private static boolean injectMapping(Object parent,Object object, ConfigurationSection section, Field field, String propertyName) {
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
                    if(Arrays.stream(getMapping.ignoreKey()).anyMatch(key::equalsIgnoreCase)){
                        continue;
                    }
                    ConfigurationSection configurationSection = mapperSection.getConfigurationSection(key);
                    if (configurationSection == null) {
                        continue;
                    }
                    Object instance = createInstance((Class<?>) typeArguments[1]);
                    injectDefaultValue(instance, key, object);
                    ConfigMapping.loadFromConfig(object,instance, null, configurationSection);
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


    public static class Test {
        public static HashMap<String, HashMap<String, List<String>>> gemLevelQualityMapping = new HashMap<>();
    }


}
