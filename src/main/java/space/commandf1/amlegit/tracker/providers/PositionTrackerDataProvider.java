package space.commandf1.amlegit.tracker.providers;

import com.github.retrooper.packetevents.protocol.world.Location;
import lombok.Getter;
import space.commandf1.amlegit.tracker.TrackerDataProvider;
import space.commandf1.amlegit.tracker.trackers.PositionTracker;

/**
 * @author commandf1
 */
public class PositionTrackerDataProvider extends TrackerDataProvider<PositionTracker> {
    @Getter
    private final boolean onGround, serverOnGround, lastOnGround, lastLastOnGround, horizontalCollision;

    @Getter
    private final Location deltaLocation, deltaLastLocation, deltaLastLastLocation;

    @Getter
    private final boolean sneaking;

    private final boolean hasRespawned;

    @Getter
    private final long lastRespawnTime;

    @Getter
    private final org.bukkit.Location location, lastLocation, lastLastLocation, lastLastLastLocation;

    public boolean hasRespawned() {
        return this.hasRespawned;
    }

    public PositionTrackerDataProvider(PositionTracker tracker) {
        super(tracker);
        this.onGround = this.getTracker().isOnGround();
        this.serverOnGround = this.getTracker().isServerOnGround();
        this.lastOnGround = this.getTracker().isLastOnGround();
        this.lastLastOnGround = this.getTracker().isLastLastOnGround();
        this.horizontalCollision = this.getTracker().isHorizontalCollision();
        this.deltaLocation = tracker.getDeltaLocation();
        this.deltaLastLocation = tracker.getDeltaLastLocation();
        this.deltaLastLastLocation = tracker.getDeltaLastLastLocation();
        this.sneaking = tracker.isSneaking();
        this.hasRespawned = tracker.hasRespawned();
        this.lastRespawnTime = tracker.getLastRespawnTime();
        this.location = tracker.getLocation();
        this.lastLocation = tracker.getLastLocation();
        this.lastLastLocation = tracker.getLastLastLocation();
        this.lastLastLastLocation = tracker.getLastLastLastLocation();
    }
}
