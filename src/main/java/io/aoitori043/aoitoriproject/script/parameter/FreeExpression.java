package io.aoitori043.aoitoriproject.script.parameter;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.executor.CommandCompiler;
import io.aoitori043.aoitoriproject.script.executor.FunctionExecutor;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-09  15:52
 * @Description: ?
 */
@Data
public class FreeExpression {

    Expression expression;
    List<AbstractCommand> abstractCommands;

    public FreeExpression(List<String> list) {
        abstractCommands = CommandCompiler.nowCommandCompiler.startParserFunction(list);
    }

    public FreeExpression(String cmd) {
        if(cmd!=null){
            expression = new Expression(cmd);
        }
    }

    public FreeExpression(String cmd, Expression.CompiledType compiledType) {
        if(cmd!=null){
            expression = new Expression(cmd, compiledType);
        }
    }

    public Boolean resolveAsBoolean(PlayerDataAccessor playerDataAccessor){
        if (expression != null) {
            return expression.executeAsBoolean(playerDataAccessor, new ConcurrentHashMap<>());
        } else if (abstractCommands != null) {
            AbstractCommand.PerformReturnContent performReturnContent = new AbstractCommand.PerformReturnContent();
            FunctionExecutor.syncExecuteNotDelay(playerDataAccessor,abstractCommands,performReturnContent,new ConcurrentHashMap<>());
            Object result = performReturnContent.getResult();
            return result != null && Boolean.parseBoolean(result.toString());
        }
        return null;
    }

    public Object interpret(PlayerDataAccessor playerDataAccessor) {
        if (expression != null) {
            return expression.interpret(playerDataAccessor, new ConcurrentHashMap<>());
        } else if (abstractCommands != null) {
            AbstractCommand.PerformReturnContent performReturnContent = new AbstractCommand.PerformReturnContent();
            FunctionExecutor.syncExecuteNotDelay(playerDataAccessor,abstractCommands,performReturnContent,new ConcurrentHashMap<>());
            return performReturnContent.getResult();
        }
        return null;
    }

    public Object interpret(PlayerDataAccessor playerDataAccessor, ConcurrentHashMap<String,Object> formalVariables) {
        if (expression != null) {
            return expression.interpret(playerDataAccessor, formalVariables);
        } else if (abstractCommands != null) {
            AbstractCommand.PerformReturnContent performReturnContent = new AbstractCommand.PerformReturnContent();
            FunctionExecutor.syncExecuteNotDelay(playerDataAccessor,abstractCommands,performReturnContent,formalVariables);
            return performReturnContent.getResult();
        }
        return null;
    }

    public static FreeExpression freeExpressionFactory(Object o) {
        if(o instanceof List){
            return new FreeExpression((List) o);
        }else{
            return new FreeExpression(o.toString());
        }
    }
}
