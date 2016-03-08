package org.mokey.acupple.dashcam.common.utils;

/**
 * Created by Yuan on 2015/8/6.
 */
public class Calculator {
    /**
     * mod by shift
     *
     * @param val
     * @param bits
     * @return
     */
    public static long mod(long val, int bits) {
        return val - ((val >> bits) << bits);
    }

    /**
     * multiply by shift
     *
     * @param val
     * @param bits
     * @return
     */
    public static long mul(long val, int bits) {
        return val << bits;
    }

    /**
     * divide by shift
     *
     * @param val
     * @param bits
     * @return
     */
    public static long div(long val, int bits) {
        return val >> bits;
    }

}
