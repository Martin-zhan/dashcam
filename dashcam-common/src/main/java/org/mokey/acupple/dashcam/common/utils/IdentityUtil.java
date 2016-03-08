package org.mokey.acupple.dashcam.common.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Yuan on 2015/7/30.
 */
public class IdentityUtil {
    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    /**
     * Close an Closeable, ignore the null input and the close exception
     *
     * @param in
     */
    public static void close(Closeable in) {
        if (null != in) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static Integer getRelativeDay(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return 365 - calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static Integer getRelativeMillSeconds(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        return Integer.valueOf(3600 * 24 * 1000 - (calendar
                .get(Calendar.HOUR_OF_DAY)
                * 3600
                * 1000
                + calendar.get(Calendar.MINUTE)
                * 60
                * 1000
                + calendar.get(Calendar.SECOND) * 1000 + calendar
                .get(Calendar.MILLISECOND)));
    }

    /**
     * Add on 2015-07-02, magic hash algorithm introduced by ZhaoQing.Chen
     *
     * @param str
     * @return
     */
    public static int getHashCode(String str) {
        int hash, i;
        char[] arr = str.toCharArray();
        for (hash = i = 0; i < arr.length; ++i) {
            hash += arr[i];
            hash += (hash << 12);
            hash ^= (hash >> 4);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        return hash;
    }

    private static AtomicLong logid = new AtomicLong();

    /**
     * Can generate 2^8 -1 (that is 255) unique id withn 1 ms per PC
     *
     * I think this is enough
     *
     * @return
     */
    public static long getUniqueID() {
        return (System.currentTimeMillis() << 8)
                | (logid.incrementAndGet() & 2 ^ 8 - 1);
    }
}
