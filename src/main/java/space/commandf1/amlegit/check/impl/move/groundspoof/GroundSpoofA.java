package space.commandf1.amlegit.check.impl.move.groundspoof;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import lombok.val;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.tracker.providers.PositionTrackerDataProvider;
import space.commandf1.amlegit.tracker.trackers.PositionTracker;

import java.util.Optional;

import static space.commandf1.amlegit.constant.MathConstant.EPSILON;
import static space.commandf1.amlegit.constant.MinecraftConstant.BLOCK_STEP;

/**
 * @author commandf1
 */
public class GroundSpoofA extends Check implements Setbackable {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 1000;

    public GroundSpoofA(Plugin plugin) {
        super("GroundSpoof", 10, "Check for ground spoof", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.PLAYER_POSITION,
                PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    @Override
    @ReceivedPacketOnly
    public void onCheck(final CheckHandler handler) {
        Optional<PositionTrackerDataProvider> optionalProvider = handler.getTrackerDataProvider(PositionTrackerDataProvider.class);
        if (optionalProvider.isEmpty()) {
            return;
        }

        val positionTrackerDataProvider = optionalProvider.get();
        val onGround = positionTrackerDataProvider.isOnGround();
        val serverOnGround = positionTrackerDataProvider.isServerOnGround();

        double y = positionTrackerDataProvider.getLocation().getY();
        boolean mathOnGround = Math.abs(y % BLOCK_STEP) < EPSILON || Math.abs(y % BLOCK_STEP - BLOCK_STEP) < EPSILON;

        if (onGround && !serverOnGround && !mathOnGround) {
            if (handler.increaseBuffer(1000) > this.maxBuffer) {
                handler.fail();
            }
        } else {
            handler.decreaseBuffer(1);
        }
    }

    @Override
    public void handleSetback(final AbstractCheckHandler handler) {
        Optional<PositionTracker> optionalTracker = handler.getPlayerData().getTracker(PositionTracker.class);
        optionalTracker.ifPresent(tracker -> handler.getPlayerData().getPlayer().teleport(tracker.getLastLastLastLocation()));
    }
}
