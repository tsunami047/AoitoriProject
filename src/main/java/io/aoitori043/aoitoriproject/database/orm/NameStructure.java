package io.aoitori043.aoitoriproject.database.orm;

import io.aoitori043.aoitoriproject.database.orm.sign.Column;
import io.aoitori043.aoitoriproject.database.orm.sign.Entity;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:20
 * @Description: ?
 */
public class NameStructure {

    public static HashMap<String,String> sqlFieldNameMap = new HashMap<>();
    public static HashMap<Class,String> sqlTableNameMap = new HashMap<>();

    //通过hash减少时间
    public String getTableName(Class clazz){
        String tableName = sqlTableNameMap.get(clazz);
        if(tableName!=null){
            return sqlTableNameMap.get(clazz);
        }
        if (clazz.isAnnotationPresent(Entity.class)) {
            Entity annotation = (Entity) clazz.getAnnotation(Entity.class);
            if (!annotation.tableName().isEmpty()) {
                sqlTableNameMap.put(clazz,annotation.tableName());
                return annotation.tableName();
            }
        }
        sqlTableNameMap.put(clazz,clazz.getSimpleName());
        return clazz.getSimpleName();
    }

    public String underscoreToCamel(String fieldName) {
        String[] parts = fieldName.split("_");
        StringBuilder camelCase = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                camelCase.append(part);
            } else {
                camelCase.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return camelCase.toString();
    }

    public String camelToUnderscore(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public String getFieldName(Field field){
        String name = field.getName();
        String sqlColumnName = sqlFieldNameMap.get(name);
        if(sqlColumnName != null){
            return sqlColumnName;
        }
        try {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    return column.name();
                }
            }
            sqlColumnName = camelToUnderscore(name);
            sqlFieldNameMap.put(name,sqlColumnName);
            return sqlColumnName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getFieldName(Class clazz, String fieldName){
        String sqlColumnName = sqlFieldNameMap.get(fieldName);
        if(sqlColumnName != null){
            return sqlColumnName;
        }
        try {
            Field field = clazz.getField(fieldName);
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.name().isEmpty()) {
                    return column.name();
                }
            }
            sqlColumnName = camelToUnderscore(fieldName);
            sqlFieldNameMap.put(fieldName,sqlColumnName);
            return sqlColumnName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String getFieldSQLType(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.type().isEmpty()) {
                return column.type().toUpperCase();
            }
        }
        Class<?> fieldType = field.getType();
        if (fieldType == String.class) {
            return "VARCHAR(255)";
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return "INT";
        } else if (fieldType == long.class || fieldType == Long.class) {
            return "BIGINT";
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "BOOLEAN";
        } else if (fieldType == float.class || fieldType == Float.class) {
            return "FLOAT";
        } else if (fieldType == double.class || fieldType == Double.class) {
            return "DOUBLE";
        } else if (fieldType == byte.class || fieldType == Byte.class) {
            return "TINYINT";
        } else if (fieldType == short.class || fieldType == Short.class) {
            return "SMALLINT";
        } else if (fieldType == char.class || fieldType == Character.class) {
            return "CHAR";
        } else if (fieldType == Date.class || fieldType == java.sql.Date.class || fieldType == java.sql.Timestamp.class) {
            return "DATETIME";
        }else {
            // 其他类型按照需求自行添加
            return "VARCHAR(255)";
        }
    }

}
