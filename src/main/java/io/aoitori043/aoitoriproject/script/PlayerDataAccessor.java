package io.aoitori043.aoitoriproject.script;

import io.aoitori043.aoitoriproject.script.event.EventWrapper;
import io.aoitori043.aoitoriproject.script.event.VariableUpdateEvent;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author: natsumi
 * @CreateTime: 2024-07-05  21:25
 * @Description: ?
 */
@Data
public class PlayerDataAccessor {

    public Player player;
    public ConcurrentHashMap<String, VariablesAttribute> variables;
    public LinkedHashMap<String,LinkedHashMap<String,EventWrapper>> events;
    public LinkedHashMap<String,Function> functions;
    public LinkedHashMap<String,ClassImpl> classImplMap;

    @Data
    public static class Function{
        public String name;
        public List<AbstractCommand> commands;
        public List<String> parameters;
    }

    public boolean hasVariable(String name){
        return variables.containsKey(name);
    }

    public PlayerDataAccessor(Player player) {
        this.player = player;
        this.variables = new ConcurrentHashMap<>();
        this.events = new LinkedHashMap<>();
        this.functions = new LinkedHashMap<>();
        this.classImplMap = new LinkedHashMap<>();
    }


    public void addVariable(VariablesAttribute variablesAttribute){
        variables.put(variablesAttribute.getVarName(),variablesAttribute);
    }

    public void addClassImpl(ClassImpl classImpl){
        this.classImplMap.put(classImpl.name, classImpl);
        for (Map.Entry<String, VariablesAttribute> entry : classImpl.variables.entrySet()) {
            String varName = entry.getKey();
            VariablesAttribute variablesAttribute = entry.getValue();
            VariablesAttribute clone = variablesAttribute.clone();
            variables.put(varName, clone);
        }
        for (Map.Entry<String, EventWrapper> entry : classImpl.events.entrySet()) {
            String eventName = entry.getKey();
            LinkedHashMap<String, EventWrapper> classImplEventMap = events.computeIfAbsent(eventName, k -> new LinkedHashMap<>());
            classImplEventMap.put(classImpl.name, entry.getValue());
        }
        this.functions.putAll(classImpl.functions);
    }

    public void removeClassImpl(String classImplName){
        ClassImpl aClass = this.classImplMap.get(classImplName);
        for (String s : aClass.getEvents().keySet()) {
            for (Map.Entry<String, LinkedHashMap<String, EventWrapper>> entry : this.events.entrySet()) {
                entry.getValue().remove(classImplName);
            }
        }
        for (String s : aClass.variables.keySet()) {
            this.variables.remove(s);
        }
        for (String s : aClass.getFunctions().keySet()) {
            this.functions.remove(s);
        }
    }

    public Function getFunction(String functionName){
        return functions.get(functionName);
    }


    public Collection<EventWrapper> getEvent(String eventName){
        LinkedHashMap<String, EventWrapper> stringEventWrapperLinkedHashMap = events.get(eventName);
        if(stringEventWrapperLinkedHashMap!=null){
            return stringEventWrapperLinkedHashMap.values();
        }
        return Collections.EMPTY_LIST;
    }

    public Object getValue(String varName) {
        VariablesAttribute variablesAttribute = variables.get(varName);
        if(variablesAttribute == null){
            return null;
        }
        return variablesAttribute.value;
    }

    public void setValue(String varName, Object value) {
        String name = player.getName();
        VariablesAttribute variablesAttribute = variables.get(varName);
        if(variablesAttribute == null){
            throw new NullPointerException(name+" 玩家不存在临时变量: "+value);
        }
        Object newValue;
        switch (variablesAttribute.type) {
            case BOOLEAN:
                newValue = Boolean.valueOf(value.toString());
                break;
            case INT:
                newValue = Integer.valueOf(value.toString());
                break;
            case LONG:
                newValue = Long.valueOf(value.toString());
                break;
            case STRING:
                newValue = value.toString();
                break;
            case DOUBLE:
                newValue = Double.valueOf(value.toString());
                break;
            default:{
                return;
            }
        }
        callVariableUpdateEvent(varName, variablesAttribute, newValue);
    }

    public void addValue(String varName, Object value) {
        String name = player.getName();
        VariablesAttribute variablesAttribute = variables.get(varName);
        if(variablesAttribute == null){
            throw new NullPointerException(name+" 玩家不存在临时变量: "+value);
        }
        switch (variablesAttribute.type) {
            case INT: {
                int result = (int)variablesAttribute.getValue() + Integer.parseInt(value.toString());
                callVariableUpdateEvent(varName, variablesAttribute, result);
                break;
            }
            case LONG:{
                long result = (long)variablesAttribute.getValue() + Long.parseLong(value.toString());
                callVariableUpdateEvent(varName, variablesAttribute, result);
                break;
            }
            case DOUBLE: {
                double result = (double)variablesAttribute.getValue() + Double.parseDouble(value.toString());
                callVariableUpdateEvent(varName, variablesAttribute, result);
                break;
            }
        }
    }

    private void callVariableUpdateEvent(String varName, VariablesAttribute variablesAttribute, Object result) {
        VariableUpdateEvent.VariableUpdateEventResult eventResult = (VariableUpdateEvent.VariableUpdateEventResult) VariableUpdateEvent.call(this, new ConcurrentHashMap<>(), varName, variablesAttribute.getValue(), result);
        if (eventResult.isCancel) {
            return;
        }
        if(eventResult.getNewVariable()!=null){
            variablesAttribute.value = eventResult.getNewVariable();
        }else {
            variablesAttribute.value = result;
        }
    }

    public void cleanPlayerVariables() {
        variables.clear();
    }


    public enum VariableType {
        INT, DOUBLE, STRING, BOOLEAN,LONG
    }

    @Data

    public static class VariablesAttribute {
        VariableType type;
        String varName;
        Object initValue;
        Object value;

        public boolean load(String define){
            String[] split = define.split(" ");
            try{
                type = VariableType.valueOf(split[0].toUpperCase());
            }catch (Exception e){
                System.out.println("不存在变量类型： "+split[0]);
                return false;
            }
            varName = split[1];
            switch (type) {
                case DOUBLE:
                    initValue = Double.valueOf(split[2]);
                    break;
                case INT:
                    initValue = Integer.valueOf(split[2]);
                    break;
                case STRING:
                    initValue = split[2];
                    break;
                case LONG:
                    initValue = Long.valueOf(split[2]);
                    break;
                case BOOLEAN:
                    initValue = Boolean.valueOf(split[2]);
                    break;
            }
            value = initValue;
            return true;
        }

        protected VariablesAttribute clone() {
            VariablesAttribute variablesAttribute = new VariablesAttribute();
            variablesAttribute.setType(type);
            variablesAttribute.setVarName(varName);
            variablesAttribute.setInitValue(initValue);
            variablesAttribute.setValue(value);
            return variablesAttribute;
        }
    }



}
