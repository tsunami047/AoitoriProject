package io.aoitori043.aoitoriproject.database.point;

import io.aoitori043.aoitoriproject.script.TemporaryDataManager;
import io.aoitori043.syncdistribute.rmi.data.PersistentDataAccess;
import io.aoitori043.syncdistribute.rmi.data.access.DataAccess;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-22  15:32
 * @Description: ?
 */
public class PointManager extends PointVisitor{

    public void bind(DataAccess d) {
        PersistentDataAccess.registerDataAccess.put(d.getVarName(),d);

    }

    public void unbind(DataAccess d) {
        PersistentDataAccess.registerDataAccess.remove(d.getVarName());
    }

    public <T> void set(String playerName, String dataName, T value) {
        PersistentDataAccess persistentDataAccess = TemporaryDataManager.getPlayerDataAccessor(playerName).getPersistentDataAccess();
        persistentDataAccess.set(dataName,value);
    }

}
