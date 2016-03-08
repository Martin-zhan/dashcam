package org.mokey.acupple.dashcam.common;

/**
 * Created by Yuan on 2015/7/30.
 */
public class Constants {
    public static final String METRIC_TAG_APPID = "appid";

    public static final String METRIC_TAG_HOSTIP = "hostip";

    /**
     * Raw log kafka topic
     */
    public static final String MSG_TOPIC = System.getProperty("msg_topic",
            "com.dcf.iqunxing.fx.dashcam");

    /**
     * Environment(hostName, hostIp, env, envGroup) topic
     */
    public static final String ENV_TOPIC = "com.dcf.iqunxing.fx.dashcam.env";
}
