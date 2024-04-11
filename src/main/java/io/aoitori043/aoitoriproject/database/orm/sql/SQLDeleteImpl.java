package io.aoitori043.aoitoriproject.database.orm.sql;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

import static io.aoitori043.aoitoriproject.database.orm.ReflectASMUtil.createInstance;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:27
 * @Description: ?
 */
public class SQLDeleteImpl {

    public SQLClient sqlClient;

    public SQLDeleteImpl(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public <T> boolean deleteNotCache(Class<T> clazz, T whereEntity) {
        try (Connection connection = HikariConnectionPool.getConnection()) {
            StringBuilder sql = new StringBuilder("DELETE FROM ").append(sqlClient.nameStructure.getTableName(clazz)).append(" WHERE ");

            Object idValue = FieldAccess.get(clazz).get(whereEntity, "id");
            if (idValue != null) {
                sql.append("id = ?");
            } else {
                FieldAccess fieldAccess = FieldAccess.get(clazz);
                String[] fieldNames = fieldAccess.getFieldNames();
                for (String fieldName : fieldNames) {
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
                if (idValue != null) {
                    statement.setObject(paramIndex++, idValue);
                } else {
                    FieldAccess fieldAccess = FieldAccess.get(clazz);
                    String[] fieldNames = fieldAccess.getFieldNames();
                    for (String fieldName : fieldNames) {
                        Object o = fieldAccess.get(whereEntity, fieldName);
                        if (o != null) {
                            statement.setObject(paramIndex++, o);
                        }
                    }
                }

                int rowsDeleted = statement.executeUpdate();
                return rowsDeleted > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> boolean deleteNotCache(Class<T> clazz, Consumer<T> consumer) {
        T instance = createInstance(clazz);
        consumer.accept(instance);
        return deleteNotCache(clazz, instance);
    }

}
