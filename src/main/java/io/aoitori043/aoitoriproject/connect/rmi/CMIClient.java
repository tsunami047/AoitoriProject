package io.aoitori043.aoitoriproject.connect.rmi;

import io.aoitori043.aoitoriproject.connect.rmi.inter.OnlineService;

import java.rmi.Naming;

public class CMIClient {

    public static OnlineService onlineService;

    public static boolean isOnline(String playerName) {
        try {
            return onlineService.isOnline(playerName);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static void start(){
        try {
            onlineService = (OnlineService) Naming.lookup("rmi://localhost:1900/online");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
