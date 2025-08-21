package space.commandf1.amlegit.check.analysis;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;

import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AnalyticCheck extends Check {
    private final NavigableMap<Long, CheckHandler> checkHandlers = new ConcurrentSkipListMap<>();

    public AnalyticCheck(String name,
                         long defaultMaxVL,
                         String description,
                         String type,
                         Plugin plugin) {
        super(name, defaultMaxVL, description, type, plugin);
    }

    @Override
    public final void onCheck(CheckHandler handler) {
        long now = System.currentTimeMillis();
        this.checkHandlers.put(now, handler);
        if (this.shouldAnalysis(this.checkHandlers.firstKey() - now, this.checkHandlers.size())) {
            this.handleCheck(new AnalyticCheckHandler(handler.getPlayerData(),
                    this,
                    new ConcurrentSkipListMap<>(this.checkHandlers))
            );
            checkHandlers.clear();
        }
    }

    public abstract boolean shouldAnalysis(long delay, int packetSize);

    public abstract void handleCheck(AnalyticCheckHandler handler);
}
