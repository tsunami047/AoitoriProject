package io.aoitori043.aoitoriproject.database.orm.fetch;

import java.util.List;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-10  20:38
 * @Description: ?
 */
public class FetchBuilder<T> {

    public FetchBuilder<T> checkDate(String fieldName,String date){
        return this;
    }

    public List<T> start(){

        return java.util.Collections.emptyList();
    }
}
