package io.aoitori043.aoitoriproject.database.orm.fetch;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-10  20:34
 * @Description: ?
 */
public class TableFetch {

    public SQLClient sqlClient;

    public TableFetch(SQLClient sqlClient) {
        this.sqlClient = sqlClient;
    }



    public <T> FetchBuilder<T> buildFetcher(Class<T> clazz) {
        SQLClient.EntityAttributes entityAttribute = sqlClient.getEntityAttribute(clazz);
        return new FetchBuilder<T>();
    }


}
