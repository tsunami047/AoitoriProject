package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:01
 * @Description: ?
 */
@Data
@Deprecated
public class VariableBlock implements Block {
    String original;
    String variableName;

    @Override
    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        return playerDataAccessor.getValue(variableName);
    }

    @Override
    public String getData() {
        return original;
    }
}
