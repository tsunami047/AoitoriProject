package io.aoitori043.syncdistribute.rmi.data.access;

import com.google.gson.GsonBuilder;
import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @Author: natsumi
 * @CreateTime: 2024-10-29  19:40
 * @Description: ?
 */
@Getter
public class ObjectAccess extends DataAccess{

    Class clazz;
    Consumer<GsonBuilder> gsonBuilderConsumer;

    @Builder
    public ObjectAccess(String varName, Class clazz,Consumer<GsonBuilder> gsonBuilderConsumer) {
        super(varName);
        this.clazz = clazz;
        this.gsonBuilderConsumer = gsonBuilderConsumer;

    }

    @Override
    public Object get(PersistentDataAccess persistentDataAccess, String originValue) {
        try {
            return PersistentDataAccess.gson.fromJson(originValue, clazz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void register() {
        super.register();
        GsonBuilder gsonBuilder = new GsonBuilder();
        for (Map.Entry<String, DataAccess> entry : PersistentDataAccess.registerDataAccess.entrySet()) {
            DataAccess objectAccess = entry.getValue();
            if (objectAccess instanceof ObjectAccess) {
                Consumer<GsonBuilder> builder = ((ObjectAccess) objectAccess).getGsonBuilderConsumer();
                if (builder == null) continue;
                builder.accept(gsonBuilder);
            }
        }
        PersistentDataAccess.gson = gsonBuilder.create();
    }
}
