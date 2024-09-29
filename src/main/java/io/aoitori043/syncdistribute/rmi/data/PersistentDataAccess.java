package io.aoitori043.syncdistribute.rmi.data;


import io.aoitori043.syncdistribute.rmi.data.access.DataAccess;
import io.aoitori043.syncdistribute.rmi.RMIClient;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PersistentDataAccess {

    public static HashMap<String, DataAccess> registerDataAccess = new HashMap<>();

    public String playerName;
    public ConcurrentHashMap<String, String> persistentVariables;

    public PersistentDataAccess(String playerName) {
        try {
            this.playerName = playerName;
            Map<String, String> currentData = RMIClient.playerDataService.getCurrentData(playerName);
            persistentVariables = new ConcurrentHashMap<>(currentData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String get(String varName){
        DataAccess dataAccess = registerDataAccess.get(varName);
        String currentValue = persistentVariables.get(varName);
        if(dataAccess!=null){
            return dataAccess.get(this, currentValue);
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
            RMIClient.playerDataService.set(playerName, varName, null);
            persistentVariables.remove(varName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void set(String varName, Object o){
        try {
            String value = String.valueOf(o);
            RMIClient.playerDataService.set(playerName, varName, value);
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
            RMIClient.playerDataService.set(playerName, varName, value);
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
