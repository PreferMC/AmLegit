package space.commandf1.amlegit.check.impl.move.keepsprint;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.defaults.*;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.impl.PositionTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeepSprintA extends Check implements Setbackable {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 5;

    @CheckConfigHandler(name = "tolerance")
    @AlertDescription(name = "Tolerance")
    private int tolerance = 6000;

    public KeepSprintA(Plugin plugin) {
        super("KeepSprint", 5, "Check for keep sprint", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.ENTITY_ACTION);
    }

    private final Map<PlayerData, Long> sprintPacketSent = new ConcurrentHashMap<>();
    private final Map<PlayerData, Long> stopSneakTime = new ConcurrentHashMap<>();

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    @ReceivedPacketOnly
    public void onCheck(CheckHandler handler) {
        PlayerData playerData = handler.getPlayerData();
        if (playerData.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        PacketReceiveEvent event = (PacketReceiveEvent) handler.getEvent();
        WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
        PositionTracker tracker = (PositionTracker) handler.getPlayerData().getTracker(PositionTracker.class).get();

        if (packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
            /* Update the last stop sneaking time */
            stopSneakTime.put(playerData, System.currentTimeMillis());

            if (sprintPacketSent.containsKey(playerData)) {
                Long sprintPacketSentTime = sprintPacketSent.get(handler.getPlayerData());
                if (System.currentTimeMillis() - sprintPacketSentTime > this.tolerance) {
                    handler.decreaseBuffer(1);
                }
            }
        }

        if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
            sprintPacketSent.put(playerData, System.currentTimeMillis());

            /* Check if the player is sneaking or has recently stopped sneaking within tolerance */
            boolean isSneaking = tracker.isSneaking();
            boolean hasRecentlyStoppedSneaking = stopSneakTime.containsKey(playerData)
                    && (System.currentTimeMillis() - stopSneakTime.get(playerData)) <= this.tolerance;

            /* Only check sprinting while sneaking if not recently stopped sneaking */
            if (isSneaking && !hasRecentlyStoppedSneaking) {
                if (handler.increaseBuffer(1) >= this.maxBuffer) {
                    handler.fail();
                }
            }
        }
    }

    @Override
    public void handleSetback(AbstractCheckHandler handler) {
        Player player = handler.getPlayerData().getPlayer();
        if (player == null) {
            return;
        }

        player.setSprinting(false);
    }
}
