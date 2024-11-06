package io.aoitori043.syncdistribute.rmi;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-06  00:18
 * @Description: ?
 */
public class NodeLeaderService {

    private ConcurrentHashMap<String,Runnable> uniqueRunnableMap = new ConcurrentHashMap<>();

    public NodeLeaderService() {
    }

    public void register(String channelName, Runnable runnable){
        uniqueRunnableMap.put(channelName,runnable);
    }

    public void elect(){

    }

    public void execute(){
        
    }
}
