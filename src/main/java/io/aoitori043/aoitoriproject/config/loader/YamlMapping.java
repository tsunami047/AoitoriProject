package io.aoitori043.aoitoriproject.config.loader;

import io.aoitori043.aoitoriproject.config.*;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;

import static io.aoitori043.aoitoriproject.config.impl.MapperInjection.runAnnotatedMethodByField;
import static io.aoitori043.aoitoriproject.config.loader.MapperEvaluation.getValue;

/**
 * @Author: natsumi
 * @CreateTime: 2024-01-24  22:36
 * @Description: ?
 */
public class YamlMapping {

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

    public static void printlnError(Object object){
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getName().equals("index")) {
                try {
                    String fieldValue = (String) field.get(object);
                    System.out.println("问题可能出自文件/键："+fieldValue);
                }catch (Exception e){
                    System.out.println("缺少index，无法定位错误位置");
                    e.printStackTrace();
                }
            }
        }
    }


    public static void loadFromConfig(Object object, YamlConfiguration yamlConfiguration,String parentName) {
        Class<?> clazz = object.getClass();
        if (isInnerClass(clazz) || clazz.isAnnotationPresent(ConfigProperties.class)) {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(NonConfigProperty.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, yamlConfiguration, field);
                }else{
                    String propertyName = field.getName();
                    try {
                        getValue(object, yamlConfiguration, field, propertyName,parentName);
                        runAnnotatedMethodByField(object,field.getName());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        printlnError(object);
                    }
                }
            }
        }else {
            for (Field field : object.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    findConversion(object, yamlConfiguration, field);
                }
            }
        }
    }

    private static void findConversion(Object object, YamlConfiguration yamlConfiguration, Field field) {
        ConfigProperty annotation = field.getAnnotation(ConfigProperty.class);
        String[] values = annotation.values();
        try {
            String propertyName = null;
            if(yamlConfiguration!=null){
                if(values[0].equals("useVariableName")){
                    propertyName = field.getName();
                }else{
                    for (String value : values) {
                        if (yamlConfiguration.get(value) != null) {
                            propertyName = value;
                        }
                    }
                    if(propertyName == null){
                        propertyName = field.getName();
                    }
                }
            }else{
                propertyName = field.getName();
            }
            getValue(object, yamlConfiguration, field, propertyName,null);
            runAnnotatedMethodByField(object,field.getName());
        } catch (IllegalAccessException e) {
            printlnError(object);
            e.printStackTrace();
        }
    }

}
