package space.commandf1.amlegit.tracker.trackers;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.Tracker;
import space.commandf1.amlegit.tracker.providers.PositionTrackerDataProvider;
import space.commandf1.amlegit.util.BlockUtil;

public class PositionTracker extends Tracker {

    @Getter
    private boolean onGround, serverOnGround, lastOnGround, lastLastOnGround, horizontalCollision;

    @Getter
    private Location deltaLocation, deltaLastLocation, deltaLastLastLocation;

    @Getter
    private boolean sneaking;

    private boolean hasRespawned;

    @Getter
    private long lastRespawnTime;

    @Getter
    private org.bukkit.Location location, lastLocation, lastLastLocation, lastLastLastLocation;

    public boolean hasRespawned() {
        return this.hasRespawned;
    }

    public PositionTracker(PlayerData playerData) {
        super(playerData);
    }

    public double getVelocity() {
        double x2 = this.location.distance(this.lastLocation);
        double x1 = this.lastLocation.distance(this.lastLastLocation);
        return x2 - x1 / ((double) 1 / 20);
    }

    public double getAcceleratedVelocity() {
        double x3 = this.location.distance(this.lastLocation);
        double x2 = this.lastLocation.distance(this.lastLastLocation);
        double x1 = this.lastLastLocation.distance(this.lastLastLastLocation);

        return (x3 - 2 * x2 + x1) / Math.sqrt(((double) 1 / 20));
    }

    public boolean hasInit() {
        return deltaLocation != null &&
                deltaLastLocation != null &&
                deltaLastLastLocation != null &&
                location != null &&
                lastLocation != null &&
                lastLastLocation != null &&
                lastLastLastLocation != null;
    }

    private void updateState(WrapperPlayClientPlayerFlying packet, Player player) {
        this.lastLastOnGround = this.lastOnGround;
        this.lastOnGround = this.onGround;
        this.onGround = packet.isOnGround();
        Block block = BlockUtil.getExactStandingBlock(player);
        if (block != null) {
            this.serverOnGround = !BlockUtil.isPassable(block.getType()) && block.getType() != Material.AIR;
        }
        this.horizontalCollision = packet.isHorizontalCollision();
        this.deltaLastLastLocation = this.deltaLastLocation;
        this.deltaLastLocation = this.deltaLocation;
        this.deltaLocation = packet.getLocation();

        this.lastLastLastLocation = this.lastLastLocation;
        this.lastLastLocation = this.lastLocation;
        this.lastLocation = this.location;
        this.location = player.getLocation();
    }

    @Override
    public void handle(PacketReceiveEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.getUniqueId().equals(this.getPlayerData().getPlayer().getUniqueId())) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
            this.updateState(packet, player);
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_FLYING) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);
            this.updateState(packet, player);
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
            this.updateState(packet, player);
        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SNEAKING) {
                this.sneaking = true;
            } else if (packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SNEAKING) {
                this.sneaking = false;
            }
        }
    }

    @Override
    public void handle(PacketSendEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.getUniqueId().equals(this.getPlayerData().getPlayer().getUniqueId())) {
            return;
        }

        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            if (!this.hasRespawned()) {
                this.hasRespawned = true;
            }

            this.lastRespawnTime = System.currentTimeMillis();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public PositionTrackerDataProvider currentDataProvider() {
        return new PositionTrackerDataProvider(this);
    }
}
