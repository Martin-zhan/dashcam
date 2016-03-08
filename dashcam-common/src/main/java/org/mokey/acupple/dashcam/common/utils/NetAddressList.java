package org.mokey.acupple.dashcam.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 2015/7/2.
 */
public class NetAddressList {
    private List<NetAddress> addresses = new ArrayList<>();

    public NetAddressList(){}

    public NetAddressList(String hosts){
        this.add(hosts);
    }

    public void add(String host, int port){
        this.addresses.add(new NetAddress(host, port));
    }

    public void add(String host){
        String[] tokens = host.split(",");
        if(tokens.length > 0){
            for (String token : tokens){
                String[] hts = token.split(":");
                if(hts.length == 2){
                    this.add(hts[0], Integer.parseInt(hts[1]));
                }
            }
        }
    }

    public List<NetAddress> getAddresses() {
        return addresses;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (NetAddress address : addresses){
            if(first) {
                sb.append(address.toString());
                first = false;
            }else {
                sb.append("," + address.toString());
            }
        }

        return sb.toString();
    }
}
