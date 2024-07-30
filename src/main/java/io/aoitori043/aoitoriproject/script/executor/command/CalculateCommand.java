package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.FunctionExecutor;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  22:24
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CalculateCommand extends AbstractCommand {

    public Expression expression;


    public void compile() {
        expression = new Expression(parameters[0]);
    }

    public CalculateCommand(int depth, String[] parameters) {
        super(depth, "calculate", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        performReturnContent.setResult(expression.execute(playerDataAccessor,variables));
        return nestedCommandWrapper;
    }
}
