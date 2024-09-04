package io.aoitori043.aoitoriproject.script.executor.command.value;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import io.aoitori043.aoitoriproject.utils.ValueFormer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-03  16:20
 * @Description: ?
 */
public class ToStringCommand extends AbstractCommand {

    Expression arg1;

    public ToStringCommand(int depth, String[] parameters) {
        super(depth, "toString", parameters);
    }

    public void compile() {
        arg1 = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        Object interpret = arg1.interpret(playerDataAccessor, variables);
        if(interpret instanceof Double){
            performReturnContent.setResult(ValueFormer.formatDouble((double)interpret));
            return null;
        }
        performReturnContent.setResult(String.valueOf(interpret));
        return null;
    }
}