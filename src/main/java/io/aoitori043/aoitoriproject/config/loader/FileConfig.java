package io.aoitori043.aoitoriproject.config.loader;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.logging.Level;


/**
 * FileConfig class
 *
 * @author elixir047
 * @date 2016/10/31
 */
public class FileConfig extends YamlConfiguration {
    private static FileConfiguration config;
    public final DumperOptions yamlOptions;
    private final Representer yamlRepresenter;
    private final Yaml yaml;

    private FileConfig() {
        this.yamlOptions = new DumperOptions();
        this.yamlRepresenter = new YamlRepresenter();
        this.yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
    }

    public FileConfig(File file) {
        this.yamlOptions = new DumperOptions();
        this.yamlRepresenter = new YamlRepresenter();
        this.yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);
        config = loadConfiguration(file);
    }

    public static YamlConfiguration loadConfiguration(File file) {
        Validate.notNull(file, "InjectFile cannot be null");
        YamlConfiguration config = new FileConfig();
        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {

        } catch (IOException | InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
        }
        return config;
    }

    public static void init(File file) {
        config = loadConfiguration(file);
    }

    @Override
    public void save(File file) throws IOException {
        Validate.notNull(file, "InjectFile cannot be null");
        Files.createParentDirs(file);
        String data = saveToString();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            writer.write(data);
        }
    }

    @Override
    public String saveToString() {
        this.yamlOptions.setIndent(options().indent());
        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String header = buildHeader();
        String dump = this.yaml.dump(getValues(false));
        if ("{}\n".equals(dump)) {
            dump = "";
        }
        return header + dump;
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "InjectFile cannot be null");
        FileInputStream stream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(stream, Charsets.UTF_8);
        load(isr);
        isr.close();
    }
}
