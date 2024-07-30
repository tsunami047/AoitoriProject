package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-09  02:06
 * @Description: ?
 */
public class ReplaceCommand extends AbstractCommand {

    Expression string;
    Expression oldString;
    Expression newString;

    public ReplaceCommand(int depth, String[] parameters) {
        super(depth, "replace", parameters);

    }

    public void compile() {
        this.string = new Expression(parameters[0]);
        this.oldString = new Expression(parameters[1]);
        this.newString = new Expression(parameters[2]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        String interpret = string.interpret(playerDataAccessor, variables).toString();
        String replace = interpret.replace(oldString.interpret(playerDataAccessor, variables).toString(), newString.interpret(playerDataAccessor, variables).toString());
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        performReturnContent.setResult(replace);
//        playerDataAccessor.player.sendMessage(msg.interpret(playerDataAccessor,variables));
        return nestedCommandWrapper;
    }
}