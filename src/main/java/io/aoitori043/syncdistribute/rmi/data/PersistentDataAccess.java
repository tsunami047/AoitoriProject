package io.aoitori043.syncdistribute.rmi.data;


import com.google.gson.Gson;
import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.syncdistribute.rmi.data.access.DataAccess;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import lombok.Data;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PersistentDataAccess {

    public static HashMap<String, DataAccess> registerDataAccess = new HashMap<>();
    public static Gson gson = new Gson();

    public String playerName;
    public ConcurrentHashMap<String, String> persistentVariables;


    public PersistentDataAccess(String playerName) {
        initPlayerData(playerName,true);
    }

    private void initPlayerData(String playerName,boolean recursion) {
        try {
            this.playerName = playerName;
            fetchData(playerName);
        }catch (java.rmi.ConnectException e){
            RMIClient.start();
            if (recursion) {
                initPlayerData(playerName,false);
            }else {
                AoitoriProject.plugin.getLogger().warning("Unable to connect to the BC server！");
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void fetchData(String playerName) throws RemoteException {
        Map<String, String> currentData = AoitoriProject.playerDataService.getCurrentData(playerName);
        persistentVariables = new ConcurrentHashMap<>(currentData);
    }

    public <T> T getAsObject(String varName){
        return (T)this.get(varName);
    }

    public String get(String varName){
        DataAccess dataAccess = registerDataAccess.get(varName);
        String currentValue = persistentVariables.get(varName);
        if(dataAccess!=null){
            return (String) dataAccess.get(this, currentValue);
        }
        return currentValue;
    }

    public long getAsLong(String varName){
        String original = this.get(varName);
        return original == null ? 0 : Long.parseLong(original);
    }

    public int getAsInt(String varName){
        String original = this.get(varName);
        return original == null ? 0 : Integer.parseInt(original);
    }

    public boolean getAsBoolean(String varName){
        String original = this.get(varName);
        return Boolean.parseBoolean(original);
    }

    public float getAsFloat(String varName){
        String original = this.get(varName);
        return original == null ? 0F : Float.parseFloat(original);
    }

    public double getAsDouble(String varName){
        String original = this.get(varName);
        return original == null ? 0D : Double.parseDouble(original);
    }

    public void del(String varName){
        try {
            AoitoriProject.playerDataService.set(playerName, varName, null);
            persistentVariables.remove(varName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void set(String varName, Object o){
        try {
            String value = String.valueOf(o);
            AoitoriProject.playerDataService.set(playerName, varName, value);
            if (o == null){
                persistentVariables.remove(varName);
            }else {
                persistentVariables.put(varName, value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void set(String varName, String value){
        try {
            AoitoriProject.playerDataService.set(playerName, varName, value);
            if (value == null){
                persistentVariables.remove(varName);
            }else {
                persistentVariables.put(varName, value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
