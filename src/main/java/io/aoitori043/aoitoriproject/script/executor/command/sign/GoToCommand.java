package io.aoitori043.aoitoriproject.script.executor.command.sign;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:42
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class GoToCommand extends AbstractCommand {

    Expression index;


    public GoToCommand(int depth, String[] parameters) {
        super(depth, "goto", parameters);
    }

    public void compile() {
        index = new Expression(parameters[0]);
    }

    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        performReturnContent.setGotoIndex(Integer.parseInt(index.interpret(playerDataAccessor,variables).toString()));
        return null;
    }
}
