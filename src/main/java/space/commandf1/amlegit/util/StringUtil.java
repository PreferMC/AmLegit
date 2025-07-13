package space.commandf1.amlegit.util;

import java.util.List;

public class StringUtil {
    public static String asString(List<String> list) {
        return String.join("\n", list);
    }

    public static String getPingString(int ping) {
        StringBuilder message = new StringBuilder();
        if (ping <= 100) {
            message.append("§a");
        } else if (ping <= 200) {
            message.append("§e");
        } else {
            message.append("§c");
        }

        message.append(ping);

        return message.toString();
    }

    public static String getPercentMessage(double percent, char percentChar, char containsColor, char notContainsColor, int percentCharNumber) {
        if (percent < 0.0) {
            percent  = 0.0;
        } else if (percent > 1.0) {
            percent = 1.0;
        }

        if (percentCharNumber <= 1) {
            throw new IllegalArgumentException("percentCharNumber must be greater than or equal to 2.");
        }

        StringBuilder builder = new StringBuilder("&" + containsColor);
        int targetNumber = (int) (percentCharNumber * percent);
        for (int i = 0; i < percentCharNumber; i++) {
            builder.append(percentChar);
            if (i == targetNumber) {
                builder.append("&").append(notContainsColor);
            }
        }

        return builder.toString();
    }
}
