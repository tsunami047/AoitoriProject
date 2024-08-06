package io.aoitori043.aoitoriproject.script.executor.command.value;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-03  16:20
 * @Description: ?
 */
public class ToBooleanCommand extends AbstractCommand {

    Expression value;

    public ToBooleanCommand(int depth, String[] parameters) {
        super(depth, "toBoolean", parameters);
    }

    public void compile() {
        value = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setResult(Boolean.parseBoolean(value.interpret(playerDataAccessor,variables).toString()));
        return null;
    }
}