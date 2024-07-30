package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-08  17:37
 * @Description: ?
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class RemovePotionCommand extends AbstractCommand {

    Expression potionType;

    public RemovePotionCommand(int depth, String[] parameters) {
        super(depth, "removePotion", parameters);
    }

    public void compile() {
        potionType = new Expression(parameters[0]);
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        playerDataAccessor.player.removePotionEffect(PotionEffectType.getByName(potionType.interpret(playerDataAccessor,variables).toString()));
        return null;
    }
}
