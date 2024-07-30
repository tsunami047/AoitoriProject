package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-06  21:03
 * @Description: ?
 */
@Data
@Deprecated
public class WeakVariableBlock implements Block {
    String weakVariableName;
    String variableName;

    @Override
    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        if (variables == null) {
            throw new NullPointerException("临时变量：" + weakVariableName + "未定义");
        } else {
            return variables.get(weakVariableName);
        }
    }

    @Override
    public String getData() {
        return weakVariableName;
    }
}