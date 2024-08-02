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
 * @CreateTime: 2024-07-07  20:56
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IfCommand extends NestedCommand {

    public ElseCommand elseCommand;
    public IfCommand elseIfCommand;
    public Expression expression;
    public String condition;

    public IfCommand(int depth, String[] parameters) {
        super(depth, "if", parameters);
        this.condition = parameters[0];
    }

    public void compile() {
        expression = new Expression(condition);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        Object execute = expression.execute(playerDataAccessor, variables);
        if (Boolean.parseBoolean(execute.toString())) {
            NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
            nestedCommandWrapper.commands = nestedCommands;
            return nestedCommandWrapper;
        } else {
            if (elseIfCommand != null) {
                return elseIfCommand.execute(playerDataAccessor, performReturnContent, variables);
            }else if (elseCommand != null) {
                return elseCommand.execute(playerDataAccessor, performReturnContent, variables);
            }
        }
        return null;
    }

}
