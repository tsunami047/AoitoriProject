package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-01  16:22
 * @Description: ?
 */
public class NotCalculateCommand extends AbstractCommand {

    Expression expression;

    public NotCalculateCommand(int depth, String[] parameters) {
        super(depth, "notCalculate", parameters);
    }

    public void compile() {
        expression = new Expression(parameters[0], Expression.CompiledType.NOT_CALCULATE);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setResult(expression.interpret(playerDataAccessor,variables).toString());
        return null;
    }
}