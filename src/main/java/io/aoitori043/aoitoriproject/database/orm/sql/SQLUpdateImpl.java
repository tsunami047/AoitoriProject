package io.aoitori043.aoitoriproject.database.orm.sql;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.aoitori043.aoitoriproject.utils.ReflectASMUtil.createInstance;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:27
 * @Description: ?
 */
public class SQLUpdateImpl {

    public SQLClient sqlClient;

    public SQLUpdateImpl(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public <T> boolean absoluteUpdate(T entity, T whereEntity) {
        try (Connection connection = HikariConnectionPool.getConnection()) {
            Class<?> clazz = entity.getClass();
            StringBuilder sql = new StringBuilder("UPDATE ").append(sqlClient.nameStructure.getTableName(clazz)).append(" SET ");
            SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(clazz);
            FieldAccess fieldAccess = entityAttribute.getFieldAccess();

            List<String> updateNames = new ArrayList<>();
            for (int i = 0; i < entityAttribute.getUpdateFields().size(); i++) {
                String fieldName = entityAttribute.getUpdateFields().get(i);
//                Object o = fieldAccess.getData(entity, fieldName);
//                if(o != null){
//                    sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ?,");
//                }
                //空指针的时候也更新
                sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ?,");
                updateNames.add(fieldName);
            }

            sql.deleteCharAt(sql.length() - 1); // 移除最后一个逗号
            sql.append(" WHERE ");

            Object idValue = fieldAccess.get(whereEntity, "id");
            if (idValue != null) {
                sql.append("id = ?");
            } else {
                for (String queryField : entityAttribute.getQueryFields()) {
                    Object o = fieldAccess.get(whereEntity, queryField);
                    if (o != null) {
                        sql.append(sqlClient.nameStructure.getFieldName(clazz, queryField)).append(" = ? AND ");
                    }
                }
                //删除结尾的AND
                if (sql.toString().endsWith("AND ")) {
                    sql.setLength(sql.length() - 5);
                }
            }
            String sqlString = sql.toString();
            SQLService.sql(sqlString);
            //插入查询参数
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                //插入需要修改的字段值
                int paramIndex = 1;
                for (String updateName : updateNames) {
                    Object o = fieldAccess.get(entity, updateName);
                    statement.setObject(paramIndex++, o);
                }
                if (idValue != null) {
                    statement.setObject(paramIndex++, idValue);
                } else {
                    for (String queryField : entityAttribute.getQueryFields()) {
                        Object o = fieldAccess.get(whereEntity, queryField);
                        if (o != null) {
                            statement.setObject(paramIndex++, o);
                        }
                    }
                }

                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public <T> boolean updateNotCache(T entity, T whereEntity) {
        try (Connection connection = HikariConnectionPool.getConnection()) {
            Class<?> clazz = entity.getClass();
            StringBuilder sql = new StringBuilder("UPDATE ").append(sqlClient.nameStructure.getTableName(clazz)).append(" SET ");
            SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(clazz);
            FieldAccess fieldAccess = entityAttribute.getFieldAccess();
            String[] fieldNames = fieldAccess.getFieldNames();
            List<String> updateNames = new ArrayList<>();
            for (int i = 0; i < entityAttribute.getUpdateFields().size(); i++) {
                String fieldName = entityAttribute.getUpdateFields().get(i);
                Object o = fieldAccess.get(entity, fieldName);
                Object o1 = fieldAccess.get(whereEntity, fieldName);
                if(o != null && (o != o1)){
                    sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ?,");
                }
                updateNames.add(fieldName);
            }

            sql.deleteCharAt(sql.length() - 1); // 移除最后一个逗号
            sql.append(" WHERE ");

            Object idValue = fieldAccess.get(whereEntity, "id");
            if (idValue != null) {
                sql.append("id = ?");
            } else {
                for (String queryField : entityAttribute.getQueryFields()) {
                    Object o = fieldAccess.get(whereEntity, queryField);
                    if (o != null) {
                        sql.append(sqlClient.nameStructure.getFieldName(clazz, queryField)).append(" = ? AND ");
                    }
                }
                //删除结尾的AND
                if (sql.toString().endsWith("AND ")) {
                    sql.setLength(sql.length() - 5);
                }
            }
            //插入查询参数
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                //插入需要修改的字段值
                int paramIndex = 1;
                for (String updateName : updateNames) {
                    Object o = fieldAccess.get(entity, updateName);
                    statement.setObject(paramIndex++, o);
                }
                if (idValue != null) {
                    statement.setObject(paramIndex++, idValue);
                } else {
                    for (String queryField : entityAttribute.getQueryFields()) {
                        Object o = fieldAccess.get(whereEntity, queryField);
                        if (o != null) {
                            statement.setObject(paramIndex++, o);
                        }
                    }
                }

                int rowsUpdated = statement.executeUpdate();
                return rowsUpdated > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> boolean updateNotCache(T entity, Consumer<T> whereConsumer) {
        Class<?> clazz = entity.getClass();
        T instance = (T) createInstance(clazz);
        whereConsumer.accept(instance);
        return updateNotCache(entity, instance);
    }

}
