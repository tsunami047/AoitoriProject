package io.aoitori043.syncdistribute.rmi;

import io.aoitori043.aoitoriproject.AoitoriProject;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-11-06  00:18
 * @Description: ?
 */
@Setter
@Getter
public class NodeLeaderService {

    private ConcurrentHashMap<String,Runnable> uniqueRunnableMap = new ConcurrentHashMap<>();
    private int leaderPort;

    public NodeLeaderService() {
        this.elect();
    }

    public void register(String channelName, Runnable runnable){
        uniqueRunnableMap.put(channelName,runnable);
    }

    public boolean isLeader(){
        return AoitoriProject.port == leaderPort;
    }

    public void elect(){
        try {
            RMIClient.leaderElectionService.participateInElection(AoitoriProject.port);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void execute(){
        for (Runnable value : uniqueRunnableMap.values()) {
            try{
                value.run();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
