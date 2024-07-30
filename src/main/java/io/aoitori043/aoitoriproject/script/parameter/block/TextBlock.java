package io.aoitori043.aoitoriproject.script.parameter.block;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:01
 * @Description: ?
 */
@Data
public class TextBlock implements Block {
    String content;
    String variableName;
    Object result;

    public void setContent(String content) {
        this.content = content;
        this.parse();
    }

    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }

    public static boolean isLong(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void parse(){
        if (content == null || content.isEmpty()) {
            result =  "";
            return;
        }
        if(isInt(content)){
            result = Integer.valueOf(content);
            return;
        }
        if(isLong(content)){
            result = Long.valueOf(content);
            return;
        }
        if(isDouble(content)){
            result = Double.valueOf(content);
            return;
        }
        if(isBoolean(content)){
            result = Boolean.valueOf(content);
            return;
        }
        result = content;

    }

    @Override
    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        return result;
    }

    @Override
    public String getData() {
        return content;
    }
}
