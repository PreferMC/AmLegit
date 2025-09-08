package space.commandf1.amlegit.check.impl.move.groundspoof;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import lombok.val;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.tracker.providers.PositionTrackerDataProvider;
import space.commandf1.amlegit.tracker.trackers.PositionTracker;

/**
 * @author commandf1
 */
public class GroundSpoofA extends Check implements Setbackable {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 1000;

    @CheckConfigHandler(name = "block-step")
    @AlertDescription(name = "BlockStep")
    private double blockStep = (double) 1 / 64;

    public GroundSpoofA(Plugin plugin) {
        super("GroundSpoof", 10, "Check for ground spoof", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.PLAYER_POSITION,
                PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    @ReceivedPacketOnly
    public void onCheck(final CheckHandler handler) {
        val positionTrackerDataProvider = handler.getTrackerDataProvider(PositionTrackerDataProvider.class).get();
        val onGround = positionTrackerDataProvider.isOnGround();
        val serverOnGround = positionTrackerDataProvider.isServerOnGround();
        val mathOnGround = positionTrackerDataProvider.getLocation().getY() % this.blockStep == 0.0D;

        if (onGround && !serverOnGround && !mathOnGround) {
            if (handler.increaseBuffer(1000) > this.maxBuffer) {
                handler.fail();
            }
        } else {
            handler.decreaseBuffer(1);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void handleSetback(final AbstractCheckHandler handler) {
        PositionTracker tracker = handler.getPlayerData().getTracker(PositionTracker.class).get();
        handler.getPlayerData().getPlayer().teleport(tracker.getLastLastLastLocation());
    }
}
