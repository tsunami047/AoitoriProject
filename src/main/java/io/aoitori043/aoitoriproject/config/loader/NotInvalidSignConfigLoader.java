package io.aoitori043.aoitoriproject.config.loader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-17  20:11
 * @Description: ?
 */
public class NotInvalidSignConfigLoader extends YamlConfiguration {

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
        NotInvalidSignConfigLoader notInvalidSignConfig = new NotInvalidSignConfigLoader();
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
            if (contents.contains("http")){
                super.loadFromString(contents);
            }else{
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < contents.length(); i++) {
                    char current = contents.charAt(i);
                    if (current == '&' && (i == 0 || contents.charAt(i - 1) != '&') &&
                            (i == contents.length() - 1 || !Character.isDigit(contents.charAt(i + 1)) &&
                                    "abcdef".indexOf(Character.toLowerCase(contents.charAt(i + 1))) != -1)) {
                        result.append('§');
                    } else {
                        result.append(current);
                    }
                }
                super.loadFromString(result.toString());
            }
    }


}
