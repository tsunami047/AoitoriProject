package io.aoitori043.aoitoriproject.script.executor;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.command.sign.DelayCommand;
import io.aoitori043.aoitoriproject.thread.AoitoriScheduler;
import kilim.Pausable;
import kilim.Task;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  22:28
 * @Description: ?
 */
public class FunctionExecutor {

    public static void syncExecuteNotDelay(PlayerDataAccessor playerDataAccessor,List<AbstractCommand> commands, AbstractCommand.PerformReturnContent performReturnContent, ConcurrentHashMap<String, Object> varsRuntime){
        for (int i = 0; i < commands.size(); i++) {
            AbstractCommand abstractCommand = commands.get(i);
            if(abstractCommand instanceof DelayCommand){
                continue;
            }
            AbstractCommand.NestedCommandWrapper execute = abstractCommand.execute(playerDataAccessor, performReturnContent, varsRuntime);
            if (performReturnContent.isReturn) {
                return;
            }
            if(execute!=null){
                if(execute.isLoop){
                    if(execute.isAsync){
                        AoitoriScheduler.forkJoinExecute(()->{
                            while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                syncExecute(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                            }
                        });
                    }else {
                        while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                            syncExecuteNotDelay(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                        }
                    }
                }else if(execute.isAsync){
                    asyncExecute(playerDataAccessor, execute.commands,performReturnContent,varsRuntime);
                }else if(execute.commands!=null){
                        syncExecuteNotDelay(playerDataAccessor, execute.commands,performReturnContent,varsRuntime);
                }
            }
            if(performReturnContent.getGotoIndex() != -1){
                i = performReturnContent.getGotoIndex();
                performReturnContent.setGotoIndex(-1);
            }
            if(performReturnContent.isBreak){
                performReturnContent.setBreak(false);
                return;
            }
        }
    }

    public static void syncExecute(PlayerDataAccessor playerDataAccessor,List<AbstractCommand> commands, AbstractCommand.PerformReturnContent performReturnContent, ConcurrentHashMap<String, Object> varsRuntime) throws Pausable {
        for (int i = 0; i < commands.size(); i++) {
            AbstractCommand abstractCommand = commands.get(i);
            if(abstractCommand instanceof DelayCommand){
                DelayCommand command = (DelayCommand) abstractCommand;
                double delay = Double.parseDouble(command.delay.interpret(playerDataAccessor, varsRuntime).toString())*1000;
                Task.sleep((long) delay);
                continue;
            }
            AbstractCommand.NestedCommandWrapper execute = abstractCommand.execute(playerDataAccessor, performReturnContent, varsRuntime);
            if (performReturnContent.isReturn) {
                return;
            }
            if(execute!=null){
                if(execute.isLoop){
                    if(execute.isAsync){
                        AoitoriScheduler.forkJoinExecute(()->{
                            while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                syncExecute(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                            }
                        });
                    }else {
                        while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                            syncExecute(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                        }
                    }
                }else if(execute.isAsync){
                    asyncExecute(playerDataAccessor, execute.commands,performReturnContent,varsRuntime);
                }else if(execute.commands!=null){
                    syncExecuteNotDelay(playerDataAccessor, execute.commands,performReturnContent,varsRuntime);
                }
            }
            if(performReturnContent.getGotoIndex() != -1){
                i = performReturnContent.getGotoIndex();
                performReturnContent.setGotoIndex(-1);
            }
            if(performReturnContent.isBreak){
                performReturnContent.setBreak(false);
                return;
            }
        }
    }

    public static void asyncExecute(PlayerDataAccessor playerDataAccessor, List<AbstractCommand> commands){
        AoitoriScheduler.forkJoinExecute(()->{
            syncExecute(playerDataAccessor, commands,new AbstractCommand.PerformReturnContent(),new ConcurrentHashMap<>());
        });
    }

    public static void asyncExecute(PlayerDataAccessor playerDataAccessor, List<AbstractCommand> commands, AbstractCommand.PerformReturnContent performReturnContent, ConcurrentHashMap<String, Object> varsRuntime) {
        AoitoriScheduler.forkJoinExecute(()->{
            syncExecute(playerDataAccessor, commands,performReturnContent,varsRuntime);
        });
    }
}
