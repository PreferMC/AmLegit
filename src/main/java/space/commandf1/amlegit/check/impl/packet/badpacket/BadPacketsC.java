package space.commandf1.amlegit.check.impl.packet.badpacket;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import space.commandf1.amlegit.check.defaults.AlertDescription;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.data.LRUCache;
import space.commandf1.amlegit.data.PlayerData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BadPacketsC extends Check {

    @CheckConfigHandler(name = "max-delayed")
    @AlertDescription(name = "MaxDelayed")
    /* 若每秒62个transaction，最高允许30秒延迟；对于每个玩家，最大消耗约206K内存 */
    private int maxDelayed = 30 * 62;

    private final Map<PlayerData, LRUCache<Short>> transactions = new ConcurrentHashMap<>();

    public BadPacketsC(Plugin plugin) {
        super("BadPackets", 10, "TransactionOrder", "C", plugin);
        this.addAllowedPackets(PacketType.Play.Client.WINDOW_CONFIRMATION, PacketType.Play.Server.WINDOW_CONFIRMATION);
    }

    private LRUCache<Short> getCache(@NotNull CheckHandler handler) {
        return transactions.computeIfAbsent(
                handler.getPlayerData(),
                k -> new LRUCache<>(this.maxDelayed)
        );
    }

    @Override
    public void onCheck(@NotNull CheckHandler handler) {
        if (handler.getEvent() instanceof PacketSendEvent event
                && event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            LRUCache<Short> cache = getCache(handler);
            cache.put(new WrapperPlayServerWindowConfirmation(event).getActionId());
        }

        if (handler.getEvent() instanceof PacketReceiveEvent event
                && event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            LRUCache<Short> cache = getCache(handler);
            short actionId = new WrapperPlayClientWindowConfirmation(event).getActionId();
            short expected = cache.getLeastUsed();
            if (expected == actionId) {
                cache.remove(expected);
                return;
            }

            if (!cache.contains(actionId)) {
                // invalid packet
                handler.alert("Invalid Transaction");
                handler.punish("Invalid Transaction");
                return;
            }

            handler.fail();
            // fix the order
            do {
                cache.remove(cache.getLeastUsed());
            } while (cache.getLeastUsed() == actionId);
        }
    }
}
