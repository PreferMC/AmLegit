package space.commandf1.amlegit.check.impl.packet.badpacket;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.tracker.impl.NetworkTracker;
import space.commandf1.amlegit.tracker.impl.PositionTracker;

public class BadPacketsB extends Check implements Setbackable {

    @CheckConfigHandler(name = "tolerance")
    @AlertDescription(name = "Tolerance")
    private long tolerance = 5;

    @CheckConfigHandler(name = "ping-tolerance")
    @AlertDescription(name = "PingTolerance")
    private long pingTolerance = 10;

    @CheckConfigHandler(name = "max-ping-difference")
    @AlertDescription(name = "MaxPingDifference")
    private long maxPingDifference = 60;

    @CheckConfigHandler(name = "increase-buffer")
    @AlertDescription(name = "IncreaseBuffer")
    private long increaseBuffer = 1;

    @CheckConfigHandler(name = "decrease-buffer")
    @AlertDescription(name = "DecreaseBuffer")
    private long decreaseBuffer = 2;

    public BadPacketsB(Plugin plugin) {
        super("BadPackets", 3, "Checks for C0F disabler", "B", plugin);
        this.addAllowedPackets(PacketType.Play.Client.WINDOW_CONFIRMATION, PacketType.Play.Server.WINDOW_CONFIRMATION,
                PacketType.Play.Client.PLAYER_POSITION, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
                PacketType.Play.Client.PLAYER_ROTATION, PacketType.Play.Client.PLAYER_FLYING);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void onCheck(final CheckHandler handler) {
        NetworkTracker tracker = (NetworkTracker) handler.getPlayerData().getTracker(NetworkTracker.class).get();

        if (Math.abs(tracker.getLastPing() - tracker.getPing()) > this.maxPingDifference) {
            return;
        }

        if (Math.abs(tracker.getHighestPing() - tracker.getPing()) < this.pingTolerance
        && Math.abs(tracker.getHighestPing() - tracker.getLowestPing()) > this.maxPingDifference) {
            return;
        }

        if (handler.getEvent() instanceof PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                handler.decreaseBuffer(this.decreaseBuffer);
            } else {
                handler.increaseBuffer(this.increaseBuffer);
            }
        }

        if (handler.buffer() > this.tolerance + ((double) (tracker.getPing() * 2L) / 1000) / 0.05D) {
            handler.fail();
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void handleSetback(final AbstractCheckHandler handler) {
        PositionTracker tracker = (PositionTracker) handler.getPlayerData().getTracker(PositionTracker.class).get();
        handler.getPlayerData().getPlayer().teleport(tracker.getLastLastLastLocation());
    }
}
