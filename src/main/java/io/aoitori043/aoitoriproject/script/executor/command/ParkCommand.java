package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-01  03:55
 * @Description: ?
 */
public class ParkCommand extends AbstractCommand {

    public Expression checkTime;
    public Expression waitTime;
    public Expression expression;

    public ParkCommand(int depth, String[] parameters) {
        super(depth, "park", parameters);
    }

    public void compile() {
        this.expression = new Expression(parameters[0]);
        this.checkTime = new Expression(parameters[1]);
        this.waitTime = new Expression(parameters[2]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        return null;
    }
}