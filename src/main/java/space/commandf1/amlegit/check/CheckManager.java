package space.commandf1.amlegit.check;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.Check;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author commandf1
 */
public class CheckManager {
    private static final Map<Plugin, CheckManager> MANAGERS = new ConcurrentHashMap<>();

    private final Map<String, Check> checks = new ConcurrentHashMap<>();

    @Getter
    private final Plugin plugin;

    private CheckManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerChecks(Check... checks) {
        Arrays.stream(checks).forEach(this::registerCheck);
    }

    public Check[] getChecks() {
        return checks.values().toArray(new Check[0]);
    }

    public void registerCheck(Check check) {
        checks.put(check.getName() + check.getType(), check);
    }

    public static CheckManager getManager(Plugin plugin) {
        CheckManager toReturn = MANAGERS.get(plugin);
        if (toReturn == null) {
            toReturn = new CheckManager(plugin);
        }

        MANAGERS.put(plugin, toReturn);

        return toReturn;
    }
}
