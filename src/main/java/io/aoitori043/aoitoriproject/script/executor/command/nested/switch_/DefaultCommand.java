package io.aoitori043.aoitoriproject.script.executor.command.nested.switch_;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.command.nested.NestedCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-06-28  13:30
 * @Description: ?
 */
public class DefaultCommand extends NestedCommand {


    public List<AbstractCommand> nestedCommands;

    public DefaultCommand(int depth, String[] parameters) {
        super(depth, "default", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        return nestedCommandWrapper;
    }

    @Override
    public void compile() {

    }
}
