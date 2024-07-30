package io.aoitori043.aoitoriproject.script;

import io.aoitori043.aoitoriproject.script.event.EventWrapper;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  21:59
 * @Description: ?
 */
@Data
public class ClassImpl {

    public HashSet<String> functionDefinitionName;

    public String name;
    public ConcurrentHashMap<String, PlayerDataAccessor.VariablesAttribute> variables;
    public LinkedHashMap<String, EventWrapper> events;
    public LinkedHashMap<String, PlayerDataAccessor.Function> functions;

    public ClassImpl(String name) {
        this.functionDefinitionName = new HashSet<>();
        this.name = name;
        variables = new ConcurrentHashMap<>();
        events = new LinkedHashMap<>();
        functions = new LinkedHashMap<>();
    }

    public void addDefinitionFunctionName(String functionName){
        functionDefinitionName.add(functionName);
    }

    public void addVariable(String varName,PlayerDataAccessor.VariablesAttribute variablesAttribute) {
        variables.put(varName,variablesAttribute);
    }

    public void addEvents(String originalSign,List<AbstractCommand> abstractCommands){
        int bracketsIndex = originalSign.lastIndexOf("(");
        EventWrapper eventWrapper = new EventWrapper();
        if(bracketsIndex == -1){
            wrapperExtracted(originalSign, eventWrapper);
        }else{
            String equalLeftChars = originalSign.substring(0,bracketsIndex);
            wrapperExtracted(equalLeftChars, eventWrapper);
            String equalRightChars = originalSign.substring(bracketsIndex+1,originalSign.length()-1);
            if(equalRightChars.isEmpty()){
                eventWrapper.setParameters(new String[]{});
            }else {
                eventWrapper.setParameters(equalRightChars.split(","));
            }
        }
        eventWrapper.setCommands(abstractCommands);
        events.put(originalSign.substring(0, bracketsIndex),eventWrapper);
    }

    private static void wrapperExtracted(String originalSign, EventWrapper eventWrapper) {
        int checkSignIndex = originalSign.indexOf("$");
        if(checkSignIndex==-1){
            eventWrapper.setEventName(originalSign);
        }else{
            eventWrapper.setEventName(originalSign.substring(0,checkSignIndex));
            eventWrapper.setCheckSign(originalSign.substring(checkSignIndex+1));
        }
    }

    public void addFunction(String functionName,PlayerDataAccessor.Function function) {
        functions.put(functionName,function);
        List<String> parameters = function.getParameters();
        if(parameters.size() == 1 && parameters.get(0).isEmpty()){
            function.setParameters(new ArrayList<>());
        }
    }


}
