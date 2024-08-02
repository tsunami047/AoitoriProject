package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-01  06:23
 * @Description: ?
 */
public class SetResultCommand extends AbstractCommand {

    Expression result;

    public SetResultCommand(int depth, String[] parameters) {
        super(depth, "setResult", parameters);
    }

    public void compile() {
        result = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        variables.put("result", result.interpret(playerDataAccessor,variables));
        return null;
    }
}