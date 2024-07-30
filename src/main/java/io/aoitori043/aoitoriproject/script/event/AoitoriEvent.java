package io.aoitori043.aoitoriproject.script.event;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.FunctionExecutor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2023-12-25  01:01
 * @Description: ?
 */
@Getter
@NoArgsConstructor
public abstract class AoitoriEvent {
    public PlayerDataAccessor playerDataAccessor;
    public String eventName;
    public List<AbstractCommand> functionBody;
    public boolean isCancel = false;
    @Setter
    public ConcurrentHashMap<String, Object> varRuntime;

    public AoitoriEvent(PlayerDataAccessor playerDataAccessor, String eventName, List<AbstractCommand> functionBody, ConcurrentHashMap<String, Object> map)  {
        this.playerDataAccessor = playerDataAccessor;
        this.eventName = eventName;
        this.functionBody = functionBody;
        this.varRuntime = map;
    }


    public void putVariable(String varName, String value) {
        this.varRuntime.put(varName, value);
    }


    protected void execute() {
        AbstractCommand.PerformReturnContent performReturnContent = new AbstractCommand.PerformReturnContent();
        FunctionExecutor.syncExecuteNotDelay(playerDataAccessor,functionBody,performReturnContent,this.varRuntime);
        isCancel = performReturnContent.isCancel;
    }

    public abstract EventResult invoke();

    @Data
    public static class EventResult {
        public boolean isCancel = false;
        public EventResult(boolean isCancel) {
            this.isCancel = isCancel;
        }
    }
}
