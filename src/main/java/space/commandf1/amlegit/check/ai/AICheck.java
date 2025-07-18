package space.commandf1.amlegit.check.ai;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.config.ai.AIDataConfig;
import space.commandf1.amlegit.config.check.CheckConfigHandler;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * How do AI checks work?
 * Here are the steps:
 * 1. Collection
 * We do collect data from player, ready for the following checks
 * 2. Requestion
 * We do request for the check to detect whether the player is cheating
 * 3. Detection
 * We decide whether to flag or not when we get the result.
 * */
public abstract class AICheck extends Check {

    @CheckConfigHandler(name = "server-url")
    private String serverURL = "http://127.0.0.1:14511/prefict";

    @CheckConfigHandler(name = "token")
    private String token = "null";

    @CheckConfigHandler(name = "collectable")
    private boolean collectable = true;

    @Getter
    @Setter
    private long minAmountToRequest;

    public AICheck(String name, long defaultMaxVL, String description, String type,
                   long minAmountToRequest, Plugin plugin) {
        super(name, defaultMaxVL, description, type, plugin);
        this.minAmountToRequest = minAmountToRequest;
    }

    private final Map<AIData, CheckHandler> aiDataQueue = new ConcurrentHashMap<>();

    @Override
    public final void onCheck(final CheckHandler handler) {
        AIData collection = this.collect(handler);
        aiDataQueue.put(collection, handler);

        List<AIData> aiDataOfCurrentHandler = aiDataQueue.keySet()
                .stream()
                .filter(aiData -> aiDataQueue.get(aiData).equals(handler))
                .toList();

        if (aiDataOfCurrentHandler.size() >= this.minAmountToRequest) {
            if (collectable) {
                this.saveCollectedData(aiDataOfCurrentHandler, handler);
            }
            this.request(aiDataOfCurrentHandler);
            aiDataOfCurrentHandler.forEach(this.aiDataQueue.keySet()::remove);
        }
    }

    private void saveCollectedData(List<AIData> aiData, CheckHandler handler) {
        new AIDataConfig(this.getPlugin(), handler.getPlayerData().getPlayer().getName(),
                this.getName() + this.getType(), aiData);
    }

    @SneakyThrows
    /*
     * TODO:
     * 完善请求与接受
     * 需要用到类: AICheckResult
     * */
    private void request(List<AIData> aiData) {
        URL url = new URL(this.serverURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");

        /* detailed */

        httpURLConnection.connect();
    }

    public abstract AIData collect(final CheckHandler handler);

    public abstract void detect(final AICheckResult result);
}
