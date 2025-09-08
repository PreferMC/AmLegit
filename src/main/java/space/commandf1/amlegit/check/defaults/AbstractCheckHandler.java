package space.commandf1.amlegit.check.defaults;

import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.check.action.ActionHandler;
import space.commandf1.amlegit.config.check.CheckConfig;
import space.commandf1.amlegit.config.check.CheckConfigHolder;
import space.commandf1.amlegit.config.settings.SettingsConfig;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.TrackerDataProvider;
import space.commandf1.amlegit.util.ServerUtil;
import space.commandf1.amlegit.util.StringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("UnusedReturnValue")
public abstract class AbstractCheckHandler {
    @Getter
    private final PlayerData playerData;

    @Getter
    private final Check check;

    private final Set<TrackerDataProvider<?>> trackers = new HashSet<>();

    public <T extends TrackerDataProvider<?>> Optional<T> getTrackerDataProvider(Class<T> trackerClass) {
        return trackers.stream().filter(trackerClass::isInstance).map(trackerClass::cast).findFirst();
    }

    public TrackerDataProvider<?>[] getTrackerDataProviders() {
        return trackers.toArray(TrackerDataProvider[]::new);
    }

    private static final Map<PlayerData, Long> buffers = new ConcurrentHashMap<>(),
            vls = new ConcurrentHashMap<>();

    public AbstractCheckHandler(PlayerData playerData, Check check) {
        this.playerData = playerData;
        this.check = check;
        this.playerData.getTrackers().forEach(tracker -> trackers.add(tracker.currentDataProvider()));
    }

    public synchronized final long increaseBuffer(long buffer) {
        Long currentBuffer = buffers.get(this.playerData);
        if (currentBuffer == null) {
            currentBuffer = 0L;
        }
        currentBuffer += buffer;
        buffers.put(this.playerData, currentBuffer);
        return currentBuffer;
    }

    public final long getMaxVL() {
        return (long) CheckConfig.getConfig(this.getCheck().getPlugin())
                .getCheckConfigHolder(this.getCheck(), "maxVL").getValue();
    }

    public synchronized final long fail() {
        Long currentVL = vls.get(playerData);
        if (currentVL == null) {
            currentVL = 0L;
        }
        currentVL += 1L;
        vls.put(playerData, currentVL);
        buffers.put(this.playerData, 0L);

        if (this.check instanceof Setbackable setbackable &&
                CheckConfig.getConfig(this.getCheck().getPlugin())
                        .getCheckConfigHolder(this.getCheck(), "setback")
                        .getValue() instanceof Boolean setback
                && setback) {
            setbackable.handleSetback(this);
        }

        this.alert();

        if (currentVL >= this.getMaxVL()) {
            this.punish();
        }

        return currentVL;
    }

    public final void alert(String description) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                SettingsConfig.getConfig(this.getCheck().getPlugin())
                        .getStringValue(SettingsConfig.Configs.PREFIX));
        String alert = SettingsConfig.getConfig(this.getCheck().getPlugin())
                .getStringValue(SettingsConfig.Configs.ALERT_FORMAT)
                .replace("%player%", this.playerData.getPlayer().getName())
                .replace("%check%", this.check.getName())
                .replace("%type%", this.check.getType())
                .replace("%bar%", StringUtil.getPercentMessage((double) this.vl() / this.getMaxVL(),
                        '|', 'f', '8', 10))
                .replace("%tps%", String.valueOf(ServerUtil.getTps()))
                .replace("%ping%", StringUtil.getPingString(this.playerData.getPing()))
                .replace("%vl%", String.valueOf(this.vl()))
                .replace("%maxvl%", String.valueOf(this.getMaxVL()));
        alert = ChatColor.translateAlternateColorCodes('&', alert);
        if (SettingsConfig.getConfig(this.getCheck().getPlugin()).getBooleanValue(SettingsConfig.Configs.ALERT_TO_CONSOLE)) {
            String finalAlert = alert;
            Bukkit.getScheduler().runTask(this.getCheck().getPlugin(),
                    () -> Bukkit.getConsoleSender().sendMessage(prefix + finalAlert));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData alertPlayerData = PlayerData.getByUUID(player.getUniqueId());
            if (!player.hasPermission("amlegit.alert") || alertPlayerData == null || !alertPlayerData.isAlertEnabled()) {
                continue;
            }

            TextComponent alertText = new TextComponent(prefix + alert);
            TextComponent hoverText = new TextComponent(this.getCheck().getInfoMessage(description));
            alertText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] { hoverText }));
            Bukkit.getScheduler().runTask(this.getCheck().getPlugin(),
                    () -> player.spigot().sendMessage(alertText));
        }
    }

    public final void alert() {
        this.alert(this.getCheck().getDescription());
    }

    public synchronized void punish(String description) {
        CheckConfig config = CheckConfig.getConfig(this.check.getPlugin());
        CheckConfigHolder<?> commands = config.getCheckConfigHolder(this.check, "commands");
        if (commands.getValue() instanceof ActionHandler actionHandler) {
            Bukkit.getScheduler().runTask(this.getCheck().getPlugin(), () -> actionHandler.execute(command ->
                    command.replace("%player%", this.getPlayerData().getPlayer().getName())
                            .replace("%check%", this.getCheck().getName())
                            .replace("%type%", this.getCheck().getType())
                            .replace("%maxVL%", String.valueOf(this.getMaxVL()))
                            .replace("%description%", description)
                            .replace("%vl%", String.valueOf(this.vl()))
                            .replace("%buffer%", String.valueOf(this.buffer()))
            ));
        }
        vls.put(playerData, 0L);
        buffers.put(playerData, 0L);
    }

    public void punish() {
        this.punish(this.getCheck().getDescription());
    }

    public synchronized final long decreaseBuffer(long buffer) {
        long currentBuffer = buffers.computeIfAbsent(this.playerData, k -> 0L);

        currentBuffer -= Math.min(currentBuffer, buffer);
        buffers.put(this.playerData, currentBuffer);
        return currentBuffer;
    }

    public synchronized final long buffer() {
        return buffers.computeIfAbsent(this.playerData, k -> 0L);
    }

    public synchronized final long vl() {
        return vls.computeIfAbsent(this.playerData, k -> 0L);
    }
}
