package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import javax.script.Invocable;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.impl.ConfigHandler.engine;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:33
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JSMethodCommand extends AbstractCommand {

    public Expression methodName;
    public Expression[] compiledParameters;

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        try {
            Invocable invocable = (Invocable) engine;
            Object[] interpretParameters = new Object[compiledParameters.length];
            for (int i = 0; i < compiledParameters.length; i++) {
                interpretParameters[i]=compiledParameters[i].execute(playerDataAccessor, variables);
            }
            Object result = invocable.invokeFunction(methodName.interpret(playerDataAccessor,variables).toString(), interpretParameters);
            if(result!=null){
                performReturnContent.setResult(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSMethodCommand(int depth, String[] parameters) {
        super(depth, "js", parameters);
    }

    public void compile() {
        methodName = new Expression(parameters[0]);
        compiledParameters = new Expression[parameters.length - 1];
        for (int i = 1; i < parameters.length; i++) {
            compiledParameters[i - 1] = new Expression(parameters[i]);
        }
    }
}
