package space.commandf1.amlegit.check;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CheckManager {
    private static final Map<Plugin, CheckManager> managers = new HashMap<>();

    private final Map<String, Check> checks = new HashMap<>();

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
        checks.put(check.getName(), check);
    }

    public static CheckManager getManager(Plugin plugin) {
        CheckManager toReturn = managers.get(plugin);
        if (toReturn == null) {
            toReturn = new CheckManager(plugin);
        }

        managers.put(plugin, toReturn);

        return toReturn;
    }
}
