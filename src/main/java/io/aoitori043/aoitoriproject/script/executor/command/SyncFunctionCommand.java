package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.FunctionExecutor;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-30  03:53
 * @Description: ?
 */
public class SyncFunctionCommand extends AbstractCommand {

    public Expression functionName;
    public Expression[] expressionParameters;


    public void compile() {
        functionName = new Expression(parameters[0]);
        expressionParameters = new Expression[parameters.length - 1];
        for (int i = 1; i < parameters.length; i++) {
            expressionParameters[i - 1] = new Expression(parameters[i]);
        }
    }

    public SyncFunctionCommand(int depth, String[] parameters) {
        super(depth, "sync", parameters);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        String interpret = functionName.interpret(playerDataAccessor, variables).toString();
        PlayerDataAccessor.Function function = playerDataAccessor.getFunctions().get(interpret);
        if(function == null){
            return null;
        }
        for (int i = 0; i < function.parameters.size(); i++) {
            String varName = function.parameters.get(i);
            Expression expressionParameter = expressionParameters[i+1];
            variables.put(varName,expressionParameter.interpret(playerDataAccessor, variables).toString());
        }
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.setCommands(function.commands);
        nestedCommandWrapper.setUseNewReturnContext(true);
//        FunctionExecutor.syncExecute(playerDataAccessor,function.commands,performReturnContent,variables);
        return nestedCommandWrapper;
    }
}
