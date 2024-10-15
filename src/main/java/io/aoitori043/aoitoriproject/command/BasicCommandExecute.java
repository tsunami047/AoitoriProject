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

import static io.aoitori043.aoitoriproject.AoitoriProject.onlinePlayerNames;

/**
 * @Author: natsumi
 * @CreateTime: 2024-03-23  13:31
 * @Description: ?
 */

public class BasicCommandExecute implements CommandExecutor, TabExecutor {

    public BasicCommand myBasicCommand;
    public LinkedHashMap<String, SubCommand> subCommands;
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
        subCommands.entrySet().stream().sorted(Map.Entry.comparingByValue(new SubCommandComparator())).forEachOrdered(entry -> sortedSubCommands.put(entry.getKey(), entry.getValue()));
        return sortedSubCommands;
    }

    public static PluginCommand createCommand(BasicCommand basicCommand, JavaPlugin javaPlugin) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        BasicCommandParameter annotation = basicCommand.getClass().getAnnotation(BasicCommandParameter.class);
        PluginCommand pluginCommand;
        if (annotation.name().equals("usePluginName")) {
            pluginCommand = constructor.newInstance(javaPlugin.getName(), javaPlugin);
        } else {
            pluginCommand = constructor.newInstance(annotation.name(), javaPlugin);
        }
        pluginCommand.setDescription(annotation.description());
        pluginCommand.setAliases(basicCommand.getAllAlias());
        CraftServer server = (CraftServer) Bukkit.getServer();
        SimpleCommandMap commandMap = server.getCommandMap();
        commandMap.register(javaPlugin.getName(), pluginCommand);
        return pluginCommand;
    }

    public static void registerCommandExecute(BasicCommand basicCommand) {
        BasicCommandExecute basicCommandExecute = new BasicCommandExecute();
        basicCommandExecute.register(basicCommand);
    }

    public static String convertNanosecondsToSeconds(long nanoseconds) {
        double seconds = (double) nanoseconds / 1_000_000_000.0;
        DecimalFormat df = new DecimalFormat("#.################");
        return df.format(seconds);
    }

    public static List<String> getTabList(int index, SubCommand subCommand, String tabMethodName,String arg) {
        try {
            Class[] parameterTypes = new Class[]{int.class,String.class};
            Method method = subCommand.getClass().getMethod(tabMethodName, parameterTypes);
            method.setAccessible(true);
            Object[] arguments = new Object[]{index,arg};
            Object invoke = method.invoke(subCommand, arguments);
            if (invoke == null) {
                return null;
            }
            return (List<String>) invoke;
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public void register(BasicCommand basicCommand) {
        myBasicCommand = basicCommand;
//        BasicCommandParameter annotation = basicCommand.getClass().getAnnotation(BasicCommandParameter.class);
//        if(annotation == null){
//            throw new RuntimeException(basicCommand.getClass().getSimpleName()+" 没有添加注解");
//        }
        List<SubCommand> subCommandList = new ArrayList<>();
        for (int i = 0; i < basicCommand.getSubCommands().length; i++) {
            Class<? extends SubCommand> subCommandClass = basicCommand.getSubCommands()[i];
            if (SubCommand.class.isAssignableFrom(subCommandClass)) {
                try {
                    Constructor<?> constructor = subCommandClass.getDeclaredConstructor();
                    SubCommand subCommand = (SubCommand) constructor.newInstance();
                    if (subCommand.weight != -1) {
                        subCommand.weight = i;
                    }
                    subCommand.basicCommand = basicCommand;
                    subCommandList.add(subCommand);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        subCommands = new LinkedHashMap<>();
        for (Method method : basicCommand.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            TabCompletion tabCompletion = method.getAnnotation(TabCompletion.class);
            if (tabCompletion != null) {
                SubCommand newBasicCommand = subCommands.computeIfAbsent(tabCompletion.argument(), k -> new SubCommand() {
                });
                newBasicCommand.tabMethodName = method.getName();
                continue;
            }
            Parameter parameter = method.getAnnotation(Parameter.class);
            if (parameter != null) {
                try {
//                    BasicCommand newBasicCommand = (BasicCommand) basicCommand.clone();
//                    BasicCommand newBasicCommand = null;
                    SubCommand newBasicCommand = subCommands.computeIfAbsent(parameter.argument(), k -> new SubCommand() {
                    });
                    newBasicCommand.setBasicCommand(basicCommand);
                    newBasicCommand.isInBasicCommand = true;
                    newBasicCommand.setCommandprefix(parameter.argument());
                    newBasicCommand.setNotArgumentMethod(method);
                    ParameterSpecifications parameterSpecificationsAnnotation = method.getAnnotation(ParameterSpecifications.class);
                    if (parameterSpecificationsAnnotation != null) {
                        ParameterSpecification[] parameterSpecifications = parameterSpecificationsAnnotation.value();
                        if (newBasicCommand.minLength == -1) {
                            newBasicCommand.minLength = 1;
                        }
                        for (ParameterSpecification parameterSpecification : parameterSpecifications) {
                            if (!parameterSpecification.nullable()) {
                                newBasicCommand.minLength++;
                            }
                        }
                    } else if (method.getAnnotation(ParameterSpecification.class) != null) {
                        if (method.getAnnotation(ParameterSpecification.class).nullable()) {
                            newBasicCommand.minLength = 1;
                        }else{
                            newBasicCommand.minLength = 2;
                        }
                    }
                    newBasicCommand.isNotArgument = true;
                    newBasicCommand.help = parameter.help();
                    newBasicCommand.notArgumentMethod = method;
                    ExecutePermission executePermission = method.getAnnotation(ExecutePermission.class);
                    if (executePermission != null) {
                        newBasicCommand.isOp = executePermission.isOp();
                        newBasicCommand.permission = executePermission.permission();
                    }else{
                        newBasicCommand.isOp = true;
                        newBasicCommand.permission = "nope.";
                    }
                    ExecutionStartMessage executionStartMessage = method.getAnnotation(ExecutionStartMessage.class);
                    if (executionStartMessage != null) {
                        newBasicCommand.executionStartMessage = executionStartMessage.message();
                    }
                    ExecutionEndMessage executionEndMessage = method.getAnnotation(ExecutionEndMessage.class);
                    if (executionEndMessage != null) {
                        newBasicCommand.executionEndMessage = executionEndMessage.message();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (SubCommand subCommand : subCommandList) {
            SubArgument subArgument = subCommand.getClass().getAnnotation(SubArgument.class);
            if (subArgument == null) {
                basicCommand.plugin.getLogger().warning(subCommand.getClass().getSimpleName() + " 缺少明确参数注解！");
                continue;
            }
            subCommand.commandprefix = subArgument.argument();
            Method[] methods = subCommand.getClass().getDeclaredMethods(); //获取一个类声明的方法
            for (Method method : methods) { //遍历
                method.setAccessible(true);
                NotArgument notArgument = method.getAnnotation(NotArgument.class);
                if (notArgument != null) {
                    subCommand.setMethod(method);
                    ParameterSpecifications parameterSpecificationsAnnotation = method.getAnnotation(ParameterSpecifications.class);
                    if (parameterSpecificationsAnnotation != null) {
                        ParameterSpecification[] parameterSpecifications = parameterSpecificationsAnnotation.value();
                        if (subCommand.minLength == -1) {
                            subCommand.minLength = 1;
                        }
                        for (ParameterSpecification parameterSpecification : parameterSpecifications) {
                            if (!parameterSpecification.nullable()) {
                                subCommand.minLength++;
                            }
                        }
                    } else if (method.getAnnotation(ParameterSpecification.class) != null) {
                        if (method.getAnnotation(ParameterSpecification.class).nullable()) {
                            subCommand.minLength = 1;
                        }else{
                            subCommand.minLength = 2;
                        }
                    }

                    subCommand.isNotArgument = true;
                    subCommand.help = notArgument.help();
                    subCommand.notArgumentMethod = method;
                    ExecutePermission executePermission = method.getAnnotation(ExecutePermission.class);
                    if (executePermission != null) {
                        subCommand.isOp = executePermission.isOp();
                        subCommand.permission = executePermission.permission();
                    }else{
                        subCommand.isOp = true;
                        subCommand.permission = "nope.";
                    }
                    ExecutionStartMessage executionStartMessage = method.getAnnotation(ExecutionStartMessage.class);
                    if (executionStartMessage != null) {
                        subCommand.executionStartMessage = executionStartMessage.message();
                    }
                    ExecutionEndMessage executionEndMessage = method.getAnnotation(ExecutionEndMessage.class);
                    if (executionEndMessage != null) {
                        subCommand.executionEndMessage = executionEndMessage.message();
                    }
                    continue;
                }
                TabCompletion tabCompletion = method.getAnnotation(TabCompletion.class);
                if (tabCompletion != null) {
                    subCommand.tabMethodName = method.getName();
//                    if(subCommand.isNotArgument){
//                        subCommand.tabMethodName = method.getName();
//                    }else {
                        SubCommand.SubCommandExecutor subCommandExecutor = subCommand.subCommands.computeIfAbsent(tabCompletion.argument(), k -> new SubCommand.SubCommandExecutor(subCommand));
                        subCommandExecutor.tabMethodName = method.getName();
//                    }
                    continue;
                }
                Parameter parameter = method.getAnnotation(Parameter.class);
                if (parameter != null) {
                    SubCommand.SubCommandExecutor subCommandExecutor = subCommand.subCommands.computeIfAbsent(parameter.argument(), k -> new SubCommand.SubCommandExecutor(subCommand));
                    ExecutePermission executePermission = method.getAnnotation(ExecutePermission.class);
                    if (executePermission != null) {
                        subCommandExecutor.isOp = executePermission.isOp();
                        subCommandExecutor.permission = executePermission.permission();
                    }else{
                        subCommandExecutor.isOp = true;
                        subCommandExecutor.permission = "nope.";
                    }
                    ExecutionStartMessage executionStartMessage = method.getAnnotation(ExecutionStartMessage.class);
                    if (executionStartMessage != null) {
                        subCommandExecutor.executionStartMessage = executionStartMessage.message();
                    }
                    ExecutionEndMessage executionEndMessage = method.getAnnotation(ExecutionEndMessage.class);
                    if (executionEndMessage != null) {
                        subCommandExecutor.executionEndMessage = executionEndMessage.message();
                    }
                    subCommandExecutor.methodName = method.getName();
                }
            }
            subCommands.put(subArgument.argument().toLowerCase(), subCommand);
        }
        subCommands = sortSubCommands(subCommands);
        try {
            PluginCommand command = createCommand(basicCommand, basicCommand.plugin);

            command.setExecutor(this);
            command.setTabCompleter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //生成插件帮助
        help = new ArrayList<>();
        help.add(String.format("§f##%s-%s written by %s", basicCommand.plugin.getName(), basicCommand.plugin.getDescription().getVersion(), listToString(basicCommand.plugin.getDescription().getAuthors())));
        help.add(String.format("§f- /%s §7显示指令帮助", basicCommand.getAlias()));
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            SubCommand subCommand = entry.getValue();
            if (subCommand.isNotArgument) {
//                help.add(String.format("§f- /" + basicCommand.getAlias() + " " + subCommand.commandprefix + " §7" + subCommand.help));
                generateNotArgumentHelp(basicCommand,subCommand);
                continue;
            }
            for (Map.Entry<String, SubCommand.SubCommandExecutor> executorEntry : subCommand.subCommands.entrySet()) {
                SubCommand.SubCommandExecutor subCommandExecutor = executorEntry.getValue();
                generateHelp(basicCommand, subCommandExecutor, subCommand);
            }
        }
    }

    private void generateNotArgumentHelp(BasicCommand basicCommand, SubCommand subCommand) {
        Method executeMethod = subCommand.getNotArgumentMethod();
        ParameterSpecifications parameterSpecificationsAnnotation = executeMethod.getAnnotation(ParameterSpecifications.class);
        TreeMap<Integer, ParameterSpecification> treeMap = new TreeMap<>(Comparator.naturalOrder());
        if (parameterSpecificationsAnnotation != null) {
            ParameterSpecification[] parameterSpecifications = parameterSpecificationsAnnotation.value();
            for (ParameterSpecification parameterSpecification : parameterSpecifications) {
                treeMap.put(parameterSpecification.index(), parameterSpecification);
            }
            subCommand.setMap(treeMap);
            StringBuilder stringBuilder = new StringBuilder("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " ");
            treeMap.forEach((index, parameter1) -> {
                if (!parameter1.nullable()) {
                    stringBuilder.append(String.format("<%s> ", parameter1.tip()));
                } else {
                    stringBuilder.append(String.format("[%s] ", parameter1.tip()));
                }
            });
            stringBuilder.append("§7").append(subCommand.help);
            help.add(String.format(stringBuilder.toString()));
        } else if (executeMethod.getAnnotation(ParameterSpecification.class) != null) {
            ParameterSpecification parameterSpecification = executeMethod.getAnnotation(ParameterSpecification.class);
            treeMap.put(parameterSpecification.index(), parameterSpecification);
            subCommand.setMap(treeMap);
            StringBuilder stringBuilder = new StringBuilder("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " ");
            treeMap.forEach((index, parameter1) -> {
                if (!parameter1.nullable()) {
                    stringBuilder.append(String.format("<%s> ", parameter1.tip()));
                } else {
                    stringBuilder.append(String.format("[%s] ", parameter1.tip()));
                }
            });
            stringBuilder.append("§7").append(subCommand.help);
            help.add(String.format(stringBuilder.toString()));
        } else {
            help.add(String.format("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " "  + "§7" + subCommand.help));
        }
    }

    private void generateHelp(BasicCommand basicCommand, SubCommand.SubCommandExecutor subCommandExecutor, SubCommand subCommand) {
        Method executeMethod = subCommandExecutor.getExecuteMethod();
        ParameterSpecifications parameterSpecificationsAnnotation = executeMethod.getAnnotation(ParameterSpecifications.class);
        TreeMap<Integer, ParameterSpecification> treeMap = new TreeMap<>(Comparator.naturalOrder());
        Parameter parameter = executeMethod.getAnnotation(Parameter.class);
        if (parameterSpecificationsAnnotation != null) {
            ParameterSpecification[] parameterSpecifications = parameterSpecificationsAnnotation.value();
            if (subCommandExecutor.minLength == -1) {
                subCommandExecutor.minLength = 1;
            }
            for (ParameterSpecification parameterSpecification : parameterSpecifications) {
                treeMap.put(parameterSpecification.index(), parameterSpecification);
                if (!parameterSpecification.nullable()) {
                    subCommandExecutor.minLength++;
                }
            }
            subCommandExecutor.map = treeMap;
            StringBuilder stringBuilder = new StringBuilder("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " " + parameter.argument() + " ");
            treeMap.forEach((index, parameter1) -> {
                if (!parameter1.nullable()) {
                    stringBuilder.append(String.format("<%s> ", parameter1.tip()));
                } else {
                    stringBuilder.append(String.format("[%s] ", parameter1.tip()));
                }
            });
            stringBuilder.append("§7").append(parameter.help());
            help.add(String.format(stringBuilder.toString()));
        } else if (executeMethod.getAnnotation(ParameterSpecification.class) != null) {
            ParameterSpecification parameterSpecification = executeMethod.getAnnotation(ParameterSpecification.class);
            treeMap.put(parameterSpecification.index(), parameterSpecification);
            subCommandExecutor.map = treeMap;
            StringBuilder stringBuilder = new StringBuilder("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " " + parameter.argument() + " ");
            treeMap.forEach((index, parameter1) -> {
                if (!parameter1.nullable()) {
                    stringBuilder.append(String.format("<%s> ", parameter1.tip()));
                } else {
                    stringBuilder.append(String.format("[%s] ", parameter1.tip()));
                }
            });
            stringBuilder.append("§7").append(parameter.help());
            help.add(String.format(stringBuilder.toString()));
        } else {
            help.add(String.format("§f- /" + basicCommand.getAlias() + " " + subCommand.getCommandprefix() + " " + parameter.argument() + " §7" + parameter.help()));
            if (subCommandExecutor.minLength == -1) {
                subCommandExecutor.minLength = 1;
            }
        }
    }

    public void sendHelp(CommandSender sender) {
        for (String label : help) {
            myBasicCommand.sendMessage(sender, label);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(subCommands.keySet());
        }
        String argsHead = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(argsHead);
        if (subCommand == null || subCommand.subCommands == null) {
            return Collections.emptyList();
        }
        if(subCommand.isNotArgument){
            String tabMethodName = subCommand.tabMethodName;
            List<String> tabList = getTabList(args.length - 2, subCommand, tabMethodName,args[args.length-1]);
            if (tabList != null) {
                return tabList;
            }else{
//                return Collections.emptyList();
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
        }
        SubCommand.SubCommandExecutor subCommandExecutor;
        if (args.length == 2) {
            subCommandExecutor = subCommand.subCommands.get("notArgument");
            if (subCommandExecutor == null) {
//                return Collections.emptyList();
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
        }
        String firstArgument = args[1];
        subCommandExecutor = subCommand.subCommands.get(firstArgument);
        if (subCommandExecutor == null) {
//            return Collections.emptyList();
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        }
        String tabMethodName = subCommandExecutor.getTabMethodName();
        List<String> tabList = getTabList(args.length - 3, subCommand, tabMethodName,args[args.length-1]);
        if (tabList != null) {
            return tabList;
        }
        TreeMap<Integer, ParameterSpecification> map = subCommandExecutor.getMap();
        ParameterSpecification parameterSpecification = map.get(args.length - 3);
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
                return new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5"));
            case Double:
                return new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0", "5.0"));
            case Text:
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        }
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int i = 0;
        int length = str.length();
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static String mergeAndHighlight(String[] strings, int indexToHighlight) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i == indexToHighlight) {
                result.append("§c").append(strings[i]).append("§f");
            } else {
                result.append(strings[i]);
            }
            if (i < strings.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    private static final String DOUBLE_PATTERN = "[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?";

    public static boolean isDouble(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches(DOUBLE_PATTERN);
    }






    @Override
    public boolean onCommand(CommandSender sender, Command myCommand, String lable, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String argsHead = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(argsHead);
        if (subCommand == null) {
            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, 0) + " §c<-");
            myBasicCommand.sendMessageWithPrefix(sender, "没有匹配参数，输入 /" + myBasicCommand.getAlias() + " 可以获取指令帮助。");
            return true;
        }
        if(subCommand.isNotArgument){
            if(subCommand.isOp){
                if(sender instanceof Player){
                    if (!((Player) sender).isOp()) {
                        if(subCommand.permission.equals("nope.") || !sender.hasPermission(subCommand.permission)){
                            myBasicCommand.sendMessageWithPrefix(sender, "§c你没有权限执行这条指令。");
                            return true;
                        }
                    }
                }else if(!(sender instanceof ConsoleCommandSender)){
                    myBasicCommand.sendMessageWithPrefix(sender, "无法判断该命令执行者是否拥有权限执行此指令。");
                    return true;
                }
            }else if(!subCommand.permission.equals("nope.") && !sender.hasPermission(subCommand.permission)){
                myBasicCommand.sendMessageWithPrefix(sender, "§c你没有权限执行这条指令。");
                return true;
            }
            if(args.length < subCommand.minLength){
                myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + String.join(" ", args) + " §c___ <-");
                myBasicCommand.sendMessageWithPrefix(sender, "缺少参数，输入 /" + myBasicCommand.getAlias() + " 可以获取指令帮助。");
                return true;
            }
            if(subCommand.map != null) {
                for (Map.Entry<Integer, ParameterSpecification> entry : subCommand.map.entrySet()) {
                    if (entry.getValue().nullable() && entry.getKey()+1 > args.length-1) {
                        continue;
                    }
                    String arg = args[entry.getKey() + 1];
                    switch (entry.getValue().type()) {
                        case Int: {
                            if (!isInteger(arg)) {
                                myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 1));
                                myBasicCommand.sendMessageWithPrefix(sender, "参数不正确，必须为整数： " + arg);
                                return true;
                            }
                            break;
                        }
                        case Double: {
                            if (!isDouble(arg)) {
                                myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 1));
                                myBasicCommand.sendMessageWithPrefix(sender, "参数不正确，必须为浮点数： " + arg);
                                return true;
                            }
                            break;
                        }
                        case Player: {
                            if (!onlinePlayerNames.contains(arg)) {
                                myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 1));
                                myBasicCommand.sendMessageWithPrefix(sender, "玩家不在线/不存在： " + arg);
                                return true;
                            }
                            break;
                        }
                    }
                }
            }
            if (subCommand.executionStartMessage != null) {
                myBasicCommand.sendMessageWithPrefix(sender,subCommand.executionStartMessage);
            }
            long startTime = System.nanoTime();
            subCommand.executeNotArgumentMethod(sender, args);
            long endTime = System.nanoTime();
            if (subCommand.executionEndMessage != null) {
                myBasicCommand.sendMessageWithPrefix(sender, subCommand.executionEndMessage.replace("%time%", convertNanosecondsToSeconds(endTime - startTime)));
            }
            return true;
        }
        if(args.length == 1){
            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + String.join(" ", args) + " §c___ <-");
            myBasicCommand.sendMessageWithPrefix(sender, "缺少参数，输入 /" + myBasicCommand.getAlias() + " 可以获取指令帮助。");
            return true;
        }
        SubCommand.SubCommandExecutor subCommandExecutor = subCommand.subCommands.get(args[1]);
        if (subCommandExecutor == null) {
            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, 1) + " §c<-");
            myBasicCommand.sendMessageWithPrefix(sender, "没有匹配参数，输入 /" + myBasicCommand.getAlias() + " 可以获取指令帮助。");
            return true;
        }
        if(subCommandExecutor.isOp){
            if(sender instanceof Player){
                if (!((Player) sender).isOp()) {
                    if(subCommandExecutor.permission.equals("nope.") || !sender.hasPermission(subCommandExecutor.permission)){
                        myBasicCommand.sendMessageWithPrefix(sender, "§c你没有权限执行这条指令。");
                        return true;
                    }
                }
            }else if(!(sender instanceof ConsoleCommandSender)){
                myBasicCommand.sendMessageWithPrefix(sender, "无法判断该命令执行者是否拥有权限执行此指令。");
                return true;
            }
        }else if(!subCommandExecutor.permission.equals("nope.") && !sender.hasPermission(subCommandExecutor.permission)){
            myBasicCommand.sendMessageWithPrefix(sender, "§c你没有权限执行这条指令。");
            return true;
        }
        if (args.length < subCommandExecutor.minLength+2) {
            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + String.join(" ", args) + " §c___ <-");
            myBasicCommand.sendMessageWithPrefix(sender, "缺少参数，输入 /" + myBasicCommand.getAlias() + " 可以获取指令帮助。");
            return true;
        }
        if(subCommandExecutor.map != null) {
            for (Map.Entry<Integer, ParameterSpecification> entry : subCommandExecutor.map.entrySet()) {
                ParameterSpecification value = entry.getValue();
                if (value.nullable() && args.length - 1 < entry.getKey() + 2) {
                    continue;
                }
                String arg = args[entry.getKey() + 2];
                switch (value.type()) {
                    case Int: {
                        if (!isInteger(arg)) {
                            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 2));
                            myBasicCommand.sendMessageWithPrefix(sender, "参数不正确，必须为整数： " + arg);
                            return true;
                        }
                        break;
                    }
                    case Double: {
                        if (!isDouble(arg)) {
                            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 2));
                            myBasicCommand.sendMessageWithPrefix(sender, "参数不正确，必须为浮点数： " + arg);
                            return true;
                        }
                        break;
                    }
                    case Player: {
                        if (!onlinePlayerNames.contains(arg)) {
                            myBasicCommand.sendMessageWithPrefix(sender, "§f/" + lable + " " + mergeAndHighlight(args, entry.getKey() + 2));
                            myBasicCommand.sendMessageWithPrefix(sender, "玩家不在线/不存在： " + arg);
                            return true;
                        }
                        break;
                    }
                }
            }
        }

        if (subCommandExecutor.executionStartMessage != null) {
            myBasicCommand.sendMessageWithPrefix(sender, subCommandExecutor.executionStartMessage);
        }
        long startTime = System.nanoTime();
        subCommandExecutor.executeCommand(sender, args);
        long endTime = System.nanoTime();
        if (subCommandExecutor.executionEndMessage != null) {
            myBasicCommand.sendMessageWithPrefix(sender, subCommandExecutor.executionEndMessage.replace("%time%", convertNanosecondsToSeconds(endTime - startTime)));
        }
        return true;
    }

    static class SubCommandComparator implements Comparator<SubCommand> {
        @Override
        public int compare(SubCommand subCommand1, SubCommand subCommand2) {
            return Integer.compare(subCommand1.weight, subCommand2.weight);
        }
    }


}
