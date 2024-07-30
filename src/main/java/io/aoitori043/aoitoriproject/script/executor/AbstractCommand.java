package io.aoitori043.aoitoriproject.script.executor;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public abstract class AbstractCommand {

    public int depth;
    public String type;
    public String[] parameters;

    public AbstractCommand(int depth, String type, String[] parameters) {
        this.depth = depth;
        this.type = type;
        this.parameters = parameters;
    }


    public abstract NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables);

    public abstract void compile();

    @Data
    public static class NestedCommandWrapper{
        public Object returnValue;
        public List<AbstractCommand> commands;
        public boolean isAsync;
        public boolean isLoop;
        public Expression expression;
    }

    @Data
    public static class PerformReturnContent {
        public Object result;
        public boolean isReturn;
        public boolean isCancel;
        public boolean isBreak;
        public int gotoIndex = -1;
    }
}
