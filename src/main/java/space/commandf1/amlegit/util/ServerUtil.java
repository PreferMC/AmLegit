package space.commandf1.amlegit.util;

public class ServerUtil {
    public static double getTps() {
        Class<?> minecraftServerClass = NMSUtil.getNMSClass("MinecraftServer");
        try {
            Object minecraftServer = minecraftServerClass.getDeclaredMethod("getServer").invoke(null);
            double[] recentTps = (double[]) minecraftServerClass.getDeclaredField("recentTps").get(minecraftServer);
            return recentTps[0];
        } catch (Exception e) {
            return -1;
        }
    }
}
