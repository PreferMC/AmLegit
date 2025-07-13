package space.commandf1.amlegit.config;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigHandler {
    private final List<String> header = new ArrayList<>();

    @Getter
    private final Plugin plugin;

    @Getter
    private final String name;

    private final File configFile;

    @Getter
    private final YamlConfiguration config;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public ConfigHandler(Plugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        this.configFile = new File(dataFolder.getPath() + File.separator + this.getName() + ".yml");

        if (this.configFile.exists()) {
            this.config = YamlConfiguration.loadConfiguration(this.configFile);
        } else {
            this.config =  new YamlConfiguration();
        }
    }

    public final void setHeader(String[] header) {
        this.header.clear();
        this.header.addAll(Arrays.asList(header));
    }

    @SneakyThrows
    public void reloadConfig() {
        if (configFile.exists()) {
            this.config.load(configFile);
        } else {
            this.config.loadFromString("");
        }
    }

    @SneakyThrows
    public void saveConfig() {
        if (!this.header.isEmpty()) {
            this.config.options().header(StringUtil.asString(this.header));
        }
        this.config.save(configFile);
    }

}
