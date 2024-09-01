package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-08-23  16:55
 * @Description: ?
 */
public class GetPlayerMode extends AbstractCommand {

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        performReturnContent.setResult(System.currentTimeMillis());
        return nestedCommandWrapper;
    }

    public GetPlayerMode(int depth, String[] parameters) {
        super(depth, "getMode", parameters);
    }

    public void compile() {
    }
}