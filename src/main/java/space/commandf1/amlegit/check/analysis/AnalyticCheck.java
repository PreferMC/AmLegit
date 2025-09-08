package space.commandf1.amlegit.check.analysis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.TrackerDataProvider;
import space.commandf1.amlegit.tracker.providers.NetworkTrackerDataProvider;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Analytic check
 * We do collect data every time we receive the packets and then save them
 * so that we can ensure check is performing under 0ms delay
 * to disable some disablers like C03, C0F, etc.
 * and to check players without considering network delay.
 *
 * @author commandf1
 * */
@ToString
@EqualsAndHashCode(callSuper = true)
public abstract class AnalyticCheck extends Check {
    private final NavigableMap<Long, CheckHandler> checkHandlers = new ConcurrentSkipListMap<>();

    @Getter
    private final long minAcceptablePacketLossPerWindow, packetsNumbersToHandle;

    /**
     * An analytic check class
     * @param name the name of the check
     * @param defaultMaxVL the default max vl of the check
     * @param description the description of the check
     * @param type the type of the check
     * @param minAcceptablePacketLossPerWindow the number of packet missing that is acceptable per window
     * @param packetsNumbersToHandle when packet numbers reach this, we will call to check
     * @param plugin the instance of the plugin
     * */
    public AnalyticCheck(String name,
                         long defaultMaxVL,
                         String description,
                         String type,
                         long minAcceptablePacketLossPerWindow,
                         long packetsNumbersToHandle,
                         Plugin plugin) {
        super(name, defaultMaxVL, description, type, plugin);
        this.minAcceptablePacketLossPerWindow = minAcceptablePacketLossPerWindow;
        this.packetsNumbersToHandle = packetsNumbersToHandle;
    }

    private static final Comparator<PlayerData> PLAYER_DATA_COMPARATOR =
            Comparator.comparing(playerData -> playerData.getPlayer().getUniqueId());

    private final NavigableMap<PlayerData, NavigableMap<Long, Set<TrackerDataProvider<?>>>> serverTrackers = new ConcurrentSkipListMap<>(PLAYER_DATA_COMPARATOR);
    private final NavigableMap<PlayerData, NavigableMap<Long, CheckHandler>> clientPackets = new ConcurrentSkipListMap<>(PLAYER_DATA_COMPARATOR);

    @Override
    public final void onCheck(CheckHandler handler) {
        val packetReceivalTimestamp = handler.getEvent().getTimestamp();
        val networkTrackerDataProvider = handler.getTrackerDataProvider(NetworkTrackerDataProvider.class);
        if (networkTrackerDataProvider.isEmpty()) {
            return;
        }

        val ping = networkTrackerDataProvider.get().getPing();

        /* 理论客户端发包时间戳计算公式: packetReceivalTimestamp - (ping / 2) */
        val clientSendingTimestamp = packetReceivalTimestamp - ((int) (ping / 2));

        var trackers = this.serverTrackers.get(handler.getPlayerData());
        if (trackers == null) {
            trackers = new ConcurrentSkipListMap<>();
        }

        trackers.put(packetReceivalTimestamp, new HashSet<>(List.of(handler.getTrackerDataProviders())));

        var packets = this.clientPackets.get(handler.getPlayerData());
        if (packets == null) {
            packets = new ConcurrentSkipListMap<>();
        }

        packets.put(clientSendingTimestamp, handler);

        this.serverTrackers.put(handler.getPlayerData(), trackers);
        this.clientPackets.put(handler.getPlayerData(), packets);

        /* 检查包大小 */
        if (packets.size() < this.packetsNumbersToHandle) {
            return;
        }

        /* 触发检测 */
        this.handleCheck(new AnalyticCheckHandler(
                handler.getPlayerData(),
                this,
                trackers,
                packets
        ));

        /* 删除旧数据 */
        packets.clear();
        trackers.clear();
    }

    public abstract void handleCheck(AnalyticCheckHandler check);
}
