package io.aoitori043.aoitoriproject.script.executor.command.nested;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-07  20:57
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ElseCommand extends NestedCommand {

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        return nestedCommandWrapper;
    }

    public ElseCommand(int depth, String[] parameters) {
        super(depth, "else", parameters);
    }

    @Override
    public void compile() {

    }
}
