package space.commandf1.amlegit.check.impl.packet.badpacket;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.tracker.impl.PositionTracker;

public class BadPacketsB extends Check implements Setbackable {

    @CheckConfigHandler(name = "tolerance")
    @AlertDescription(name = "Tolerance")
    private long tolerance = 100;

    public BadPacketsB(Plugin plugin) {
        super("BadPackets", 3, "Checks for C0F disabler", "B", plugin);
        this.addAllowedPackets(PacketType.Play.Client.WINDOW_CONFIRMATION, PacketType.Play.Server.WINDOW_CONFIRMATION,
                PacketType.Play.Client.PLAYER_POSITION, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
                PacketType.Play.Client.PLAYER_ROTATION);
    }

    @Override
    public void onCheck(CheckHandler handler) {
        if (handler.getEvent() instanceof PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
                handler.decreaseBuffer(handler.buffer());
            } else {
                handler.increaseBuffer(1);
            }
        } else if (handler.getEvent() instanceof PacketSendEvent) {
            handler.increaseBuffer(1);
        }

        if (handler.buffer() >= this.tolerance) {
            handler.fail();
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void handleSetback(AbstractCheckHandler handler) {
        PositionTracker tracker = (PositionTracker) handler.getPlayerData().getTracker(PositionTracker.class).get();
        handler.getPlayerData().getPlayer().teleport(tracker.getLastLastLastLocation());
    }
}
