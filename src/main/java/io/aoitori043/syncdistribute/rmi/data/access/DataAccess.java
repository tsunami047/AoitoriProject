package io.aoitori043.syncdistribute.rmi.data.access;

import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import lombok.Getter;

@Getter
public abstract class DataAccess {

    public String varName;

    public DataAccess(String varName) {
        this.varName = varName;
    }

    public abstract String get(PersistentDataAccess persistentDataAccess,String originValue);

    public void register(){
        PersistentDataAccess.registerDataAccess.put(varName,this);
    }
}
