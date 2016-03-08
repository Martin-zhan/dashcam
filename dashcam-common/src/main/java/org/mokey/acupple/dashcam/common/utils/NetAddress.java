package org.mokey.acupple.dashcam.common.utils;

/**
 * Created by Yuan on 2015/7/2.
 */
public class NetAddress {
    private String host;
    private int port;

    public NetAddress(String host,int port){
        this.host = host;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return getHost() + ":" + getPort();
    }
}
