package io.aoitori043.aoitoriproject.script.executor.command.nested;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-07  21:04
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AsyncCommand extends NestedCommand {


    public AsyncCommand(int depth, String[] parameters) {
        super(depth, "async", parameters);
    }

    public void compile() {
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        nestedCommandWrapper.isAsync = true;
        return nestedCommandWrapper;
    }

}
