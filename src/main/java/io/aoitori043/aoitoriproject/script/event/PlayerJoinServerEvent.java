package io.aoitori043.aoitoriproject.script.event;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-25  00:52
 * @Description: ?
 */
public class PlayerJoinServerEvent extends AoitoriEvent {

    public static final String PLAYER_JOIN_SERVER = "playerJoinServer";

    public PlayerJoinServerEvent(PlayerDataAccessor playerDataAccessor, List<AbstractCommand> functionBody, ConcurrentHashMap<String, Object> map) {
        super(playerDataAccessor, PLAYER_JOIN_SERVER, functionBody, map);
    }

    @Override
    public EventResult invoke() {
        super.execute();
        return new EventResult(super.isCancel);
    }

    public static EventResult call(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> map) {
        try {
            Collection<EventWrapper> events = playerDataAccessor.getEvent(PLAYER_JOIN_SERVER);
            for (EventWrapper event : events) {
                try {
                    if (event != null) {
                        PlayerJoinServerEvent vue = new PlayerJoinServerEvent(playerDataAccessor, event.getCommands(), map);
                        vue.invoke();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return new EventResult(true);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
