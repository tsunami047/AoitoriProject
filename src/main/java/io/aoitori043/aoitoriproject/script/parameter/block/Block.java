package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:08
 * @Description: ?
 */
public interface Block {

    Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables);

    String getData();

    void setVariableName(String variableName);
    String getVariableName();
}
