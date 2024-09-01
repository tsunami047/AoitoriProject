package io.aoitori043.aoitoriproject.database.orm;

import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.orm.sign.*;
import io.aoitori043.aoitoriproject.database.orm.sql.SQLService;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:01
 * @Description: ?
 */
public class TableBuilder {

    public SQLClient sqlClient;

    public TableBuilder(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public enum createStatus{
        CREATE_SUCCESS, CREATE_FAILURE, LOST_FOREIGN_TABLE
    }

    public createStatus createTable(Class<?> clazz) {
        String tableName = sqlClient.nameStructure.getTableName(clazz);
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (");

        // 获取所有字段
        Field[] fields = clazz.getDeclaredFields();
        List<String> primaryKeyFields = new ArrayList<>(); // 存储主键字段名

        Map<String,Class<?>> foreignKeyMap = new HashMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ManyToMany.class) ||
                    field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class)){
                //对象映射不需要创建对应字段
                continue;
            }

            String fieldName = sqlClient.nameStructure.getFieldName(field);
            String fieldType = sqlClient.nameStructure.getFieldSQLType(field);

            // 处理主键字段
            if (field.isAnnotationPresent(Key.class)) {
                primaryKeyFields.add(fieldName);
            }
            if (field.isAnnotationPresent(PlayerName.class)) { // 处理自增ID字段
                sql.append(fieldName).append(" NVARCHAR(255)");
            }else {
                sql.append(fieldName).append(" ").append(fieldType);
            }
            if (field.isAnnotationPresent(AggregateRoot.class)) { // 处理自增ID字段
                sql.append(" AUTO_INCREMENT UNIQUE");
            }
            if(field.isAnnotationPresent(ForeignAggregateRoot.class)){
                foreignKeyMap.put(fieldName, field.getAnnotation(ForeignAggregateRoot.class).mapEntity());
//
            }
            if(!field.isAnnotationPresent(Nullable.class)){
                sql.append(" NOT NULL");
            }
            sql.append(",");
        }

        // 添加主键定义
        if (primaryKeyFields.size() == 1) {
            sql.append("PRIMARY KEY (").append(primaryKeyFields.get(0)).append("),");
        } else if (primaryKeyFields.size() > 1) {
            sql.append("PRIMARY KEY (");
            for (String fieldName : primaryKeyFields) {
                sql.append(fieldName).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append("),");
        }

        for (Map.Entry<String, Class<?>> entry : foreignKeyMap.entrySet()) {
            String fieldName = entry.getKey();
            Class<?> entityClass = entry.getValue();
            SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(entityClass);
            if (entityAttribute == null) {
                return createStatus.LOST_FOREIGN_TABLE;
            }
            switch (entityAttribute.getCacheType()) {
                case ONLY_MYSQL:
                case PLAYER_EXCLUSIVE_DATA:
                case HIGH_VALUE_DATA:
                    sql.append(String.format("FOREIGN KEY (%s) REFERENCES %s(%s) ON DELETE CASCADE,",fieldName,entityAttribute.getTableName(),entityAttribute.getIdFieldName()));
                    break;
            }
//            entityAttribute.getEmbeddedMapFieldProperties().forEach((key, value) -> {
//                if (value.valueType == entityClass) {
//
//                }
//
//            });
        }
        List<String> indexedFields = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Index.class)) {
                String fieldName = sqlClient.nameStructure.getFieldName(field);
                String indexSQL = "INDEX index_" +  fieldName + " (" + fieldName + "),";
                sql.append(indexSQL);
                indexedFields.add(fieldName);
            }
        }
        if (indexedFields.size() > 1) {
            StringBuilder compositeIndexSQL = new StringBuilder("INDEX index_composite_");
            for (String fieldName : indexedFields) {
                compositeIndexSQL.append(fieldName).append("_");
            }
            compositeIndexSQL.deleteCharAt(compositeIndexSQL.length() - 1);  // 删除最后一个下划线
            compositeIndexSQL.append(" (");
            for (String fieldName : indexedFields) {
                compositeIndexSQL.append(fieldName).append(", ");
            }
            compositeIndexSQL.delete(compositeIndexSQL.length() - 2, compositeIndexSQL.length());  // 删除最后一个逗号和空格
            compositeIndexSQL.append("),");
            sql.append(compositeIndexSQL);
        }



        /*
        CREATE TABLE IF NOT EXISTS ad_dropitems (
        id INT AUTO_INCREMENT NOT NULL,
        drops_id INT NOT NULL,
        index INT NOT NULL,
        rewards VARCHAR(255) NOT NULL,
        PRIMARY KEY (id,index),
        FOREIGN KEY (drops_id) REFERENCES ad_drops(id));
         */
        /*
        CREATE TABLE IF NOT EXISTS tns_player_race_data (
        id BIGINT AUTO_INCREMENT NOT NULL,
        player_name VARCHAR(255) NOT NULL,
        race_name VARCHAR(255) NOT NULL,
        points INT NOT NULL,
        cumulate_points INT NOT NULL,
        PRIMARY KEY (player_name),
        INDEX index_race_name (race_name)
         */


        sql.deleteCharAt(sql.length() - 1);
        sql.append(");");

        SQLService.sql(sql.toString());
        try (Connection connection = HikariConnectionPool.getConnection();
             Statement statement = connection.createStatement()) {
            int i = statement.executeUpdate(sql.toString());
            if (i == 1) {
                System.out.println("表 " + tableName + " 创建成功.");
            }
            return createStatus.CREATE_SUCCESS;
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("创建表失败: " + e.getMessage());
            return createStatus.CREATE_FAILURE;
        }

    }


}
