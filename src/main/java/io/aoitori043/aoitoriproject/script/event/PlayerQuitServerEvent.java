package io.aoitori043.aoitoriproject.script.event;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.script.AoitoriPlayerJoinEvent;
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
public class PlayerQuitServerEvent extends AoitoriEvent{

    public static final String PLAYER_LEAVE_SERVER = "playerQuitServer";

    public PlayerQuitServerEvent(PlayerDataAccessor playerDataAccessor, List<AbstractCommand> functionBody, ConcurrentHashMap<String, Object> map) {
        super(playerDataAccessor, "playerQuitServer", functionBody, map);
    }

    @Override
    public AoitoriEvent.EventResult invoke() {
        super.execute();
        return new AoitoriEvent.EventResult(super.isCancel);
    }

    public static EventResult call(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> map) {
        try {
            AoitoriProject.kilimScheduler.forkJoinExecute(()->{
                Collection<EventWrapper> events = playerDataAccessor.getEvent(PLAYER_LEAVE_SERVER);
                for (EventWrapper event : events) {
                    try {
                        if (event != null) {
                            PlayerQuitServerEvent vue = new PlayerQuitServerEvent(playerDataAccessor, event.getCommands(), map);
                            vue.invoke();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return new EventResult(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
