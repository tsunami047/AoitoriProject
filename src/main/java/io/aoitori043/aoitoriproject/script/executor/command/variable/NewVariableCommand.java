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
        switch (fieldType) {
            case INT:{
                variables.put(varName.toString(), Integer.parseInt(value.interpret(playerDataAccessor,variables).toString()));
                break;
            }
            case DOUBLE:{
                variables.put(varName.toString(), Double.parseDouble(value.interpret(playerDataAccessor,variables).toString()));
                break;
            }
            case LONG:{
                variables.put(varName.toString(), Long.parseLong(value.interpret(playerDataAccessor,variables).toString()));
                break;
            }
            case STRING:{
                variables.put(varName.toString(), value.interpret(playerDataAccessor,variables).toString());
                break;
            }
            case BOOLEAN:{
                variables.put(varName.toString(), Boolean.parseBoolean(value.interpret(playerDataAccessor,variables).toString()));
                break;
            }
        }
        return null;
    }
}