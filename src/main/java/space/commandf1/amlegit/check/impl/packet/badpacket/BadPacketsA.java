package space.commandf1.amlegit.check.impl.packet.badpacket;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.AlertDescription;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.check.defaults.ReceivedPacketOnly;
import space.commandf1.amlegit.config.check.CheckConfigHandler;

public class BadPacketsA extends Check {

    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 2;

    @CheckConfigHandler(name = "max-pitch")
    @AlertDescription(name = "MaxPitch")
    private double maxPitch = 90.0F;

    @CheckConfigHandler(name = "min-pitch")
    @AlertDescription(name = "MinPitch")
    private double minPitch = -90.0F;

    public BadPacketsA(Plugin plugin) {
        super("BadPackets", 1, "Check for impossible rotation", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.PLAYER_ROTATION, PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION);
    }

    @SneakyThrows
    @Override
    @ReceivedPacketOnly
    public void onCheck(final CheckHandler handler) {
        PacketReceiveEvent event = (PacketReceiveEvent) handler.getEvent();
        WrapperPlayClientPlayerFlying packet = null;
        if (event.getPacketType() ==  PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            packet = new WrapperPlayClientPlayerPositionAndRotation(event);
        } else if (event.getPacketType() ==  PacketType.Play.Client.PLAYER_ROTATION) {
            packet = new WrapperPlayClientPlayerRotation(event);
        }

        if (packet == null) {
            return;
        }

        float pitch = (float) packet.getClass().getDeclaredMethod("getPitch").invoke(packet);

        if (pitch > this.maxPitch) {
            if (handler.increaseBuffer(1) >= this.maxBuffer) {
                handler.fail();
            }
        } else if (pitch < this.minPitch) {
            if (handler.increaseBuffer(1) >= this.maxBuffer) {
                handler.fail();
            }
        }
    }
}
