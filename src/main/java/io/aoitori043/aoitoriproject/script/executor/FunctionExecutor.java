package io.aoitori043.aoitoriproject.script.executor;

import io.aoitori043.aoitoriproject.AoitoriProject;
import io.aoitori043.aoitoriproject.script.AoitoriPlayerQuitEvent;
import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.command.ParkCommand;
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
            try {
                AbstractCommand abstractCommand = commands.get(i);
                if (abstractCommand instanceof DelayCommand) {
                    continue;
                }
                AbstractCommand.NestedCommandWrapper execute = abstractCommand.execute(playerDataAccessor, performReturnContent, varsRuntime);
                if (performReturnContent.isReturn) {
                    return;
                }
                if (execute != null) {
                    if (execute.isLoop) {
                        if (execute.isAsync) {
                            AoitoriScheduler.forkJoinExecute(() -> {
                                while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                    syncExecute(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                                    if (performReturnContent.isReturn) {
                                        return;
                                    }
                                }
                            });
                        } else {
                            while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                syncExecuteNotDelay(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                                if (performReturnContent.isReturn) {
                                    return;
                                }
                            }
                        }
                    } else if (execute.isAsync) {
                        asyncExecute(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                    } else if (execute.commands != null) {
                        if(execute.useNewReturnContext){
                            syncExecuteNotDelay(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                        }else {
                            syncExecuteNotDelay(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                        }
                        if (performReturnContent.isReturn) {
                            return;
                        }
                    }
                }
                if (performReturnContent.getGotoIndex() != -1) {
                    i = performReturnContent.getGotoIndex();
                    performReturnContent.setGotoIndex(-1);
                }
                if (performReturnContent.isBreak) {
                    performReturnContent.setBreak(false);
                    return;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void syncExecute(PlayerDataAccessor playerDataAccessor,List<AbstractCommand> commands, AbstractCommand.PerformReturnContent performReturnContent, ConcurrentHashMap<String, Object> varsRuntime) throws Pausable {
        for (int i = 0; i < commands.size(); i++) {
            try {
                AbstractCommand abstractCommand = commands.get(i);
                if (abstractCommand instanceof DelayCommand) {
                    DelayCommand command = (DelayCommand) abstractCommand;
                    double delay = Double.parseDouble(command.delay.interpret(playerDataAccessor, varsRuntime).toString()) * 1000;
                    Task.sleep((long) delay);
                    if (!AoitoriProject.isPlayerOnline(playerDataAccessor.getPlayer().getName())) {
                        performReturnContent.setReturn(true);
                        return;
                    }
                    continue;
                } else if (abstractCommand instanceof ParkCommand) {
                    ParkCommand command = (ParkCommand) abstractCommand;
                    long checkInterval = (long) (((Double) command.checkTime.interpret(playerDataAccessor, varsRuntime)) * 1000);
                    long waitTimes = (long) (((Double) command.waitTime.interpret(playerDataAccessor, varsRuntime)) * 1000) / checkInterval;
                    boolean isWake = false;
                    for (int j = 0; j < waitTimes; j++) {
                        if (command.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                            isWake = true;
                            break;
                        }else{
                            Task.sleep(checkInterval);
                        }
                    }
                    if(isWake){
                        continue;
                    }else {
                        return;
                    }
                }
                AbstractCommand.NestedCommandWrapper execute = abstractCommand.execute(playerDataAccessor, performReturnContent, varsRuntime);
                if (performReturnContent.isReturn) {
                    return;
                }
                if (execute != null) {
                    if (execute.isLoop) {
                        if (execute.isAsync) {
                            AoitoriScheduler.forkJoinExecute(() -> {
                                while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                    syncExecute(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                                    if (performReturnContent.isReturn) {
                                        return;
                                    }
                                }
                            });
                        } else {
                            while (execute.expression.executeAsBoolean(playerDataAccessor, varsRuntime)) {
                                syncExecute(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                                if (performReturnContent.isReturn) {
                                    return;
                                }
                            }
                        }
                    } else if (execute.isAsync) {
                        asyncExecute(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                    } else if (execute.commands != null) {
                        if (execute.isUseNewReturnContext()) {
                            syncExecute(playerDataAccessor, execute.commands, new AbstractCommand.PerformReturnContent(), varsRuntime);
                        }else {
                            syncExecute(playerDataAccessor, execute.commands, performReturnContent, varsRuntime);
                        }
                        if (performReturnContent.isReturn) {
                            return;
                        }
                    }
                }
                if (performReturnContent.getGotoIndex() != -1) {
                    i = performReturnContent.getGotoIndex();
                    performReturnContent.setGotoIndex(-1);
                }
                if (performReturnContent.isBreak) {
                    performReturnContent.setBreak(false);
                    return;
                }
            }catch (Exception e){
                e.printStackTrace();
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
            syncExecute(playerDataAccessor, commands,new AbstractCommand.PerformReturnContent(),varsRuntime);
        });
    }
}
