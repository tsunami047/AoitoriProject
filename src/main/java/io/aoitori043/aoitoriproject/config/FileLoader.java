package io.aoitori043.aoitoriproject.config;

import io.aoitori043.aoitoriproject.ReflectionUtil;
import io.aoitori043.aoitoriproject.config.loader.NotInvalidSignConfigLoader;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @Author: natsumi
 * @CreateTime: 2024-03-25  00:28
 * @Description: ?
 */
public class FileLoader {

    public static void processFiles(JavaPlugin javaPlugin,String dirName, Consumer<File> fileProcessor){
        File file = new File(javaPlugin.getDataFolder(), dirName);
        processFiles(file,fileProcessor);
    }

    static void processFiles(File dir, Consumer<File> fileProcessor) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {

                if (file.isDirectory()) {
                    processFiles(file, fileProcessor);
                } else {
                    if (file.getName().toLowerCase().endsWith(".yml")) {
                        fileProcessor.accept(file);
                    }
                }
            }
        }
    }

    public static YamlConfiguration releaseAndLoadFile(JavaPlugin plugin,String path){
        File configFile = new File(plugin.getDataFolder(), path);
        if (!configFile.exists()) {
            plugin.saveResource(path, false);
        }
        return NotInvalidSignConfigLoader.loadConfiguration(configFile);
    }

    public static void extractFolder(JavaPlugin javaPlugin,String path){
        try {
            File file = (File) ReflectionUtil.getPrivateAndSuperField(javaPlugin, "file");
            extractFolderFromJar(javaPlugin, file.getPath(), path, new File(javaPlugin.getDataFolder(), path).getPath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void extractFolderFromJar(JavaPlugin plugin, String jarFilePath, String folderPath, String destinationPath) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath);
        File destinationFolder = new File(destinationPath);
        if (destinationFolder.exists()) {
            plugin.getLogger().info( destinationPath+" 文件夹已经存在，跳过释放文件");
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
            plugin.getLogger().info(destinationPath+" 文件夹释放成功！");
        }
        jarFile.close();
    }



}
