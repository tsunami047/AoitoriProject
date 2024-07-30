package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:02
 * @Description: ?
 */
@Data
public class FunctionBlock implements Block {
    AbstractCommand abstractCommand;
    String original;
    String variableName;

    @Override
    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        AbstractCommand.PerformReturnContent performReturnContent = new AbstractCommand.PerformReturnContent();
        abstractCommand.execute(playerDataAccessor, performReturnContent, variables);
        return performReturnContent.getResult();
    }

    @Override
    public String getData() {
        return original;
    }

}
