package space.commandf1.amlegit.util;

import org.bukkit.Bukkit;

public class NMSUtil {
    public static String getVersion() {
        return Bukkit.getServer().getClass().getName().replace("net.minecraft.server", "").replace("org.bukkit.craftbukkit.", "").replace("CraftServer", "").replace(".", "");
    }

    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName("net.minecraft.server." + getVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
