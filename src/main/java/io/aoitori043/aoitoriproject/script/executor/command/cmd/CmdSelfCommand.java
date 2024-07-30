package io.aoitori043.aoitoriproject.script.executor.command.cmd;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CmdSelfCommand extends AbstractCommand {

    Expression cmd;

    public CmdSelfCommand(int depth, String[] parameters) {
        super(depth, "cmd_self", parameters);
    }

    public void compile() {
        cmd = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        playerDataAccessor.player.performCommand(cmd.interpret(playerDataAccessor, variables).toString());
        return null;
    }
}