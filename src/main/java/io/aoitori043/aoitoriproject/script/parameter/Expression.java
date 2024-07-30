package io.aoitori043.aoitoriproject.script.parameter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.CommandCompiler;
import io.aoitori043.aoitoriproject.script.parameter.block.Block;
import io.aoitori043.aoitoriproject.script.parameter.block.FunctionBlock;
import io.aoitori043.aoitoriproject.script.parameter.block.PlaceholderBlock;
import io.aoitori043.aoitoriproject.script.parameter.block.TextBlock;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.jexl3.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.aoitori043.aoitoriproject.script.executor.CommandCompiler.customSplit;
import static java.time.temporal.ChronoUnit.HOURS;

/**
 * @Author: natsumi
 * @CreateTime: 2024-05-05  22:00
 * @Description:
 * 表达式变量替换成可以被jexl解析的变量，生成对应表达式，需要执行的时候，遍历块集合，放入变量，解析。
 * 纯文本类型，纯变量引用，直接返回object，不包装编译
 *
 */
@Data
public class Expression {

    public static JexlEngine jexl = new JexlBuilder().create();
    public static final String[] varNames = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};

    private Block[] blocks;

    public boolean cacheSafe = false;
    public boolean isMathExpression;
    public String variableExpression;
    private Block[] varaibleBlocks;
    private JexlExpression expression;


    public static Cache<String, Object> cache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(Duration.of(5, HOURS))
            .recordStats()
            .build();

    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        return isMathExpression ? this.execute(playerDataAccessor, variables) : interpretFilling(playerDataAccessor, variables);
    }

    private @NotNull Object interpretFilling(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        if(blocks.length == 1){
            return blocks[0].interpret(playerDataAccessor, variables);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Block block : blocks) {
            stringBuilder.append(block.interpret(playerDataAccessor, variables));
        }
        return stringBuilder.toString();
    }




//    public Object execute(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables){
////        if(cacheSafe){
////            Object interpret = interpretFilling(playerDataAccessor, variables);
////            Object ifPresent = cache.getIfPresent(interpret.toString());
////            if(ifPresent != null){
////                return ifPresent;
////            }else{
////                Object expressionResult = getExpressionResult(playerDataAccessor, variables);
////                cache.put(interpret.toString(),expressionResult);
////                return expressionResult;
////            }
////        }
//        return getExpressionResult(playerDataAccessor, variables);
//    }

    public Object execute(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables) {
        Map<String, Object> map = new HashMap<>();
        for (Block varaibleBlock : varaibleBlocks) {
            map.put(varaibleBlock.getVariableName(), varaibleBlock.interpret(playerDataAccessor,variables));
        }
        JexlContext context = new MapContext(map);
        return expression.evaluate(context);
    }

    public boolean executeAsBoolean(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String, Object> variables){
        return (boolean) execute(playerDataAccessor,variables);
    }

    public static String extract(String input) {
        if (input == null) {
            return null;
        }
        int firstQuoteIndex = input.indexOf('\'');
        if (firstQuoteIndex != 0) {
            return input;
        }
        int secondQuoteIndex = input.indexOf('\'', firstQuoteIndex + 1);
        if (secondQuoteIndex != -1) {
            return input.substring(firstQuoteIndex + 1, secondQuoteIndex);
        } else {
            return input;
        }
    }



    public Expression(String expression) {
        variableExpression = extract(expression);
        this.compile(variableExpression);
    }

    private static @NotNull StringBuilder getStringBuffer(StringBuilder buffer, List<Block> blockList) {
        if (buffer.length() > 0) {
            blockList.add(wrapperToTextBlock(buffer.toString()));
            buffer = new StringBuilder();
        }
        return buffer;
    }

    private static int findCharIndex(String text, int startIndex, char anchor) {
        for (int i = startIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == anchor && (i == 0 || text.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        throw new RuntimeException("语法错误！'" + anchor + "' 必须闭包，否则需要使用转义符'\\'转义: " + text);
    }

    public static Block wrapperToTextBlock(String refFunc) {
        TextBlock textBlock = new TextBlock();
        textBlock.setContent(refFunc);
        return textBlock;
    }

    public static Block wrapperToPlaceholderBlock(String refFunc) {
        PlaceholderBlock placeholderBlock = new PlaceholderBlock();
        placeholderBlock.setOriginal(refFunc);
        placeholderBlock.setRemovePercentSign(refFunc.substring(1,refFunc.length()-1));
        return placeholderBlock;
    }

    public static int countCharacter(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    public static int findClosingBracketIndex(String text, int startIndex) {
        int openBracketCount = 0;
        for (int i = startIndex + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') {
                openBracketCount++;
            } else if (c == ')') {
                openBracketCount--;
                if (openBracketCount == 0) {
                    return i;
                }
            }
        }
        throw new RuntimeException("语法错误！'('必须闭包，否则需要使用转义符'\\'转义: " + text);
    }

    public Block[] wrapperToFunctionBlock(String original) {
        for (int i = 0; i < original.indexOf("("); i++) {
            AbstractCommand abstractCommand = CommandCompiler.nowCommandCompiler.parsingStatement(original.substring(i),0);
            if(abstractCommand!=null){
                FunctionBlock functionBlock = new FunctionBlock();
                functionBlock.setAbstractCommand(abstractCommand);
                functionBlock.setOriginal(original.substring(i));
                if(i == 0){
                    return new Block[]{functionBlock};
                }
                String leftChar = original.substring(0,i);
                TextBlock textBlock = new TextBlock();
                textBlock.setContent(leftChar);
                return new Block[]{textBlock,functionBlock};
            }
        }
        return new Block[]{};
    }

    //"msg(replace('今天时间: %time%','%time%','getTime()')取公共冷却时间())"
    private void compile(String parameter) {
        if (parameter == null) {
            return;
        }
        List<Block> variableBlockList = new ArrayList<>();
        List<Block> blockList = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        int varIndex = 0;
        for (int index = 0; index < parameter.length(); index++) {
            char c = parameter.charAt(index);
            if (c == '(' && (index == 0 || parameter.charAt(index - 1) != '\\')) {
//                buffer = getStringBuffer(buffer, blockList);
                int closingIndex = findClosingBracketIndex(parameter, index-1);
                String nestedText = parameter.substring(index, closingIndex + 1);
                index = closingIndex;
//                String[] parameters = customSplit(nestedText.substring(0, nestedText.length() - 1),",");
                Block[] blocks = wrapperToFunctionBlock(buffer+nestedText);
                buffer = new StringBuilder();
                for (Block block : blocks) {
                    blockList.add(block);
                    block.setVariableName(varNames[varIndex++]);
                    variableBlockList.add(block);
                }
                continue;
            }
            if (c == '%' && (index == 0 || parameter.charAt(index - 1) != '\\')) {
                buffer = getStringBuffer(buffer, blockList);
                int asteriskIndex = findCharIndex(parameter, index, '%');
                String nestedText = parameter.substring(index, asteriskIndex + 1);
                index = asteriskIndex;
                Block block = wrapperToPlaceholderBlock(nestedText);
                blockList.add(block);
                variableBlockList.add(block);
                block.setVariableName(varNames[varIndex++]);
                continue;
            }
            if (c == '\\' && (index == parameter.length() - 1 ||
                    parameter.charAt(index + 1) != '%')) {
                continue;
            }
            buffer.append(c);
        }
        getStringBuffer(buffer, blockList);
        this.blocks = blockList.toArray(new Block[0]);
        this.varaibleBlocks = variableBlockList.toArray(new Block[0]);
        this.variableExpression = generateEncodedExpression();
        if(isValidMathExpression(parameter)){
            if (this.blocks.length == 1) {
                if(this.blocks[0] instanceof TextBlock){
                    TextBlock block = (TextBlock) this.blocks[0];
                    this.isMathExpression = block.getResult() instanceof String;
                }else{
                    this.isMathExpression = false;
                }
            }else {
                this.isMathExpression = true;
            }
        }
        try {
            this.expression = jexl.createExpression(variableExpression);
        }catch (Exception e){
            this.isMathExpression = false;
        }
    }

    public static void main(String[] args) {
        JexlExpression expression1 = jexl.createExpression("a + 3");
        Map<String, Object> map = new HashMap<>();
        map.put("a","1");
        JexlContext context = new MapContext(map);
        System.out.println(expression1.evaluate(context));
    }

    public static boolean isValidMathExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return false;
        }
        String pattern = ".*[+\\-*/!=].*";
        return expression.matches(pattern);
    }

    public String generateEncodedExpression(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Block block : blocks) {
            if(block instanceof TextBlock){
                stringBuilder.append(block.getData());
            }else {
                stringBuilder.append(block.getVariableName());
            }
        }
        return stringBuilder.toString();
    }

}
