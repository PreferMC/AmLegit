package space.commandf1.amlegit.util;

public class SystemUtil {
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux") || os.contains("nix") || os.contains("nux");
    }
}
