package space.commandf1.amlegit.util;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.entity.Player;

public class PlayerUtil {
    public static int getPing(Player player) {
        return PacketEvents.getAPI().getPlayerManager().getPing(player);
    }
}
