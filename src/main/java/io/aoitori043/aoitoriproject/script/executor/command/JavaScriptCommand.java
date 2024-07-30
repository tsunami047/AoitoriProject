package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:33
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JavaScriptCommand extends AbstractCommand {

    public Expression methodName;

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        return null;
    }

    public JavaScriptCommand(int depth, String[] parameters) {
        super(depth, "js", parameters);
    }

    public void compile() {
        methodName = new Expression(parameters[0]);
    }
}
