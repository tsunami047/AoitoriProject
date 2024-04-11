package io.aoitori043.aoitoriproject.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:39
 * @Description: ?
 */
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

//    public static class ParameterSpecificationWrapper{
//        public int index;
//        public String tip;
//        public ParameterSpecification.Type type;
//        public boolean nullable;
//
//        public ParameterSpecificationWrapper(int index, String tip, ParameterSpecification.Type type, boolean nullable) {
//            this.index = index;
//            this.tip = tip;
//            this.type = type;
//            this.nullable = nullable;
//        }
//    }
//
//    public ParameterSpecificationWrapper data;
    public int weight;
    public int minLength;
    public TreeMap<Integer,ParameterSpecification> map;
    public boolean isOp = false;
    public String permission = null;
    public String executionStartMessage = null;
    public String executionEndMessage = null;

    public SubCommand() {
    }

    public abstract List<String> getTabCompletion(int index);

    public abstract void execute(List<ArgumentHelper> arguments);
}
