package io.aoitori043.aoitoriproject.database.orm.sql;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.utils.ReflectASMUtil;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.aoitori043.aoitoriproject.utils.ReflectASMUtil.createInstance;


/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:26
 * @Description: ?
 */
public class SQLQueryImpl {

    public SQLClient sqlClient;

    public SQLQueryImpl(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }



    public <T> List<T> directFindByIds(T instance) {
        Class<?> clazz = instance.getClass();
        List<T> resultList = new ArrayList<>();
        try (Connection connection = HikariConnectionPool.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM ").append(sqlClient.nameStructure.getTableName(clazz)).append(" WHERE ");
            // 获取 id 字段的值
            Object idValue = FieldAccess.get(clazz).get(instance, "id");
            SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(instance.getClass());
            FieldAccess fieldAccess = entityAttribute.getFieldAccess();
            if (idValue != null) {
                sql.append("id = ?");
            } else {
                // 如果 id 不存在，则根据其他字段进行搜索
                for (String fieldName : entityAttribute.getDeclaredFieldNames()) {
                    Object o = fieldAccess.get(instance, fieldName);
                    if (o != null) {
                        sql.append(sqlClient.nameStructure.getFieldName(clazz, fieldName)).append(" = ? AND ");
                    }
                }
                if (sql.toString().endsWith("AND ")) {
                    sql = new StringBuilder(sql.substring(0, sql.length() - 5));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (idValue != null) {
                    statement.setObject(paramIndex++, idValue);
                } else {
                    for (String fieldName : entityAttribute.getDeclaredFieldNames()) {
                        Object o = fieldAccess.get(instance, fieldName);
                        if (o != null) {
                            statement.setObject(paramIndex++, o);
                        }
                    }
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        T resultInstance = (T) ReflectASMUtil.createInstance(clazz);
                        for (String fieldName : entityAttribute.getDeclaredFieldNames()) {
                            fieldAccess.set(resultInstance, fieldName, resultSet.getObject(sqlClient.nameStructure.getFieldName(clazz, fieldName)));

                        }
                        resultList.add(resultInstance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }


    public <T> List<T> directFindByIds(Class<T> clazz, Consumer<T> consumer) {
        T instance = createInstance(clazz);
        consumer.accept(instance);
        return directFindByIds(instance);
    }
}
