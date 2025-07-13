package space.commandf1.amlegit.util;

import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class ListUtil {
    public static Number getMin(List<? extends Number> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        double minValue = Double.POSITIVE_INFINITY;
        Number minNumber = null;

        for (Number num : list) {
            if (num != null) {
                double current = num.doubleValue();
                if (current < minValue) {
                    minValue = current;
                    minNumber = num;
                }
            }
        }

        return minNumber;
    }

    public static Number getMax(List<? extends Number> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        double maxValue = Double.NEGATIVE_INFINITY;
        Number maxNumber = null;

        for (Number num : list) {
            if (num != null) {
                double current = num.doubleValue();
                if (current > maxValue) {
                    maxValue = current;
                    maxNumber = num;
                }
            }
        }

        return maxNumber;
    }
}
