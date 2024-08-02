package io.aoitori043.aoitoriproject.script.executor.command.nested;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-07  21:01
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ForCommand extends NestedCommand {

    public List<AbstractCommand> nestedCommands;
    public Expression expression;

    public ForCommand(int depth,String[] parameters) {
        super(depth, "for", parameters);
        nestedCommands = new ArrayList<>();

    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        nestedCommandWrapper.isLoop = true;
        nestedCommandWrapper.expression = expression;
        return nestedCommandWrapper;
    }

    @Override
    public void compile() {
        this.expression = new Expression(parameters[0]);
    }
}
