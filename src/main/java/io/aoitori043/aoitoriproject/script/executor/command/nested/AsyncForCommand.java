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
 * @CreateTime: 2024-07-07  22:31
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AsyncForCommand extends NestedCommand{
    public List<AbstractCommand> nestedCommands;
    public Expression expression;

    public AsyncForCommand(int depth, String[] parameters) {
        super(depth, "async-for", parameters);
    }

    @Override
    public AbstractCommand.NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, AbstractCommand.PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        AbstractCommand.NestedCommandWrapper nestedCommandWrapper = new AbstractCommand.NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        nestedCommandWrapper.isLoop = true;
        nestedCommandWrapper.isAsync = true;
        nestedCommandWrapper.expression = expression;
        return nestedCommandWrapper;
    }

    @Override
    public void compile() {

    }
}
