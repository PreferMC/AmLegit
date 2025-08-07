package space.commandf1.amlegit.check.impl.move.analysis;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;

public class AnalysisA extends Check implements Setbackable {
    public AnalysisA(Plugin plugin) {
        super("Analysis",
                12,
                "Analysis whether the player is hacking or not",
                "A",
                plugin
        );
        this.addAllowedPackets();
    }

    @Override
    @ReceivedPacketOnly
    public void onCheck(final CheckHandler handler) {
        val event = (PacketReceiveEvent) handler.getEvent();
        val playerData = handler.getPlayerData();

    }

    @Override
    public void handleSetback(final AbstractCheckHandler handler) {
        Player player = handler.getPlayerData().getPlayer();
        if (player == null) {
            return;
        }

        player.setSprinting(false);
    }
}
