package space.commandf1.amlegit.config.settings;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.config.ConfigHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsConfig extends ConfigHandler {
    private static final Map<Plugin, SettingsConfig> settingsConfigs = new ConcurrentHashMap<>();

    public SettingsConfig(Plugin plugin) {
        super(plugin, "settings");
        settingsConfigs.put(plugin, this);
        this.init();
    }

    public static SettingsConfig getConfig(Plugin plugin) {
        return settingsConfigs.get(plugin);
    }

    private void init() {
        for (Configs config : Configs.values()) {
            if (!this.getConfig().isSet(config.getPath())) {
                this.getConfig().set(config.getPath(), config.defaultValue);
            }
        }
        this.saveConfig();
    }

    public Object getValue(Configs config) {
        return this.getConfig().get(config.getPath());
    }

    public <T> T getValue(Configs config, Class<T> type) {
        Object object = this.getConfig().get(config.getPath());
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        return null;
    }

    public String getStringValue(Configs config) {
        return this.getConfig().getString(config.getPath());
    }

    public boolean getBooleanValue(Configs config) {
        return this.getConfig().getBoolean(config.getPath());
    }

    @Getter
    public enum Configs {
        PREFIX("prefix", "&b「&3AmLegit&b」» &7"),
        ALERT_FORMAT("alert.format", "&b%player% &7flagged &b%check%&e%type% &f%bar% &7(&bping=%ping%&7, &btps=%tps%&7&7, &bvl=%vl%/%maxvl%&7)"),
        ALERT_TO_CONSOLE("alert.print-to-console", true),
        CHECK_DETECT_OPS("check.detect-ops", true);
        private final Object defaultValue;
        private final String path;

        Configs(String path, Object defaultValue) {
            this.defaultValue = defaultValue;
            this.path = path;
        }
    }
}
