package io.aoitori043.aoitoriproject.database.orm.sql;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    public <T> boolean updateNotCache(T entity, T whereEntity) {
        try (Connection connection = HikariConnectionPool.getConnection()) {
            Class<?> clazz = entity.getClass();
            StringBuilder sql = new StringBuilder("UPDATE ").append(sqlClient.nameStructure.getTableName(clazz)).append(" SET ");
            FieldAccess fieldAccess = FieldAccess.get(clazz);
            String[] fieldNames = fieldAccess.getFieldNames();
            SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(clazz);
            for (String fieldName : entityAttribute.getUpdateFields()) {
                Object o = fieldAccess.get(entity, fieldName);
                if (o != null) {
                    sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ?,");
                }
            }
            sql.deleteCharAt(sql.length() - 1); // 移除最后一个逗号
            sql.append(" WHERE ");

            Object idValue = fieldAccess.get(whereEntity, "id");
            if (idValue != null) {
                sql.append("id = ?");
            } else {
                // 如果 id 不存在，则根据其他字段进行搜索
                for (String fieldName : fieldNames) {
                    if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(fieldName)){
                        continue;
                    }
                    Object o = fieldAccess.get(whereEntity, fieldName);
                    if (o != null) {
                        sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ? AND ");
                    }
                }
                if (sql.toString().endsWith("AND ")) {
                    sql.setLength(sql.length() - 5);
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                for (String fieldName : entityAttribute.getUpdateFields()) {
                    Object o = fieldAccess.get(entity, fieldName);
                    if (o != null) {
                        statement.setObject(paramIndex++, o);
                    }
                }
                if (idValue != null) {
                    statement.setObject(paramIndex++, idValue);
                } else {
                    for (String fieldName : fieldNames) {
                        if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(fieldName)){
                            continue;
                        }
                        Object o = fieldAccess.get(whereEntity, fieldName);
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
