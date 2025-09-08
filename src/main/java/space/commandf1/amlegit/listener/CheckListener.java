package space.commandf1.amlegit.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.AmLegitPlugin;
import space.commandf1.amlegit.check.CheckManager;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.config.check.CheckConfig;
import space.commandf1.amlegit.config.settings.SettingsConfig;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.trackers.PositionTracker;

public class CheckListener implements PacketListener {
    private final AmLegitPlugin plugin;

    public CheckListener(AmLegitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        this.processChecks(event);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void processChecks(ProtocolPacketEvent event) {
        if (event.getPlayer() == null) return;
        Player player = event.getPlayer();

        if (!SettingsConfig.getConfig(this.plugin).getBooleanValue(SettingsConfig.Configs.CHECK_DETECT_OPS)
                && player.hasPermission("amlegit.bypass")) {
            return;
        }

        PlayerData playerData = PlayerData.getByUUID(player.getUniqueId());

        if (playerData == null) {
            return;
        }

        if (!playerData.getTracker(PositionTracker.class).get().hasInit()) return;

        CheckManager checkManager = this.plugin.getCheckManager();
        for (Check check : checkManager.getChecks()) {
            if (!check.getAllowedPacketTypes().contains(event.getPacketType())) {
                continue;
            }

            CheckConfig config = CheckConfig.getConfig(this.plugin);

            if (config.getCheckConfigHolder(check, "enable").getValue() instanceof Boolean enabled && !enabled) {
                continue;
            }

            if (!(event instanceof PacketReceiveEvent) && check.isReceivedPacketOnly()) {
                continue;
            }

            if (!(event instanceof PacketSendEvent) && check.isSentPacketOnly()) {
                continue;
            }

            check.onCheck(check.newCheckHandler(playerData, check, event));
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        this.processChecks(event);
    }
}
