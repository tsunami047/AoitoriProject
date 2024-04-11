package io.aoitori043.aoitoriproject.database.orm;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-10  23:34
 * @Description: ?
 */
public class ValueCheck {

    public static void invalidate(Object object){
        if(object == null){
            throw new NullPointerException("key value is null");
        }
    }
}
