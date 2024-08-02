package io.aoitori043.aoitoriproject.script.executor.command.sign;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:39
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ReturnCommand extends AbstractCommand {

    Expression result;

    public boolean hasReturnValue(){
        return result!=null;
    }

    public ReturnCommand(int depth, String[] parameters) {
        super(depth, "return", parameters);
    }

    public void compile() {
        if(parameters.length > 0){
            result = new Expression(parameters[0]);
        }
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setReturn(true);
        if(result!=null){
            performReturnContent.setResult(this.result.interpret(playerDataAccessor, variables));
        }
        return null;
    }
}
