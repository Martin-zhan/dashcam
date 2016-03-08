package org.mokey.acupple.dashcam.agent.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by Yuan on 2015/6/17.
 */
public class RandomUtil {
    private static Random random = new SecureRandom();
    private static Random local;
    private static Random global = new Random();

    public static long nextLong() {
        long num = random.nextLong();
        if (num < 0) {
            num = -num;
        }
        return num;
    }

    public static double nextDouble() {
        return getLocalRandom().nextDouble();
    }

    private static Random getLocalRandom() {
        Random inst = local;
        if (inst == null) {
            int seed;
            synchronized (global) {
                seed = global.nextInt();
            }
            local = inst = new Random(seed);
        }
        return inst;
    }
}
