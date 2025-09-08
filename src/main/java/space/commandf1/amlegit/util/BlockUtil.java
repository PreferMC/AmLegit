package space.commandf1.amlegit.util;

import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockUtil {
    private static final double BLOCK_EDGE_OFFSET = 0.331D;

    public static Block getBlockAsync(final Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }

    public static Block getExactStandingBlock(Player player) {
        val location = player.getLocation().clone().subtract(0, 1, 0);
        var target = location.getBlock();

        if (!isPassable(target)) {
            return target;
        } else {
            // location.getBlockX()
        }

        return target;
    }

    public static boolean isPassable(final Block block) {
        if (block == null) {
            return false;
        }

        return isPassable(block.getType());
    }

    public static boolean isPassable(final Material material) {
        return isLiquid(material) || Material.SIGN == material
                || Material.WALL_SIGN == material
                || Material.TORCH == material
                || Material.LONG_GRASS == material
                || Material.WEB == material
                || Material.PORTAL == material
                || Material.AIR == material;
    }

    public static boolean isLiquid(final Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER
                || material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }
}
