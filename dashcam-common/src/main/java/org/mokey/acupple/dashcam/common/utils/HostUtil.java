package org.mokey.acupple.dashcam.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Yuan on 2015/7/30.
 */
public class HostUtil {
    private static String hostName = "Unknown";
    private static String hostIp = "0.0.0.0";

    static {
        try {
            boolean assigned = false;
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface)allNetInterfaces.nextElement();
                Enumeration addresses = netInterface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                        hostName = ip.getHostName();
                        hostIp = ip.getHostAddress();
                        if(hostName == null || hostName.equals(hostIp)){
                            try {
                                hostName = (InetAddress.getLocalHost()).getHostName();
                            }catch (Exception e){}
                        }
                        assigned = true;
                        break;
                    }
                }
                if (assigned) {
                    break;
                }
            }
        } catch (SocketException e) {
            // do nothing
        }
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getHostIp() {
        return hostIp;
    }
}
