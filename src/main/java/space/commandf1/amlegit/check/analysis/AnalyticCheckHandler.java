package space.commandf1.amlegit.check.analysis;

import lombok.val;
import space.commandf1.amlegit.check.defaults.AbstractCheckHandler;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.TrackerDataProvider;

import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author commandf1
 */
public class AnalyticCheckHandler extends AbstractCheckHandler {

    private final NavigableMap<Long, Set<TrackerDataProvider<?>>> serverTrackers;
    private final NavigableMap<Long, CheckHandler> clientPackets;

    public AnalyticCheckHandler(PlayerData playerData,
                                Check check,
                                NavigableMap<Long, Set<TrackerDataProvider<?>>> serverTrackers,
                                NavigableMap<Long, CheckHandler> clientPackets) {
        super(playerData, check);
        this.serverTrackers = new ConcurrentSkipListMap<>(serverTrackers);
        this.clientPackets = new ConcurrentSkipListMap<>(clientPackets);
    }

    public CheckHandler getHandlerAt(long timestamp) {
        return this.clientPackets.get(timestamp);
    }

    public <T extends TrackerDataProvider<?>> Optional<T> getTrackerDataProviderAt(long timestamp, Class<T> clazz) {
        val trackerDataProviders = this.serverTrackers.get(timestamp);
        if (trackerDataProviders != null) {
            for (TrackerDataProvider<?> provider : trackerDataProviders) {
                if (clazz.isInstance(provider)) {
                    return Optional.of(clazz.cast(provider));
                }
            }
        }

        return Optional.empty();
    }
}
