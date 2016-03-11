package org.mokey.acupple.dashcam.services.utils;

/**
 * Created by Yuan on 2015/7/30.
 */
public class AggregateUtil {
    public static long getMinutePart(long timestamp) {
        return timestamp / (60 * 1000) * (60 * 1000);
    }

    public static long getHourPart(long timestamp) {
        return timestamp / (60 * 60 * 1000) * (60 * 60 * 1000);
    }

    public static long getNextHourPart(long timestamp) {
        return getHourPart(timestamp) + 60 * 60 * 1000;
    }
}
