package io.aoitori043.aoitoriproject.config.impl;

import io.aoitori043.aoitoriproject.config.*;
import org.bukkit.configuration.ConfigurationSection;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static io.aoitori043.aoitoriproject.config.InvalidUtil.performNullCheck;
import static io.aoitori043.aoitoriproject.config.impl.YamlFieldMapper.getValue;

/**
 * @Author: natsumi
 * @CreateTime: 2024-01-17  06:10
 * @Description: ?
 */
public class ConfigMapping {

    static ScriptEngineManager manager = new ScriptEngineManager();
    static ScriptEngine engine = manager.getEngineByName("js");

    private static String executeJavaScriptCode(String jsCode) {
        try {
            return String.valueOf(engine.eval(jsCode));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static void performJS(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExecuteJS.class)) {
                try {
                    try {
                        field.setAccessible(true);
                        String fieldValue = (String) field.get(object);
                        if(fieldValue == null){
                            continue;
                        }
                        field.set(object,String.valueOf(executeJavaScriptCode(fieldValue)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isInnerClass(Class<?> clazz) {
        String className = clazz.getName();
        return className.contains("$");
    }


    public static void loadFromConfig(Object object,String parentName,ConfigurationSection section) {
        if (section == null) {
            return;
        }
        Class<?> clazz = object.getClass();
        if (isInnerClass(clazz) || clazz.isAnnotationPresent(ConfigProperties.class)) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(NonConfigProperty.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, section, field,parentName);
                }else{
                    String propertyName = field.getName();
                    try {
                        getValue(object, section, field, propertyName,parentName);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, section, field,parentName);
                }
            }
        }
        try {
            performNullCheck(object);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void findConversion(Object object, ConfigurationSection section, Field field,String parentName) {
        field.setAccessible(true);
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
        String propertyName = annotation.value();
        try {
            if(propertyName.equals("useVariableName")){
                propertyName = field.getName();
            }
            getValue(object, section, field, propertyName,parentName);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> T createInstance(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    public static boolean isStaticField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers);
    }



//
//
//
//    private static void getValue(Object object, ConfigurationSection section, Field field, String propertyName) throws IllegalAccessException {
//        field.setAccessible(true);
//        Object fieldSetObj = isStaticField(field) ? null : object;
//        if(isStaticField(field) && field.getName().equals("config")){
//            field.set(null,object);
//            return;
//        }
//        propertyName = propertyName.replace("$", ".");
//        if(section.getMap(propertyName) != null){
//            if (field.getType() == String.class) {
//                String string = section.getString(propertyName);
//                if(string.equalsIgnoreCase("null")){
//                    return;
//                }
//                field.set(fieldSetObj, string);
//            } else if (field.getType() == int.class) {
//                field.set(fieldSetObj, section.getInt(propertyName));
//            } else if (field.getType() == double.class || field.getType() == float.class) {
//                field.set(fieldSetObj, section.getDouble(propertyName));
//            } else if (field.getType() == boolean.class) {
//                field.set(fieldSetObj, section.getBoolean(propertyName));
//            } else if (field.getType() == List.class) {
//                field.set(fieldSetObj, section.getStringList(propertyName));
//            }
//        }
//
//
//        GetMapping referTo = field.getAnnotation(GetMapping.class);
//        if(referTo!=null){
//            if(field.getType() == LinkedHashMap.class){
//                try {
//                    ConfigurationSection referToSection = section.getConfigurationSection(propertyName);
//                    if(referToSection==null){
//                        return;
//                    }
//                    fillInFields(object, field, propertyName, referToSection, referTo);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        }
//        GetFoldMapping fold = field.getAnnotation(GetFoldMapping.class);
//        if(fold!=null){
//            try {
//                ConfigurationSection referToSection = section.getConfigurationSection(propertyName);
//                Object instance = createInstance(fold.mapper());
//                YamlMapping.fillInFields(object, field, propertyName, referToSection, instance);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        GetFlatMapping flatReferTo = field.getAnnotation(GetFlatMapping.class);
//        if(flatReferTo!=null){
//            if(field.getType() == LinkedHashMap.class){
//                try {
//                    LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
//                    if(flatReferTo.stringKeys().length != 0) {
//                        for (String key : flatReferTo.stringKeys()) {
//                            ConfigurationSection subSection = section.getConfigurationSection(key.replace("$", "."));
//                            Object instance = createInstance(flatReferTo.mapper());
//
//                            ConfigMapping.loadFromConfig(instance, subSection);
//                            try {
//                                Field index = instance.getClass().getField("index");
//                                index.set(instance,key);
//                            }catch (Exception e){}
//                            try {
//                                Field parent = instance.getClass().getField("parent");
//                                parent.set(instance,object);
//                            }catch (Exception e){}
//                            map.putMap(key, instance);
//                        }
//                    }else{
//                        Enum[] enumConstants = flatReferTo.enumKeys().getEnumConstants();
//                        for (Enum enumConstant :enumConstants){
//                            String name = enumConstant.name();
//                            ConfigurationSection subSection = section.getConfigurationSection(name.replace("$", "."));
//                            Object instance = createInstance(flatReferTo.mapper());
//                            if(subSection != null){
//                                ConfigMapping.loadFromConfig(instance, subSection);
//                            }
//                            try {
//                                Field index = instance.getClass().getField("index");
//                                index.set(instance,enumConstant);
//                            }catch (Exception e){}
//                            try {
//                                Field parent = instance.getClass().getField("parent");
//                                parent.set(instance,object);
//                            }catch (Exception e){}
//                            map.putMap(name, instance);
//                        }
//                    }
//                    field.set(object, map);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }
//
//    private static void fillInFields(Object object, Field field, String propertyName, ConfigurationSection referToSection, GetMapping referTo) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        Set<String> stringKeys = referToSection.getKeys(false);
//        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
//        for (String key : stringKeys) {
//            ConfigurationSection subSection = referToSection.getConfigurationSection(key);
//            Object instance = createInstance(referTo.mapper());
//            ConfigMapping.loadFromConfig(instance, subSection);
//            try {
//                Field index = instance.getClass().getField("index");
//                index.set(instance, propertyName);
//            }catch (Exception e){}
//            try {
//                Field parent = instance.getClass().getField("parent");
//                parent.set(instance, object);
//            }catch (Exception e){}
//            map.putMap(key,instance);
//        }
//        field.set(object, map);
//    }


}
