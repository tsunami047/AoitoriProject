package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.utils.PlaceholderAPIUtil;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:02
 * @Description: ?
 */
@Data
public class PlaceholderBlock implements Block {
    String removePercentSign;
    String original;
    String variableName;

    public PlaceholderBlock() {
    }

    @Override
    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        if(variables!=null){
            Object tempVariables = variables.get(removePercentSign);
            if(tempVariables != null){
                return tempVariables;
            }
        }
        Object value = playerDataAccessor.getValue(removePercentSign);
        if(value != null){
            return value;
        }
        return PlaceholderAPIUtil.throughPAPI(playerDataAccessor.player, original);
    }

    @Override
    public String getData() {
        return original;
    }
}
