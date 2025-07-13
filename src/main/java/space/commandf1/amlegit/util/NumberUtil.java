package space.commandf1.amlegit.util;

import java.math.BigDecimal;

public class NumberUtil {
    public static boolean isLessThanZero(Number number) {
        if (number == null) {
            return false;
        }

        return new BigDecimal(number.toString()).compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isNumber(Object obj) {
        return obj instanceof Byte ||
                obj instanceof Short ||
                obj instanceof Integer ||
                obj instanceof Long ||
                obj instanceof Float ||
                obj instanceof Double;
    }

    public static boolean isInteger(Object obj) {
        return obj instanceof Byte ||
                obj instanceof Short ||
                obj instanceof Integer ||
                obj instanceof Long;
    }

    public static boolean isDecimal(Object obj) {
        return obj instanceof Float ||
                obj instanceof Double;
    }
}
