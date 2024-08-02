package io.aoitori043.aoitoriproject.script.executor.command.nested.switch_;


import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.command.nested.NestedCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-06-28  13:24
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CaseCommand extends NestedCommand {


    public String condition;
    public List<AbstractCommand> nestedCommands;
    public Expression result;

    public CaseCommand(int depth, String[] parameters) {
        super(depth, "case", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        if(result!=null){
            performReturnContent.setResult(result.interpret(playerDataAccessor,variables));
            performReturnContent.setReturn(true);
            return null;
        }
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.commands = nestedCommands;
        return nestedCommandWrapper;
    }

    @Override
    public void compile() {
        String parameter = parameters[0];
        if(parameter.contains("->")){
            this.condition = parameter.substring(0,parameter.indexOf("->")).trim();
            String substring = parameter.substring(parameter.indexOf("->")+2).trim();
            this.result = new Expression(substring);
            return;
        }
        if(parameter.charAt(parameter.length()-1) == ':'){
            this.condition = parameter.substring(0,parameter.length()-1);
        }else{
            this.condition = parameter;
        }
    }
}
