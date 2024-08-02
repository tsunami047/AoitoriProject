package io.aoitori043.aoitoriproject.script.executor;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.command.nested.NestedCommand;
import io.aoitori043.aoitoriproject.script.executor.command.sign.ReturnCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  15:17
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomCommand extends AbstractCommand {

    public Expression[] funcArgs;
    public Boolean hasReturnValue;


    public void compile() {
        funcArgs = new Expression[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            funcArgs[i] = new Expression(parameters[i]);
        }
    }

    public CustomCommand(int depth,String funcName, String[] parameters) {
        super(depth, funcName, parameters);
    }

    public boolean nestedHasReturnValue(NestedCommand nestedCommand) {
        for (AbstractCommand command : nestedCommand.getNestedCommands()) {
            if (command instanceof ReturnCommand) {
                ReturnCommand returnCommand = (ReturnCommand) command;
                if (returnCommand.hasReturnValue()) {
                    hasReturnValue = true;
                }
            }
            if(command instanceof NestedCommand){
                if (nestedHasReturnValue((NestedCommand) command)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        PlayerDataAccessor.Function function = playerDataAccessor.getFunctions().get(super.type);
        if (function == null) {
            AoitoriProject.plugin.getLogger().warning("不存在指令类型： "+ super.type);
            return null;
        }
//        if(hasReturnValue == null){
//            for (AbstractCommand command : function.commands) {
//                if (command instanceof ReturnCommand) {
//                    ReturnCommand returnCommand = (ReturnCommand) command;
//                    if (returnCommand.hasReturnValue()) {
//                        hasReturnValue = true;
//                        break;
//                    }
//                }
//                if(command instanceof NestedCommand){
//                    if (nestedHasReturnValue((NestedCommand) command)) {
//                        hasReturnValue = true;
//                        break;
//                    }
//                }
//            }
//            hasReturnValue = false;
//        }
        for (int i = 0; i < funcArgs.length; i++) {
            variables.put(function.parameters.get(i),funcArgs[i].interpret(playerDataAccessor,variables));
        }
//        if(hasReturnValue){
//            FunctionExecutor.syncExecuteNotDelay(playerDataAccessor, function.commands, performReturnContent, variables);
//        }else {
//            NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
//            nestedCommandWrapper.setCommands(function.commands);
//        }
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        nestedCommandWrapper.setCommands(function.commands);
        nestedCommandWrapper.setUseNewReturnContext(true);
        return nestedCommandWrapper;
    }
}
