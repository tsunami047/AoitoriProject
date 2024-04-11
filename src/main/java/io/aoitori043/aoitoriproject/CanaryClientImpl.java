package io.aoitori043.aoitoriproject;

import io.aoitori043.aoitoriproject.database.orm.SQLClient;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-02  16:53
 * @Description: ?
 */
public class CanaryClientImpl {

    public static SQLClient sqlClient = new SQLClient();

    public static void init(){
        sqlClient.build();
    }
}
