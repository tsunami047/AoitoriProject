package io.aoitori043.aoitoriproject.database.point;

import com.google.gson.Gson;
import lombok.Data;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-26  03:32
 * @Description: ?
 */
@Data
public abstract class ObjectDataAccess<T> implements DataAccess {

    DataType varType = DataType.OBJECT_DATA;
    public static Gson gson = new Gson();
    String varName;
    private final Class<T> type;

    public ObjectDataAccess(String varName, Class<T> type) {
        this.varName = varName;
        this.type = type;
    }

    public T deserialize(String string){
        return gson.fromJson(string,type);
    }

    public String serialize(T object) {
        return gson.toJson(object);
    }
}
