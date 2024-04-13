package io.aoitori043.aoitoriproject.command;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:39
 * @Description: ?
 */
@Data
public abstract class SubCommand {

    public static List<ArgumentHelper> fillParameters(String[] args){
        List<ArgumentHelper> list = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            ArgumentHelper argumentHelper = new ArgumentHelper(i, args[i]);
            list.add(argumentHelper);
        }
        return list;
    }

    public static class ArgumentHelper{
        int index;
        private String originalArg;

        public ArgumentHelper(int index,String originalArg) {
            this.index = index;
            this.originalArg = originalArg;
        }

        public String getOriginalArg(){
            return originalArg;
        }

        public Player getAsPlayer(){
            return Bukkit.getPlayer(originalArg);
        }

        public int getAsInt(){
            if(originalArg == null){
                return 1;
            }
            return Integer.parseInt(originalArg);
        }

        public double getAsDouble(){
            if(originalArg == null){
                return 1;
            }
            return Double.parseDouble(originalArg);
        }
    }

    @Data
    public static class SubCommandExecutor{
        public SubCommand instance;
        public String methodName;
        public String tabMethodName;
        public int minLength;
        public TreeMap<Integer,ParameterSpecification> map;
        public boolean isOp = false;
        public String permission = null;
        public String executionStartMessage = null;
        public String executionEndMessage = null;

        public SubCommandExecutor(SubCommand instance) {
            this.instance = instance;
        }

        public Method getExecuteMethod(){
            try {
                Class[] parameterTypes = new Class[]{CommandSender.class, List.class};
                return instance.getClass().getMethod(methodName, parameterTypes);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public Method getTabExecuteMethod(){
            try {
                Class[] parameterTypes = new Class[]{int.class};
                return instance.getClass().getMethod(tabMethodName, parameterTypes);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        public void executeCommand(CommandSender sender,String[] arguments){
            try {
                Method executeMethod = getExecuteMethod();
                executeMethod.invoke(instance,sender, SubCommand.fillParameters(arguments));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void executeNotArgumentMethod(CommandSender sender,String[] arguments) {
        try {
            Class[] parameterTypes = new Class[]{CommandSender.class, List.class};
            Method method = this.getClass().getMethod(this.notArgumentMethodName, parameterTypes);
            method.invoke(this,sender,SubCommand.fillParameters(arguments));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public HashMap<String,SubCommandExecutor> subCommands = new HashMap<>();

    public BasicCommand basicCommand;
    public int weight;
    public String notArgumentMethodName;
    public boolean isNotArgument = false;
    public String help;
    public String commandprefix;

    public boolean isOp = false;
    public String permission = null;
    public String executionStartMessage = null;
    public String executionEndMessage = null;


    public SubCommand() {
    }

    public void sendMessage(CommandSender sender, String message){
        basicCommand.sendMessage(sender, message);
    }

}
