package space.commandf1.amlegit.check.impl.packet.backtrack;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.AlertDescription;
import space.commandf1.amlegit.check.Check;
import space.commandf1.amlegit.check.CheckHandler;
import space.commandf1.amlegit.check.ReceivedPacketOnly;
import space.commandf1.amlegit.config.check.CheckConfigHandler;

public class BacktrackA extends Check {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 10;

    public BacktrackA(Plugin plugin) {
        super("Backtrack", 12, "Check for position difference", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.INTERACT_ENTITY);
    }

    @Override
    @ReceivedPacketOnly
    public void onCheck(CheckHandler handler) {
        PacketReceiveEvent event = (PacketReceiveEvent) handler.getEvent();
        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            return;
        }

        Entity target = SpigotConversionUtil.getEntityById(handler.getPlayerData().getPlayer().getWorld(), packet.getEntityId());


    }
}
