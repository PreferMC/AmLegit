package space.commandf1.amlegit.config.ai;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.ai.AIData;
import space.commandf1.amlegit.config.ConfigHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIDataConfig extends ConfigHandler {
    private static final Map<String, AIDataConfig> aiDataConfigs = new HashMap<>();

    @Getter
    private final String checkName;

    public AIDataConfig(Plugin plugin, String playerName, String checkName, List<AIData> aiData) {
        this(plugin, playerName, checkName);
        List<AIData> existedData = this.getAIData(this.getCheckName());
        existedData.addAll(aiData);
        this.getConfig().set(this.getCheckName(), existedData);
        this.saveConfig();
    }

    private AIDataConfig(Plugin plugin, String playerName, String checkName) {
        super(plugin, new File(plugin.getDataFolder(), "ai-data"), playerName);
        this.checkName = checkName;
        aiDataConfigs.put(playerName, this);
    }

    public static AIDataConfig of(Plugin plugin, File configFile) {
        String playerName = configFile.getName().replace(".yml", "");
        return new AIDataConfig(plugin, playerName, playerName);
    }

    public List<AIData> getAIData(String checkName) {
        List<?> rawList = this.getConfig().getList(checkName, new ArrayList<>());
        List<AIData> result = new ArrayList<>();

        for (Object item : rawList) {
            if (item instanceof AIData) {
                result.add((AIData) item);
            }
        }
        return result;
    }

    public static AIDataConfig getConfig(String playerName) {
        return aiDataConfigs.get(playerName);
    }
}
