package space.commandf1.amlegit.tracker.providers;

import lombok.Getter;
import space.commandf1.amlegit.tracker.TrackerDataProvider;
import space.commandf1.amlegit.tracker.trackers.NetworkTracker;

/**
 * @author commandf1
 */
public class NetworkTrackerDataProvider extends TrackerDataProvider<NetworkTracker> {
    @Getter
    private final long ping, lastPing, lastLastPing, lowestPing, highestPing;

    public NetworkTrackerDataProvider(NetworkTracker tracker) {
        super(tracker);
        this.ping = this.getTracker().getPing();
        this.lastPing = this.getTracker().getLastPing();
        this.lastLastPing = this.getTracker().getLastLastPing();
        this.lowestPing = this.getTracker().getLowestPing();
        this.highestPing = this.getTracker().getHighestPing();
    }
}
