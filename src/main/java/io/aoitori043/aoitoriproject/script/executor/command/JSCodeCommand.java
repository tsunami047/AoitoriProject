package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import io.aoitori043.aoitoriproject.script.parameter.JavaScriptExpression;
import org.jetbrains.annotations.NotNull;

import javax.script.Invocable;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.impl.ConfigHandler.engine;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-31  15:38
 * @Description: ?
 */
public class JSCodeCommand extends AbstractCommand {

    public Expression methodName;
    public JavaScriptExpression compiledParameters;

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        try {
            Invocable invocable = (Invocable) engine;
            String interpret = compiledParameters.interpret(playerDataAccessor, variables);
            engine.eval(interpret);
            Object result = invocable.invokeFunction(methodName.interpret(playerDataAccessor,variables).toString());
            if(result!=null){
                performReturnContent.setResult(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSCodeCommand(int depth, String[] parameters) {
        super(depth, "js-code", parameters);
    }

    public void compile() {
        methodName = new Expression(parameters[0]);
        compiledParameters = new JavaScriptExpression(parameters[1]);

    }
}
