package space.commandf1.amlegit.check.impl.move.groundspoof;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.config.check.DefaultDisableCheck;
import space.commandf1.amlegit.tracker.impl.PositionTracker;
import space.commandf1.amlegit.util.BlockUtil;

import java.util.List;

@DefaultDisableCheck
public class GroundSpoofA extends Check implements Setbackable {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 1000;

    @CheckConfigHandler(name = "block-step")
    @AlertDescription(name = "BlockStep")
    private double blockStep = (double) 1 / 64;

    @CheckConfigHandler(name = "on-ground-offset")
    @AlertDescription(name = "OnGroundOffset")
    private double offset = 0.331D;

    public GroundSpoofA(Plugin plugin) {
        super("GroundSpoof", 10, "Check for ground spoof", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.PLAYER_POSITION,
                PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    @ReceivedPacketOnly
    public void onCheck(final CheckHandler handler) {
        PositionTracker tracker = (PositionTracker) handler.getPlayerData().getTracker(PositionTracker.class).get();
        boolean onGround = tracker.isOnGround();
        boolean serverOnGround = tracker.isServerOnGround();
        // boolean mathOnGround = tracker.getLocation().getY() % this.blockStep == 0.0D;

        if (onGround && !serverOnGround) {
            double[] offsets = {this.offset, -this.offset};
            for (double offset : offsets) {
                Location clone = tracker.getLastLocation().clone();
                List<Double> trys = List.of(tracker.getLocation().getY(), tracker.getLocation().getY() -1);
                for (Double aTry : trys) {
                    clone.setY(aTry);

                    clone.setX(clone.getX() + offset);

                    Block block = clone.getBlock();
                    if (block != null && !BlockUtil.isPassable(block.getType()) && block.getType() != Material.AIR) {
                        return;
                    }
                }
            }

            for (double offset : offsets) {
                Location clone = tracker.getLastLocation().clone();
                List<Double> trys = List.of(tracker.getLocation().getY(), tracker.getLocation().getY() -1);
                for (Double aTry : trys) {
                    clone.setY(aTry);
                    clone.setZ(clone.getZ() + offset);

                    Block block = clone.getBlock();
                    if (block != null && !BlockUtil.isPassable(block.getType()) && block.getType() != Material.AIR) {
                         return;
                    }
                }
            }

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
        PositionTracker tracker = (PositionTracker) handler.getPlayerData().getTracker(PositionTracker.class).get();
        handler.getPlayerData().getPlayer().teleport(tracker.getLastLastLastLocation());
    }
}
