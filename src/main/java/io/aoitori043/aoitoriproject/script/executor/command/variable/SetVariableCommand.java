package io.aoitori043.aoitoriproject.script.executor.command.variable;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:56
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class SetVariableCommand extends AbstractCommand {

    Expression varName;
    Expression value;

    public SetVariableCommand(int depth, String[] parameters) {
        super(depth, "set_variable",parameters);
    }

    public void compile() {
        varName = new Expression(parameters[0]);
        value = new Expression(parameters[1]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        String varNameText = varName.interpret(playerDataAccessor, variables).toString();
        if (playerDataAccessor.hasVariable(varNameText)) {
            playerDataAccessor.setValue(varNameText,value.interpret(playerDataAccessor,variables));
        }else{
            variables.put(varNameText, value.interpret(playerDataAccessor,variables));
        }
        return null;
    }
}