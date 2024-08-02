package io.aoitori043.aoitoriproject.script.event;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2023-12-25  01:05
 * @Description: ?
 */
public class VariableUpdateEvent extends AoitoriEvent {

    public String varName;
    public Object oldValue;
    public Object newValue;

    public VariableUpdateEvent(PlayerDataAccessor playerDataAccessor, List<AbstractCommand> functionBody, ConcurrentHashMap<String, Object> map, String varName, Object oldValue, Object newValue) {
        super(playerDataAccessor, "variableUpdate", functionBody, map);
        this.varName = varName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Setter
    @Getter
    public static class VariableUpdateEventResult extends EventResult{
        Object newVariable;
        public VariableUpdateEventResult(boolean isCancel) {
            super(isCancel);
        }
    }

    public static EventResult call(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> map, String variableName, Object oldValue, Object newValue) {
        boolean cancelled = false;
        try {
            Collection<EventWrapper> events = playerDataAccessor.getEvent("variableUpdate$" + variableName);
            for (EventWrapper event : events) {
                try {
                    if (event != null) {
                        VariableUpdateEvent vue = new VariableUpdateEvent(playerDataAccessor, event.getCommands(), map, variableName, oldValue, newValue);
                        EventResult invoke = vue.invoke();
                        if(invoke.isCancel){
                            cancelled = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AoitoriProject.plugin.getLogger().info("无法执行临时变量更新函数：" + playerDataAccessor.getPlayer().getName() + " " + variableName + " " + oldValue + " " + newValue);
                    return new EventResult(true);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        VariableUpdateEventResult variableUpdateEventResult = new VariableUpdateEventResult(cancelled);
        Object o = map.get("result");
        variableUpdateEventResult.setNewVariable(o);
        return variableUpdateEventResult;
    }

    @Override
    public EventResult invoke() {
        this.varRuntime.put("new", newValue);
        this.varRuntime.put("old", oldValue);
        super.execute();
        return new EventResult(super.isCancel);
    }
}
