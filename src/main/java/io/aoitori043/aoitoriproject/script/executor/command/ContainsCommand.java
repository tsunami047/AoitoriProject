package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-31  23:21
 * @Description: ?
 */
public class ContainsCommand extends AbstractCommand {

    Expression arg1;
    Expression arg2;

    public ContainsCommand(int depth, String[] parameters) {
        super(depth, "contains", parameters);
    }

    public void compile() {
        arg1 = new Expression(parameters[0]);
        arg2 = new Expression(parameters[1]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setResult(
        arg1.interpret(playerDataAccessor, variables).toString().contains(arg2.interpret(playerDataAccessor, variables).toString()));
        return null;
    }
}