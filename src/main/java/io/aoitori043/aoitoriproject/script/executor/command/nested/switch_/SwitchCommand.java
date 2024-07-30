package io.aoitori043.aoitoriproject.script.executor.command.nested.switch_;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-06-28  13:23
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
public class SwitchCommand extends AbstractCommand {

    public Expression expression;
    public HashMap<String, CaseCommand> caseMap;
    public DefaultCommand defaultCommand;

    public SwitchCommand(int depth, String[] parameters) {
        super(depth, "switch", parameters);
        this.caseMap = new HashMap<>();
    }

    public void compile() {
        expression = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        String interpret = expression.interpret(playerDataAccessor, variables).toString();
        CaseCommand caseCommand = this.caseMap.get(interpret);
        if(caseCommand == null){
            if (defaultCommand!=null) {
                return defaultCommand.execute(playerDataAccessor, performReturnContent, variables);
            }
        }else{
            return caseCommand.execute(playerDataAccessor, performReturnContent, variables);
        }
        return null;
    }
}
