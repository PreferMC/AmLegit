package space.commandf1.amlegit.util;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class PlayerUtil {
    public static int getPing(Player player) {
        int ping;
        try {
            Object entityPlayer = player.getClass().getDeclaredMethod("getHandle").invoke(player);
            Class<?> entityPlayerClass = entityPlayer.getClass();
            Field field = entityPlayerClass.getField("ping");
            ping = field.getInt(entityPlayer);
        } catch (Exception e) {
            return -1;
        }

        if (player.isValid() && (ping > 1000 || ping < 0)) {
            setPing(player, 50);
        }

        return ping;
    }

    public static void setPing(Player player, int ping) {
        try {
            Object entityPlayer = player.getClass().getDeclaredMethod("getHandle").invoke(player);
            Class<?> entityPlayerClass = entityPlayer.getClass();
            Field field = entityPlayerClass.getField("ping");
            field.set(entityPlayer, ping);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
