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
 * @CreateTime: 2024-07-28  19:44
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class NewVariableCommand extends AbstractCommand {

    public enum FieldType{
        INT,
        DOUBLE,
        LONG,
        BOOLEAN,
        STRING
    }

    FieldType fieldType;
    Expression varName;
    Expression value;

    public NewVariableCommand(int depth, String[] parameters) {
        super(depth, "new_variable",parameters);
    }

    public void compile() {
        fieldType = FieldType.valueOf(parameters[0].toUpperCase());
        varName = new Expression(parameters[1]);
        value = new Expression(parameters[2]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        Object interpret = value.interpret(playerDataAccessor, variables);
        if(interpret == null){
            return null;
        }
        switch (fieldType) {
            case INT:{
                variables.put(varName.interpret(playerDataAccessor,variables).toString(), Integer.parseInt(interpret.toString()));
                break;
            }
            case DOUBLE:{
                variables.put(varName.interpret(playerDataAccessor,variables).toString(), Double.parseDouble(interpret.toString()));
                break;
            }
            case LONG:{
                variables.put(varName.interpret(playerDataAccessor,variables).toString(), Long.parseLong(interpret.toString()));
                break;
            }
            case STRING:{
                variables.put(varName.interpret(playerDataAccessor,variables).toString(), interpret);
                break;
            }
            case BOOLEAN:{
                variables.put(varName.interpret(playerDataAccessor,variables).toString(), Boolean.parseBoolean(interpret.toString()));
                break;
            }
        }
        return null;
    }
}