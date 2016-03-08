package org.mokey.acupple.dashcam.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

/**
 * Created by enousei on 3/8/16.
 */
public class Strings {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    public static boolean isNullOrEmpty(String str){
        return str == null || str.isEmpty();
    }

    public static String toString(Throwable e){
        if(e == null){
            return "NA";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        return sw.toString();
    }

    public static String randomString(int len) {
        StringBuffer sb = new StringBuffer(len);
        for(int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }
}
