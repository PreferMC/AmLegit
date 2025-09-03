package space.commandf1.amlegit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockUtil {
    public static Block getBlockAsync(final Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }

    public static Block getExactStandingBlock(Player player) {
        Location playerLoc = player.getEyeLocation();
        World world = player.getWorld();

        Vector direction = playerLoc.getDirection();
        direction.setY(-0.1);

        double checkDistance = 2.0;
        double stepSize = 0.2;

        Location checkLoc = playerLoc.clone();
        Block lastNonAirBlock = null;

        for (double d = 0; d <= checkDistance; d += stepSize) {
            checkLoc.add(direction.getX() * stepSize, direction.getY() * stepSize, direction.getZ() * stepSize);
            Block block = world.getBlockAt(checkLoc);

            if (block.getType() != Material.AIR) {
                if (!isPassable(block)) {
                    return block;
                }
                lastNonAirBlock = block;
            }
        }

        if (lastNonAirBlock != null) {
            return lastNonAirBlock;
        } else {
            return world.getBlockAt(playerLoc.getBlockX(), playerLoc.getBlockY() - 1, playerLoc.getBlockZ());
        }
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
                || Material.PORTAL == material;
    }

    public static boolean isLiquid(final Material material) {
        return material == Material.WATER || material == Material.STATIONARY_WATER
                || material == Material.LAVA || material == Material.STATIONARY_LAVA;
    }
}
