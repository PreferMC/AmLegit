package space.commandf1.amlegit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.data.PlayerData;

public record PlayerListener(Plugin plugin) implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        PlayerData.of(event.getPlayer(), this.plugin());
    }
}
