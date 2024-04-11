package io.aoitori043.aoitoriproject.config.loader;

import io.aoitori043.aoitoriproject.config.FileLoader;
import io.aoitori043.aoitoriproject.config.InjectYaml;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static io.aoitori043.aoitoriproject.config.impl.ConfigMapping.isStaticField;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-17  20:11
 * @Description: ?
 */
public class NotInvalidSignConfig extends YamlConfiguration {

    public static void fillInYamlConfiguration(JavaPlugin plugin, Object object){
        for (Field field : object.getClass().getFields()) {
            try {
                field.setAccessible(true);
                if (field.isAnnotationPresent(InjectYaml.class)) {
                    InjectYaml annotation = field.getAnnotation(InjectYaml.class);
                    String path = annotation.path();
                    YamlConfiguration yamlConfiguration = FileLoader.releaseAndLoadFile(plugin,path + ".yml");
                    field.set(isStaticField(field)?null:object,yamlConfiguration);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String readFileToString(File file) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(java.nio.file.Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static YamlConfiguration loadNotInvalidSignConfig(File file){
        String s = readFileToString(file);
        NotInvalidSignConfig notInvalidSignConfig = new NotInvalidSignConfig();
        try {
            notInvalidSignConfig.loadFromString(s);
        }catch (Exception e){
            System.out.println("-------------------------------------------");
            System.out.println("以下问题出自："+file.getAbsolutePath());
            e.printStackTrace();
            System.out.println("--------------------------------------------");
        }
        return notInvalidSignConfig;
    }

    @Override
    public void loadFromString(String contents) throws InvalidConfigurationException {
            super.loadFromString(contents.replaceAll("&", "§").replaceAll("'","\""));
    }

    public static NotInvalidSignConfig getEmptyFile() throws InvalidConfigurationException {
        NotInvalidSignConfig notInvalidSignConfig = new NotInvalidSignConfig();

        notInvalidSignConfig.loadFromString("");
        return notInvalidSignConfig;
    }

    public static NotInvalidSignConfig getNewMMEmptyFile() throws InvalidConfigurationException {
        NotInvalidSignConfig notInvalidSignConfig = new NotInvalidSignConfig();
        notInvalidSignConfig.loadFromString("");
        return notInvalidSignConfig;
    }
}
