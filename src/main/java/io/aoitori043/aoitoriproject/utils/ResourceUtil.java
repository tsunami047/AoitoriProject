package io.aoitori043.aoitoriproject.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author: natsumi
 * @CreateTime: 2023-06-16  23:21
 * @Description: ?
 */
public class ResourceUtil {

    /**
     * @param section
     * @return LinkedHashMap<String>
     * @date 2023/8/1 1:05
     * @description section转map
     */
    public static LinkedHashMap<String, String> getMapFromConfigurationSection(ConfigurationSection section) {
        LinkedHashMap<String, String> value = new LinkedHashMap<>();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            value.put(key, section.getString(key));
        }
        return value;
    }

    public static void saveResourceBetter(JavaPlugin javaPlugin, String path) {
        try {
            File file = (File) ReflectionUtil.getPrivateAndSuperField(javaPlugin, "file");
            ResourceUtil.extractFolderFromJar(javaPlugin, file.getPath(), path, new File(javaPlugin.getDataFolder(), path).getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processFiles(File dir, Consumer<File> fileProcessor) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processFiles(file, fileProcessor);
                } else {
                    fileProcessor.accept(file);
                }
            }
        }
    }

    /**
     * @param tIntensify
     * @param jarFilePath
     * @param folderPath
     * @param destinationPath
     * @date 2023/6/18 12:33
     * @description 输出jar包的文件夹
     */
    public static void extractFolderFromJar(JavaPlugin tIntensify, String jarFilePath, String folderPath, String destinationPath) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath);
        File destinationFolder = new File(destinationPath);

        if (destinationFolder.exists()) {
            tIntensify.getLogger().info("folder already exists. Skipping extraction.");
        } else {
            destinationFolder.mkdirs();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(folderPath + "/")) {
                    String relativePath = entryName.substring(folderPath.length() + 1);
                    if (entry.isDirectory()) {
                        File dir = new File(destinationFolder, relativePath);
                        dir.mkdirs();
                    } else {
                        File file = new File(destinationFolder, relativePath);
                        InputStream inputStream = jarFile.getInputStream(entry);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                    }
                }
            }

            tIntensify.getLogger().info("Folder extracted successfully.");
        }

        jarFile.close();
    }
}
