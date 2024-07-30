package io.aoitori043.aoitoriproject.script.executor;


import io.aoitori043.aoitoriproject.script.AoitoriScriptLoadEvent;
import io.aoitori043.aoitoriproject.script.ClassImpl;
import io.aoitori043.aoitoriproject.script.executor.command.*;
import io.aoitori043.aoitoriproject.script.executor.command.cmd.CmdConsoleCommand;
import io.aoitori043.aoitoriproject.script.executor.command.cmd.CmdOPCommand;
import io.aoitori043.aoitoriproject.script.executor.command.cmd.CmdSelfCommand;
import io.aoitori043.aoitoriproject.script.executor.command.nested.*;
import io.aoitori043.aoitoriproject.script.executor.command.nested.switch_.CaseCommand;
import io.aoitori043.aoitoriproject.script.executor.command.nested.switch_.DefaultCommand;
import io.aoitori043.aoitoriproject.script.executor.command.nested.switch_.SwitchCommand;
import io.aoitori043.aoitoriproject.script.executor.command.sign.*;
import io.aoitori043.aoitoriproject.script.executor.command.variable.NewVariableCommand;
import io.aoitori043.aoitoriproject.script.executor.command.variable.SetVariableCommand;
import io.aoitori043.aoitoriproject.utils.lock.SemaphoreLock;
import lombok.Data;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  22:07
 * @Description: ?
 */
@Data
public class CommandCompiler {

    public static void registerCommands(Class<? extends AbstractCommand> command,String... functionName){
        for (String s : functionName) {
            map.put(s,command);
        }
    }
    public static HashMap<String, Class<? extends AbstractCommand>> map = new HashMap<>();

    static{
        registerStandardCommand();
    }

    public static CommandCompiler nowCommandCompiler = new CommandCompiler();
    public ClassImpl currentLoadClass;

    public CommandCompiler() {
    }

    public static String[] getParameter(String originalCommandText) {
        if(originalCommandText == null || originalCommandText.isEmpty()){
            return new String[0];
        }
//        String parameters = extractParameters(originalCommandText);
//        if(parameters == null || parameters.isEmpty()){
//            return new String[0];
//        }
        String[] strings = customSplit(originalCommandText, ",");
        if(strings.length==1){
            if(Objects.equals(strings[0], "")){
                return new String[0];
            }
        }
        return strings;
    }

    public static String extractParameters(String text) {
        int i = text.indexOf("(");
        if(i == -1){
            return text;
        }
        int startIndex = i + 1;
        int endIndex = startIndex;
        int bracketCount = 1;
        while (bracketCount > 0 && endIndex < text.length()) {
            char c = text.charAt(endIndex);
            if (c == '(') {
                bracketCount++;
            } else if (c == ')') {
                bracketCount--;
            }
            endIndex++;
        }
        return text.substring(startIndex, endIndex - 1);
    }

    public static String[] customSplit(String input, String delimiter) {
        List<String> parts = new ArrayList<>();
        String regex = "(?<!\\\\)" + Pattern.quote(delimiter);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        int start = 0;
        int bracketCount = 0;

        while (matcher.find()) {
            String substring = input.substring(start, matcher.start());
            for (char c : substring.toCharArray()) {
                if (c == '(') {
                    bracketCount++;
                } else if (c == ')') {
                    bracketCount--;
                }
            }
            if (bracketCount == 0) {
                String part = input.substring(start, matcher.start());
                parts.add(part);
                start = matcher.end();
            }
        }
        parts.add(input.substring(start));
        return parts.toArray(new String[0]);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        /*
          - "for(%old%<%new%)"
  - "  setVariable(%old%,%new%+1)"
  - "  setGermComponent(game_quick_bar,gif,'电脑画布.奶刀'+%old%,enable,true)"
  - "for(%old%>%new%)"
  - "  if(%old% <= 0)"
  - "    return"
  - "  setGermComponent(game_quick_bar,gif,'电脑画布.奶刀'+%old%,enable,false)"
  - "  setVariable(%old%,%old%-1)"
         */
//        list.add("for(%old%<%new%)");
//        list.add("  setVariable(%old%,%new%+1)");
//        list.add("for(%old%>%new%)");
//        list.add("  if(%old% <= 0)");
//        list.add("    return");
//        list.add("  setGermComponent(game_quick_bar,gif,'电脑画布.奶刀'+%old%,enable,false)");
//        list.add("  setVariable(%old%,%old%-1)");
        list.add("switch(getNormalAttackLayers())");
        list.add("  case 1 -> 0.8");
        list.add("  case 2 -> 0.9");
        list.add("  case 3:");
        list.add("    return 1.2");
        nowCommandCompiler.currentLoadClass = new ClassImpl("test");
        System.out.println(nowCommandCompiler.startParserFunction(list));
//        System.out.println(nowCommandCompiler.parsingStatement("if(getItemHand(nbt,'奶刀') == '蓝刀')", 0));
    }


    //  if(getMainHandItem(nbt,'形态') == '蓝刀')
    public AbstractCommand parsingStatement(String original, int depth) {
        if (depth < 0 || depth!=0 && original.charAt(depth * 2-1) != ' ') {
            System.out.println("缩进可能有误，错误指令为：{" + original + "}，缩进层数："+depth);
            return null;
        }
        original = original.trim();
        int i = original.indexOf("(");
        String commandType;
        String trim = original;
        String[] parameter;
        if(i == -1 && (i=original.indexOf(' '))==-1){
            //没有参数
            commandType = trim;
            parameter = new String[]{};
        }else {
            commandType = original.substring(0, i).trim();
            parameter = getParameter(original.substring(i+1,original.charAt(original.length()-1)==')' ? original.length()-1 : original.length()));
        }
        Class<? extends AbstractCommand> command = map.get(commandType);
//        AbstractCommand abstractCommand = parserSystemFunction(depth,commandType, parameter);
//        if(abstractCommand != null){
//            return abstractCommand;
//        }
        if(command == null){
            if(this.currentLoadClass.functionDefinitionName.contains(commandType)){
                CustomCommand customCommand = new CustomCommand(depth,commandType, parameter);
                customCommand.compile();
                return customCommand;
            }else{
                return null;
            }
//            if(!customFunctions.contains(commandType)){
//                System.out.println("无法识别指令类型： "+commandType);
//                return null;
//            }else{
//                return new CustomCommand(depth, parameter);
//            }
        }else{
            try {
                Constructor<?> constructor = command.getConstructor(int.class, String[].class);
                AbstractCommand abstractCommand = (AbstractCommand) constructor.newInstance(depth, parameter);
                abstractCommand.compile();
                return abstractCommand;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void registerStandardCommand(){
        registerCommands(CaseCommand.class,"case");
        registerCommands(DefaultCommand.class,"default");
        registerCommands(SwitchCommand.class,"switch");

        registerCommands(AsyncCommand.class,"async","to-async");
        registerCommands(AsyncForCommand.class,"async-for","asyncFor");
        registerCommands(ElseCommand.class,"else");
        registerCommands(ForCommand.class,"for");
        registerCommands(IfCommand.class,"if");

        registerCommands(BreakCommand.class,"break");
        registerCommands(DelayCommand.class,"delay");
        registerCommands(GoToCommand.class,"goto");
        registerCommands(ReturnCommand.class,"return");
        registerCommands(CancelCommand.class,"cancel");

        registerCommands(CmdOPCommand.class,"cmd-op");
        registerCommands(CmdConsoleCommand.class,"cmd-console");
        registerCommands(CmdSelfCommand.class,"cmd-self","cmd-player");

        registerCommands(NewVariableCommand.class,"newVariable","new-variable","new");
        registerCommands(SetVariableCommand.class,"setVariable","set-variable","set");

        registerCommands(SyncFunctionCommand.class,"sync","sync-run","sync-func","syncFunc","func");
        registerCommands(AsyncFunctionCommand.class,"async-run","async-func","asyncFunc");
        registerCommands(CalculateCommand.class,"calculate","cal");
        registerCommands(ChangeHandItemCommand.class,"change-hand-item","changeHandItem");
        registerCommands(GetHandItemCommand.class,"get-hand-item","getHandItem","getItemHand","get-item-hand");
        registerCommands(GetTimeCommand.class,"get-time","getTime");
        registerCommands(HasConeEntityCommand.class,"has-cone-entity","hasConeEntity");
        registerCommands(JavaScriptCommand.class,"js");
        registerCommands(MsgCommand.class,"msg");
        registerCommands(PrintlnCommand.class,"println");
        registerCommands(RemovePotionCommand.class,"remove-potion","removePotion");
        registerCommands(ReplaceCommand.class,"replace");
    }

    public List<AbstractCommand> startParserFunction(List<String> originalList) {
        return SemaphoreLock.submit("parser", 10000, () -> parserFunction(null,originalList, 0, 0, originalList.size()));
    }

    public List<AbstractCommand> parserFunction(List<AbstractCommand> compiledCommands,List<String> originalList, int depth, int startLine, int endLine) {
        if(compiledCommands == null){
            compiledCommands = new ArrayList<>();
        }
        for (int i = startLine; i < endLine; i++) {
            String original = originalList.get(i);
            int indentationLevel = getIndentationLevel(original);
            if (indentationLevel < depth) {
//                System.out.println("缩进与栈深度不匹配，无法解析语句：" + original);
                break;
            }
            if (indentationLevel > depth) {
                continue;
            }
            AbstractCommand abstractCommand = parsingStatement(original, depth);
            if (abstractCommand == null) {
                continue;
            }
            switch (abstractCommand.getType()) {
                case "switch":{
                    compiledCommands.add(abstractCommand);
                    parserFunction(compiledCommands,originalList, depth + 1, i + 1, endLine);
                    continue;
                }
                case "loop":
                case "if":
                case "async":
                case "for": {
                    List<AbstractCommand> parser = parserFunction(null,originalList, depth + 1, i + 1, endLine);
                    ((NestedCommand) abstractCommand).setNestedCommands(parser);
                    break;
                }
                case "case": {
                    List<AbstractCommand> parser = parserFunction(null,originalList, depth + 1, i + 1, endLine);
                    CaseCommand caseCommand = (CaseCommand) abstractCommand;
                    ((NestedCommand) abstractCommand).setNestedCommands(parser);
                    boolean isAdd = false;
                    for (int i1 = compiledCommands.size() - 1; i1 >= 0; i1--) {
                        AbstractCommand abstractCommandIter = compiledCommands.get(i1);
                        if (abstractCommandIter.getType().equals("switch")) {
                            ((SwitchCommand) abstractCommandIter).caseMap.put(caseCommand.getCondition(), caseCommand);
                            isAdd = true;
                            break;
                        }
                    }
                    if (!isAdd) {
                        System.out.println("case语句无法与任何一个switch语句对应！");
                    }
                    continue;
                }
                case "default": {
                    List<AbstractCommand> parser = parserFunction(null,originalList, depth + 1, i + 1, endLine);
                    ((NestedCommand) abstractCommand).setNestedCommands(parser);
                    boolean isAdd = false;
                    for (int i1 = compiledCommands.size() - 1; i1 >= 0; i1--) {
                        AbstractCommand abstractCommandIter = compiledCommands.get(i1);
                        if (abstractCommandIter.getType().equals("switch")) {
                            ((SwitchCommand) abstractCommandIter).defaultCommand = (DefaultCommand) abstractCommand;
                            isAdd = true;
                            break;
                        }
                    }
                    if (!isAdd) {
                        System.out.println("else语句无法与任何一个if语句对应！");
                    }
                    continue;
                }
                case "else": {
                    List<AbstractCommand> parser = parserFunction(null,originalList, depth + 1, i + 1, endLine);
                    ((NestedCommand) abstractCommand).setNestedCommands(parser);
                    boolean isAdd = false;
                    for (int i1 = compiledCommands.size() - 1; i1 >= 0; i1--) {
                        AbstractCommand abstractCommandIter = compiledCommands.get(i1);
                        if (abstractCommandIter.getType().equals("if")) {
                            ((IfCommand) abstractCommandIter).setElseCommand((ElseCommand) abstractCommand);
                            isAdd = true;
                            break;
                        }
                    }
                    if (!isAdd) {
                        System.out.println("else语句无法与任何一个if语句对应！");
                    }
                    continue;
                }
            }
            AoitoriScriptLoadEvent aoitoriScriptLoadEvent = new AoitoriScriptLoadEvent();
            aoitoriScriptLoadEvent.setOriginalCommand(original);
            aoitoriScriptLoadEvent.setCommandCompiler(this);
            aoitoriScriptLoadEvent.setDepth(depth);
            aoitoriScriptLoadEvent.setStartLine(startLine);
            aoitoriScriptLoadEvent.setEndLine(endLine);
            aoitoriScriptLoadEvent.setOriginalList(originalList);
            aoitoriScriptLoadEvent.setCompiledCommands(compiledCommands);
//            Bukkit.getPluginManager().callEvent(aoitoriScriptLoadEvent);
            AbstractCommand currentCommand = aoitoriScriptLoadEvent.getCurrentCommand();
            depth = aoitoriScriptLoadEvent.getDepth();
            startLine = aoitoriScriptLoadEvent.getStartLine();
            endLine = aoitoriScriptLoadEvent.getEndLine();
            originalList = aoitoriScriptLoadEvent.getOriginalList();
            compiledCommands = aoitoriScriptLoadEvent.getCompiledCommands();
            if(currentCommand == null){
                compiledCommands.add(abstractCommand);
            }else{
                compiledCommands.add(currentCommand);
            }
        }
        return compiledCommands;
    }

    public int getIndentationLevel(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count / 2;
    }
}
