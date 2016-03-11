package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;

/**
 * Created by enousei on 3/10/16.
 */
@Table(name = "dashcam_host")
public class LogHost implements HBase{
    @Entity(family = "hostinfo")
    private int appId;

    @Entity(family = "hostinfo")
    private String envgroup;

    @Entity(family = "hostinfo")
    private String env;

    @Entity(family = "hostinfo")
    private String hostip;

    @Entity(family = "hostinfo")
    private String hostname;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getEnvgroup() {
        return envgroup;
    }

    public void setEnvgroup(String envgroup) {
        this.envgroup = envgroup;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getHostip() {
        return hostip;
    }

    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public byte[] getRowKey() {
        byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(appId)));
        byte[] appIdBytes = Bytes.toBytes(appId);
        byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envgroup));
        byte[] envHash = Bytes.toBytes(CamUtil.getHashCode(env));
        byte[] hostIpHash = Bytes.toBytes(CamUtil.getHashCode(hostip));

        return CamUtil.concat(appIdHash, appIdBytes, envGroupHash, envHash, hostIpHash);
    }
}
