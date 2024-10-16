package io.aoitori043.aoitoriproject.command;

import io.aoitori043.aoitoriproject.utils.raytrace.MethodSupplement;
import lombok.Data;
import lombok.Getter;
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
public abstract class SubCommand implements Cloneable {

    @Override
    public SubCommand clone() throws CloneNotSupportedException {
        return (SubCommand) super.clone();
    }

    public List<ArgumentHelper> fillParameters(String[] args){
        List<ArgumentHelper> list = new ArgumentList<>();
        for (int i = isNotArgument?1:2; i < args.length; i++) {
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
                executeMethod.invoke(instance,sender, instance.fillParameters(arguments));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void executeNotArgumentMethod(CommandSender sender,String[] arguments) {
        try {
            if (isInBasicCommand){
                this.notArgumentMethod.invoke(basicCommand,sender,this.fillParameters(arguments));
            }else{
                this.notArgumentMethod.invoke(this,sender,this.fillParameters(arguments));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public HashMap<String,SubCommandExecutor> subCommands = new HashMap<>();

    public boolean isInBasicCommand;

    public BasicCommand basicCommand;
    public int weight;
    public Method notArgumentMethod;
    public TreeMap<Integer,ParameterSpecification> map;
    public boolean isNotArgument = false;
    public String help;
    public String commandprefix;
    public String tabMethodName;
    public Method method;

    public boolean isOp = false;
    public String permission = null;
    public String executionStartMessage = null;
    @Getter
    public String executionEndMessage = null;

    public int minLength = -1;


    public SubCommand() {
    }

    public void sendMessage(CommandSender sender, String message){
        basicCommand.sendMessage(sender, message);
    }

}
