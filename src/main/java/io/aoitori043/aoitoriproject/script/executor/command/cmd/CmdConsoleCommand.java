package io.aoitori043.aoitoriproject.script.executor.command.cmd;


import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import io.aoitori043.aoitoriproject.utils.CMDUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class CmdConsoleCommand extends AbstractCommand {

    Expression cmd;

    public CmdConsoleCommand(int depth, String[] parameters) {
        super(depth, "cmd_console", parameters);
    }

    public void compile() {
        cmd = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        CMDUtils.performCmd(cmd.interpret(playerDataAccessor, variables).toString());
        return null;
    }
}