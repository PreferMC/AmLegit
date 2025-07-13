package space.commandf1.amlegit.check.impl.move.scaffold;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.val;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.check.AlertDescription;
import space.commandf1.amlegit.check.Check;
import space.commandf1.amlegit.check.CheckHandler;
import space.commandf1.amlegit.check.ReceivedPacketOnly;
import space.commandf1.amlegit.config.check.CheckConfigHandler;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.util.BlockUtil;

public class ScaffoldA extends Check {
    @CheckConfigHandler(name = "max-buffer")
    @AlertDescription(name = "MaxBuffer")
    private int maxBuffer = 3;

    @CheckConfigHandler(name = "on-ground-offset")
    @AlertDescription(name = "OnGroundOffset")
    private double offset = 0.331D;

    @CheckConfigHandler(name = "maxCursorPositionOffSide")
    @AlertDescription(name = "MaxCursorPositionOffSide")
    private double maxCursorPositionOffSide = 0.9375D;

    @CheckConfigHandler(name = "maxCursorPositionOnSide")
    @AlertDescription(name = "MaxCursorPositionOnSide")
    private double maxCursorPositionOnSide = 1.0D;

    @CheckConfigHandler(name = "minCursorPositionOffSide")
    @AlertDescription(name = "MinCursorPositionOffSide")
    private double minCursorPositionOffSide = 0.0D;

    public ScaffoldA(Plugin plugin) {
        super("Scaffold", 12, "Check for scaffold", "A", plugin);
        this.addAllowedPackets(PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT);
    }

    @Override
    @ReceivedPacketOnly
    public void onCheck(CheckHandler handler) {
        PlayerData playerData = handler.getPlayerData();
        Player player = playerData.getPlayer();
        PacketReceiveEvent event = (PacketReceiveEvent) handler.getEvent();
        WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
        Vector3f cursorPosition = packet.getCursorPosition();
        Vector3i blockPosition = packet.getBlockPosition();

        /* let's check whether the player bridges with the block he holds on */
        ItemStack itemInHand = player.getItemInHand();
        val itemStack = SpigotConversionUtil.fromBukkitItemStack(itemInHand);
        packet.getItemStack().ifPresentOrElse(item -> {
            if (!item.getType().equals(itemStack.getType())) {
                if (handler.increaseBuffer(5) > this.maxBuffer) {
                    handler.fail();
                }
            }
        }, () -> {
            if (packet.getFace() != BlockFace.OTHER) {
                if (handler.increaseBuffer(5) > this.maxBuffer) {
                    handler.fail();
                }
            }
        });

        if (packet.getFace() != BlockFace.OTHER) {
            Block blockOnGround = BlockUtil.getBlockOnGround(playerData, this.offset);
            if (blockOnGround != null
                    && blockOnGround.getLocation().getBlockX() == blockPosition.getX()
                    && blockOnGround.getLocation().getBlockY() == blockPosition.getY()
                    && blockOnGround.getLocation().getBlockZ() == blockPosition.getZ()) {
                /* we have to check whether the player placed the block in the correct cursor position */
                switch (packet.getFace()) {
                    case SOUTH:
                        if (cursorPosition.getZ() != this.maxCursorPositionOnSide
                                || cursorPosition.getX() > this.maxCursorPositionOffSide
                                || cursorPosition.getY() > this.maxCursorPositionOffSide) {
                            if (handler.increaseBuffer(1) > this.maxBuffer) {
                                handler.fail();
                            }
                        }
                        break;
                    case NORTH:
                        if (cursorPosition.getZ() != this.minCursorPositionOffSide
                                || cursorPosition.getX() > this.maxCursorPositionOffSide
                                || cursorPosition.getY() > this.maxCursorPositionOffSide) {
                            if (handler.increaseBuffer(1) > this.maxBuffer) {
                                handler.fail();
                            }
                        }
                        break;
                    case WEST:
                        if (cursorPosition.getZ() > this.maxCursorPositionOffSide
                                || cursorPosition.getX() != this.minCursorPositionOffSide
                                || cursorPosition.getY() > this.maxCursorPositionOffSide) {
                            if (handler.increaseBuffer(1) > this.maxBuffer) {
                                handler.fail();
                            }
                        }
                        break;
                    case EAST:
                        if (cursorPosition.getZ() > this.maxCursorPositionOffSide
                                || cursorPosition.getX() != this.maxCursorPositionOnSide
                                || cursorPosition.getY() > this.maxCursorPositionOffSide) {
                            if (handler.increaseBuffer(1) > this.maxBuffer) {
                                handler.fail();
                            }
                        }
                        break;
                }

                /* after that, we have to check whether the block is placeable */

            }
        }
    }
}
