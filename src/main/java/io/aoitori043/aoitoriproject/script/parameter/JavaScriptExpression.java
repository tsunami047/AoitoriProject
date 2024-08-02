package io.aoitori043.aoitoriproject.script.parameter;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.parameter.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.impl.ConfigHandler.engine;
import static io.aoitori043.aoitoriproject.script.parameter.Expression.*;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-31  15:40
 * @Description: ?
 */
public class JavaScriptExpression {

    private Block[] blocks;

    public JavaScriptExpression(String expression) {
        this.compile(expression);
    }

    public String interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Block block : blocks) {
            stringBuilder.append(block.interpret(playerDataAccessor, variables));
        }
        return stringBuilder.toString();
    }

    public static @NotNull StringBuilder getScriptStringBuffer(StringBuilder buffer, List<Block> blockList) {
        if (buffer.length() > 0) {
//            blockList.add(wrapperToTextBlock(buffer.toString()));
            buffer = new StringBuilder();
        }
        return buffer;
    }

    public void compile(String parameter) {
        try {
            engine.eval(parameter);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (parameter == null) {
            return;
        }
        List<Block> blockList = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int varIndex = 0;
        for (int index = 0; index < parameter.length(); index++) {
            char c = parameter.charAt(index);
            if (c == '%' && (index == 0 || parameter.charAt(index - 1) != '\\')) {
                buffer = getScriptStringBuffer(buffer, blockList);
                int asteriskIndex = findCharIndex(parameter, index, '%');
                String nestedText = parameter.substring(index, asteriskIndex + 1);
                index = asteriskIndex;
                Block block = wrapperToPlaceholderBlock(nestedText);
                blockList.add(block);
                block.setVariableName(varNames[varIndex++]);
                continue;
            }
            if (c == '\\' && (index == parameter.length() - 1 ||
                    parameter.charAt(index + 1) != '%')) {
                continue;
            }
            buffer.append(c);
        }
        getScriptStringBuffer(buffer, blockList);
        this.blocks = blockList.toArray(new Block[0]);
    }
}
