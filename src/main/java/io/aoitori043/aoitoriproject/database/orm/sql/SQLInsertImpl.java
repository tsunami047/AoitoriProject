package io.aoitori043.aoitoriproject.database.orm.sql;

import com.esotericsoftware.reflectasm.FieldAccess;
import io.aoitori043.aoitoriproject.database.mysql.HikariConnectionPool;
import io.aoitori043.aoitoriproject.database.orm.SQLClient;

import java.sql.*;
import java.util.function.Consumer;

import static io.aoitori043.aoitoriproject.database.orm.ReflectASMUtil.createInstance;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  17:26
 * @Description: ?
 */
public class SQLInsertImpl {

    public SQLClient sqlClient;

    public SQLInsertImpl(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }

    public <T> int directInsertObject(T object) {
        try (Connection connection = HikariConnectionPool.getConnection()) {
            Class<?> clazz = object.getClass();
            StringBuilder sql = new StringBuilder("INSERT INTO ").append(sqlClient.nameStructure.getTableName(clazz)).append(" (");
            // 获取类的所有字段
            FieldAccess fieldAccess = FieldAccess.get(clazz);
            String[] fieldNames = fieldAccess.getFieldNames();
            SQLClient.EntityAttributes entityAttribute = this.sqlClient.getEntityAttribute(clazz);
            for (String fieldName : fieldNames) {
                if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(fieldName)){
                    continue;
                }
                sql.append(sqlClient.nameStructure.getFieldName(clazz,fieldName)).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(") VALUES (");
            for (int i = 0; i < fieldNames.length-entityAttribute.getEmbeddedMapFieldProperties().size(); i++) {
                sql.append("?,");
            }
            //INSERT INTO ad_drops (id,player_name,drop_id,insert_times) VALUES (?,?,?,?)
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
            try (PreparedStatement statement = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
                int paramIndex = 1;
//                statement.setObject(paramIndex++, null);
                for (String fieldName : fieldNames) {
                    if(entityAttribute.getEmbeddedMapFieldProperties().containsKey(fieldName)){
                        continue;
                    }
                    Object o = fieldAccess.get(object, fieldName);
                    statement.setObject(paramIndex++, o);

                }
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -2;
    }


    public <T> boolean insertNotCache(Class<T> clazz, Consumer<T> consumer) {
        T instance = createInstance(clazz);
        consumer.accept(instance);
        return directInsertObject(instance)>=0;
    }


}
