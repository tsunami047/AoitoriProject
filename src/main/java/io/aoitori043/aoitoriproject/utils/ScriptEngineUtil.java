package io.aoitori043.aoitoriproject.utils;

import io.aoitori043.aoitoriproject.AoitoriProject;

import javax.script.*;
import java.util.Map;


/**
 * @Author: natsumi
 * @CreateTime: 2024-09-22  22:24
 * @Description: ?
 */
public class ScriptEngineUtil {

    private static final ScriptEngine engine;
    private static final ScriptEngineManager engineManager;

    static{
        engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("nashorn");
        if (engine == null) {
            throw new IllegalArgumentException("Script engine not found for: " + "nashorn");
        }
    }


    public static Object executeScript(String script) {
        try {
            if (script == null) {
                return null;
            }
            return engine.eval(script);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Unable to parse expression： "+script);
        }
    }

    public static Object executeScript(String script, Map<String, Object> bindings) throws ScriptException {
        Bindings scriptBindings = createBindings(bindings);
        return engine.eval(script, scriptBindings);
    }

    private static Bindings createBindings(Map<String, Object> variables) {
        Bindings bindings = engine.createBindings();
        bindings.putAll(variables);
        return bindings;
    }

    public static void setGlobalVariable(String key, Object value) {
        engine.put(key, value);
    }

    public static Object getGlobalVariable(String key) {
        return engine.get(key);
    }

    public static Object executeScriptFromFile(String filePath) throws Exception {
        try (java.io.FileReader reader = new java.io.FileReader(filePath)) {
            return engine.eval(reader);
        }
    }

    public static void addFunction(String functionName, String functionBody) throws ScriptException {
        String script = "function " + functionName + " " + functionBody;
        engine.eval(script);
    }

    public static Object invokeFunction(String functionName, Object... args) throws NoSuchMethodException, ScriptException {
        if (engine instanceof Invocable) {
            Invocable invocable = (Invocable) engine;
            return invocable.invokeFunction(functionName, args);
        } else {
            throw new UnsupportedOperationException("ScriptEngine does not support invoking functions");
        }
    }

    public static boolean isInvocable() {
        return engine instanceof Invocable;
    }

    public static boolean isCompilable() {
        return engine instanceof Compilable;
    }

    public static CompiledScript compileScript(String script) throws ScriptException {
        if (isCompilable()) {
            Compilable compilable = (Compilable) engine;
            return compilable.compile(script);
        } else {
            throw new UnsupportedOperationException("ScriptEngine does not support compilation");
        }
    }

    public static String getEngineName() {
        return engine.getFactory().getEngineName();
    }

    public static String getLanguageName() {
        return engine.getFactory().getLanguageName();
    }

    public static String getEngineVersion() {
        return engine.getFactory().getEngineVersion();
    }

    public static String getLanguageVersion() {
        return engine.getFactory().getLanguageVersion();
    }

    public static void listAvailableEngines() {
        ScriptEngineManager manager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            System.out.println("Engine Name: " + factory.getEngineName());
            System.out.println("Engine Version: " + factory.getEngineVersion());
            System.out.println("Language: " + factory.getLanguageName());
            System.out.println("Language Version: " + factory.getLanguageVersion());
            System.out.println("Extensions: " + factory.getExtensions());
            System.out.println("Mime Types: " + factory.getMimeTypes());
            System.out.println("Names: " + factory.getNames());
            System.out.println("-----------------------------------------");
        }
    }

}
