package io.aoitori043.aoitoriproject.script.executor;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  15:17
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomCommand extends AbstractCommand {

    public Expression[] funcArgs;


    public void compile() {
        funcArgs = new Expression[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            funcArgs[i] = new Expression(parameters[i]);
        }
    }

    public CustomCommand(int depth,String funcName, String[] parameters) {
        super(depth, funcName, parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        PlayerDataAccessor.Function function = playerDataAccessor.getFunctions().get(super.type);
        if (function == null) {
            return null;
        }
        for (int i = 0; i < funcArgs.length; i++) {
            variables.put(function.parameters.get(i),funcArgs[i].interpret(playerDataAccessor,variables));
        }
        FunctionExecutor.syncExecuteNotDelay(playerDataAccessor,function.commands,performReturnContent,variables);
        return null;
    }
}
