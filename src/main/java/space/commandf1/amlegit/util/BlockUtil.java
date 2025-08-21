package space.commandf1.amlegit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.impl.PositionTracker;

import java.util.List;

public class BlockUtil {
    public static Block getBlockAsync(final Location location) {
        if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Block getBlockOnGround(final PlayerData playerData, double offset) {
        PositionTracker tracker = playerData.getTracker(PositionTracker.class).get();

        double[] offsets = {offset, -offset};
        for (double offset1 : offsets) {
            Location clone = tracker.getLastLocation().clone();
            List<Double> trys = List.of(tracker.getLocation().getY(), tracker.getLocation().getY() -1);
            for (Double aTry : trys) {
                clone.setY(aTry);

                clone.setX(clone.getX() + offset1);

                Block block = clone.getBlock();
                if (block != null && !BlockUtil.isPassable(block.getType()) && block.getType() != Material.AIR) {
                    return block;
                }
            }
        }

        for (double offset1 : offsets) {
            Location clone = tracker.getLastLocation().clone();
            List<Double> trys = List.of(tracker.getLocation().getY(), tracker.getLocation().getY() -1);
            for (Double aTry : trys) {
                clone.setY(aTry);
                clone.setZ(clone.getZ() + offset1);

                Block block = clone.getBlock();
                if (block != null && !BlockUtil.isPassable(block.getType()) && block.getType() != Material.AIR) {
                    return block;
                }
            }
        }

        return null;
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
