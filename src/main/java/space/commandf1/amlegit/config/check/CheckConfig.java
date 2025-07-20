package space.commandf1.amlegit.config.check;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.CheckManager;
import space.commandf1.amlegit.check.action.ActionHandler;
import space.commandf1.amlegit.config.ConfigHandler;
import space.commandf1.amlegit.exception.InvalidCheckClassException;

import java.lang.reflect.Field;
import java.util.*;

public class CheckConfig extends ConfigHandler {
    private static final Map<Plugin, CheckConfig> checkConfigs = new HashMap<>();

    private final CheckManager checkManager;

    private final Map<Check, Map<String, CheckConfigHolder<?>>> checkConfigHolders = new HashMap<>();

    public CheckConfig(Plugin plugin, CheckManager checkManager) {
        super(plugin, "checks");
        checkConfigs.put(plugin, this);
        this.checkManager = checkManager;
        this.init();
    }

    public static CheckConfig getConfig(Plugin plugin) {
        return checkConfigs.get(plugin);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        checkConfigHolders.clear();
        this.init();
    }

    public CheckConfigHolder<?> getCheckConfigHolder(Check check, String name) {
        return checkConfigHolders.get(check).get(name);
    }

    private long getLongDefault(String path, long defaultValue) {
        long value;
        if (this.getConfig().isLong(path)) {
            value = this.getConfig().getLong(path);
        } else {
            this.getConfig().set(path, defaultValue);
            value = defaultValue;
        }

        return value;
    }

    private Object getDefault(String path, Object defaultValue) {
        Object value = this.getConfig().get(path);
        if (value == null) {
            this.getConfig().set(path, defaultValue);
            value = defaultValue;
        }

        return value;
    }

    @SuppressWarnings({"unchecked", "ExtractMethodRecommender"})
    private void init() {
        /*
        * Format:
        *
        * checkName:
        *    Type:
        *         enable: true
        *         maxVl: vl
        *         setback: true
        *         commands: []
        *
        * */
        for (Check check : checkManager.getChecks()) {
            Map<String, CheckConfigHolder<?>> stringCheckConfigHolderMap = checkConfigHolders.get(check);
            if (stringCheckConfigHolderMap == null) {
                stringCheckConfigHolderMap = new HashMap<>();
            }

            String prefix = check.getName() + "." + check.getType() + ".";
            Class<? extends Check> checkClass = check.getClass();

            /* to get existed values */
            boolean enableValue = (boolean) this.getDefault(prefix + "enable",
                    checkClass.getAnnotation(DefaultDisableCheck.class) == null);

            long maxVLValue = this.getLongDefault(prefix + "maxVL", check.getDefaultMaxVL());

            List<String> commandsValue = (List<String>) this.getDefault(prefix + "commands", new ArrayList<>());

            /* let's start a enable config holder */
            CheckConfigHolder<Boolean> enableConfig = new CheckConfigHolder<>("enable", enableValue);
            stringCheckConfigHolderMap.put("enable", enableConfig);
            CheckConfigHolder<Long> maxVLConfig = new CheckConfigHolder<>("maxVL", maxVLValue);
            stringCheckConfigHolderMap.put("maxVL", maxVLConfig);
            CheckConfigHolder<ActionHandler> commandsConfig = new CheckConfigHolder<>("commands",
                    new ActionHandler.Builder().executor(Bukkit.getConsoleSender()).commands(commandsValue).build());
            stringCheckConfigHolderMap.put("commands", commandsConfig);

            if (check.isSetbackable()) {
                boolean setback = (boolean) this.getDefault(prefix + "setback", true);
                CheckConfigHolder<Boolean> setbackConfig = new CheckConfigHolder<>("setback", setback);
                stringCheckConfigHolderMap.put("setback", setbackConfig);
            }

            Map<String, CheckConfigHolder<?>> finalStringCheckConfigHolderMap = stringCheckConfigHolderMap;
            ArrayList<Field> fields = new ArrayList<>(List.of(checkClass.getDeclaredFields()));
            Class<?> superclass = checkClass.getSuperclass();
            if (superclass != null) {
                fields.addAll(List.of(superclass.getDeclaredFields()));
            }
            fields.stream()
                    .filter(field -> field.getAnnotation(CheckConfigHandler.class) != null)
                    .forEach(checkConfigField -> {
                        checkConfigField.setAccessible(true);
                        CheckConfigHandler annotation = checkConfigField.getAnnotation(CheckConfigHandler.class);
                        CheckConfigHolder<Object> objectCheckConfigHolder = new CheckConfigHolder<>(annotation.name());
                        Object defaultValue;
                        try {
                            defaultValue = checkConfigField.get(check);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                        if (defaultValue == null) {
                            throw new InvalidCheckClassException("The field " +
                                    checkConfigField.getName() + " has a null default value");
                        }

                        Object value = this.getDefault(prefix + annotation.name(), defaultValue);
                        if (!value.equals(defaultValue)) {
                            try {
                                checkConfigField.set(check, value);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }

                        objectCheckConfigHolder.setValue(value);

                        finalStringCheckConfigHolderMap.put(annotation.name(), objectCheckConfigHolder);
                    });

            checkConfigHolders.put(check, stringCheckConfigHolderMap);
        }

        this.saveConfig();
    }
}
