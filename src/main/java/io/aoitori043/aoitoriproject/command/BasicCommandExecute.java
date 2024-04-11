package io.aoitori043.aoitoriproject.command;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:31
 * @Description: ?
 */

public class BasicCommandExecute implements CommandExecutor, TabExecutor {

    public BasicCommand myBasicCommand;
    public LinkedHashMap<String,SubCommand> subCommands;
    public List<String> help;

    public static String listToString(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            if (i < list.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public static LinkedHashMap<String, SubCommand> sortSubCommands(LinkedHashMap<String, SubCommand> subCommands) {
        LinkedHashMap<String, SubCommand> sortedSubCommands = new LinkedHashMap<>();
        subCommands.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(new SubCommandComparator()))
                .forEachOrdered(entry -> sortedSubCommands.put(entry.getKey(), entry.getValue()));
        return sortedSubCommands;
    }

    static class SubCommandComparator implements Comparator<SubCommand> {
        @Override
        public int compare(SubCommand subCommand1, SubCommand subCommand2) {
            return Integer.compare(subCommand1.weight, subCommand2.weight);
        }
    }

    public static PluginCommand createCommand(BasicCommand basicCommand,JavaPlugin javaPlugin) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        BasicCommandParameter annotation = basicCommand.getClass().getAnnotation(BasicCommandParameter.class);
        PluginCommand pluginCommand;
        if (annotation.name().equals("usePluginName")) {
            pluginCommand = constructor.newInstance(javaPlugin.getName(),javaPlugin);
        }else{
            pluginCommand = constructor.newInstance(annotation.name(),javaPlugin);
        }
        pluginCommand.setDescription(annotation.description());
        pluginCommand.setAliases(basicCommand.getAllAlias());
        CraftServer server = (CraftServer) Bukkit.getServer();
        SimpleCommandMap commandMap = server.getCommandMap();
        commandMap.register(javaPlugin.getName(),pluginCommand);
        return pluginCommand;
    }

    public static void registerCommandExecute(BasicCommand basicCommand){
        BasicCommandExecute basicCommandExecute = new BasicCommandExecute();
        basicCommandExecute.register(basicCommand);
    }

    public void register(BasicCommand basicCommand){
        myBasicCommand = basicCommand;
        BasicCommandParameter annotation = basicCommand.getClass().getAnnotation(BasicCommandParameter.class);
        if(annotation == null){
            throw new RuntimeException(basicCommand.getClass().getSimpleName()+" 没有添加注解");
        }
//        String subCommandPath = annotation.subCommandPath();
//        if(subCommandPath == null){
//            throw new RuntimeException(basicCommand.getClass().getSimpleName()+" 没有指定子指令实现路径！");
//        }
        List<SubCommand> subCommandList = new ArrayList<>();
        for (int i = 0; i < basicCommand.getSubCommands().length; i++) {
            Class<? extends SubCommand> subCommandClass = basicCommand.getSubCommands()[i];
            if (SubCommand.class.isAssignableFrom(subCommandClass)) {
                try {
                    Constructor<?> constructor = subCommandClass.getDeclaredConstructor();
                    SubCommand subCommand = (SubCommand) constructor.newInstance();
                    if(subCommand.weight!=-1){
                        subCommand.weight = i;
                    }
                    subCommandList.add(subCommand);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        subCommands = new LinkedHashMap<>();
        for (SubCommand subCommand : subCommandList) {
            SubArgument subArgument = subCommand.getClass().getAnnotation(SubArgument.class);
            if(subArgument == null){
                basicCommand.plugin.getLogger().warning(subCommand.getClass().getSimpleName()+ " 缺少明确参数注解！");
                continue;
            }
            subCommand.minLength = subArgument.minLength();
            try {
                Method execute = subCommand.getClass().getMethod("execute",List.class);
                ExecutePermission executePermission = execute.getAnnotation(ExecutePermission.class);
                if(executePermission!=null){
                    subCommand.isOp = executePermission.isOp();
                    subCommand.permission = executePermission.permission();
                }
                ExecutionStartMessage executionStartMessage = execute.getAnnotation(ExecutionStartMessage.class);
                if(executionStartMessage!=null){
                    subCommand.executionStartMessage = executionStartMessage.message();
                }
                ExecutionEndMessage executionEndMessage = execute.getAnnotation(ExecutionEndMessage.class);
                if(executionEndMessage!=null){
                    subCommand.executionEndMessage = executionEndMessage.message();
                }
            }catch (Exception e){
                basicCommand.plugin.getLogger().warning(subCommand.getClass().getSimpleName()+ " 内找不到execute()方法: "+e.getMessage());
                continue;
            }
            subCommands.put(subArgument.argument().toLowerCase(),subCommand);
        }
        subCommands = sortSubCommands(subCommands);
        try {
            PluginCommand command = createCommand(basicCommand, basicCommand.plugin);

            command.setExecutor(this);
            command.setTabCompleter(this);
        }catch (Exception e){
            e.printStackTrace();
        }

        //生成插件帮助
        help = new ArrayList<>();
        help.add(
                String.format("§f##%s-%s written by %s",
                        basicCommand.plugin.getName(),
                        basicCommand.plugin.getDescription().getVersion(),
                        listToString(basicCommand.plugin.getDescription().getAuthors())
                ));
        help.add(
                String.format("§f- /%s §7显示指令帮助",basicCommand.getAlias())
        );
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            SubCommand subCommand = entry.getValue();
            ParameterSpecifications parameterSpecificationsAnnotation = subCommand.getClass().getAnnotation(ParameterSpecifications.class);
            TreeMap<Integer,ParameterSpecification> treeMap = new TreeMap<>(Comparator.naturalOrder());
            SubArgument subArgument = subCommand.getClass().getAnnotation(SubArgument.class);
            if (parameterSpecificationsAnnotation != null) {
                ParameterSpecification[] parameterSpecifications = parameterSpecificationsAnnotation.value();
                if(subCommand.minLength == -1) {
                    subCommand.minLength = 1;
                }
                for (ParameterSpecification parameterSpecification : parameterSpecifications) {
                    treeMap.put(parameterSpecification.index(),parameterSpecification);
                    if(!parameterSpecification.nullable()){
                        subCommand.minLength ++;
                    }
                }
                subCommand.map = treeMap;
                StringBuilder stringBuilder = new StringBuilder("§f- /"+basicCommand.getAlias()+" "+subArgument.argument()+" ");
                treeMap.forEach((index,parameter)->{
                    if(!parameter.nullable()){
                        stringBuilder.append(String.format("<%s> ",parameter.tip()));
                    }else{
                        stringBuilder.append(String.format("[%s] ",parameter.tip()));
                    }
                });
                stringBuilder.append("§7").append(subArgument.help());
                help.add(
                        String.format(stringBuilder.toString())
                );
            }else{
                help.add(
                        String.format("§f- /" + basicCommand.getAlias() + " " + subArgument.argument() + " §7" + subArgument.help())
                );
                if(subCommand.minLength == -1){
                    subCommand.minLength = 1;
                }
            }
        }
    }

    public void sendHelp(CommandSender sender){
        for (String label : help) {
            myBasicCommand.sendMessage(sender,label);
        }
    }

    public static String convertNanosecondsToSeconds(long nanoseconds) {
        double seconds = (double) nanoseconds / 1_000_000_000.0;
        DecimalFormat df = new DecimalFormat("#.################");
        return df.format(seconds);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1){
            return new ArrayList<>(subCommands.keySet());
        }
        String argsHead = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(argsHead);
        if(subCommand == null || subCommand.map == null){
            return Collections.emptyList();
        }
        ParameterSpecification parameterSpecification = subCommand.map.get(args.length-2);
        if(parameterSpecification == null){
            return Collections.emptyList();
        }
        List<String> tabCompletion = subCommand.getTabCompletion(parameterSpecification.index());
        if(tabCompletion == null){
            switch (parameterSpecification.type()) {
                case Player: {
                    List<String> players = new ArrayList<>();
                    if (sender instanceof Player) {
                        players.add(sender.getName());
                        players.addAll(Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> !name.equals(sender.getName())).collect(Collectors.toList()));
                        return players;
                    }
                    return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
                }
                case Int:
                    return new ArrayList<>(Arrays.asList("1","2","3","4","5"));
                case Double:
                    return new ArrayList<>(Arrays.asList("1.0","2.0","3.0","4.0","5.0"));
                case Text:
                    return Arrays.asList("可莉不知道哦");
            }
        }
        return tabCompletion;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command myCommand, String lable, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String argsHead = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(argsHead);
        if(subCommand == null){
            myBasicCommand.sendMessageWithPrefix(sender,"§f/"+lable+" "+args[0] + " §c<-");
            myBasicCommand.sendMessageWithPrefix(sender,"没有匹配参数，输入 /"+myBasicCommand.getAlias()+" 可以获取指令帮助。");
            return true;
        }
        if(args.length < subCommand.minLength){
            myBasicCommand.sendMessageWithPrefix(sender,"必须指令参数缺少，输入 /"+myBasicCommand.getAlias()+" 可以获取指令帮助。");
            return true;
        }
        if (subCommand.executionStartMessage!=null) {
            myBasicCommand.sendMessage(sender,subCommand.executionStartMessage);
        }
        long startTime = System.nanoTime();
        subCommand.execute(SubCommand.fillParameters(args));
        long endTime = System.nanoTime();
        if (subCommand.executionEndMessage!=null) {
            myBasicCommand.sendMessage(sender,subCommand.executionEndMessage.replace("%time%",convertNanosecondsToSeconds(endTime-startTime)));
        }
        return true;
    }


}
