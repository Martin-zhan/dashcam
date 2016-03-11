package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

/**
 * The count of logs per hour of a specified appId
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_app_hcounter")
public class AppLogCounterHourly implements HBase {
    @Entity(family = "app_hcount")
    private int appId;

    @Entity(family = "app_hcount")
    private String envGroup;

    @Entity(family = "app_hcount")
    private long time;

    @Entity(family = "app_hcount", increment = true)
    private long total;

    @Entity(family = "app_hcount", increment = true)
    private long debug;

    @Entity(family = "app_hcount", increment = true)
    private long info;

    @Entity(family = "app_hcount", increment = true)
    private long warn;

    @Entity(family = "app_hcount", increment = true)
    private long error;

    @Entity(family = "app_hcount", increment = true)
    private long fatal;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getEnvGroup() {
        return envGroup;
    }

    public void setEnvGroup(String envGroup) {
        this.envGroup = envGroup;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getDebug() {
        return debug;
    }

    public void setDebug(long debug) {
        this.debug = debug;
    }

    public long getInfo() {
        return info;
    }

    public void setInfo(long info) {
        this.info = info;
    }

    public long getWarn() {
        return warn;
    }

    public void setWarn(long warn) {
        this.warn = warn;
    }

    public long getError() {
        return error;
    }

    public void setError(long error) {
        this.error = error;
    }

    public long getFatal() {
        return fatal;
    }

    public void setFatal(long fatal) {
        this.fatal = fatal;
    }

    @Override
    public byte[] getRowKey() {
        byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(appId)));
        byte[] appIdBytes = Bytes.toBytes(appId);
        byte[] envGroupHash = Bytes.toBytes(CamUtil.getHashCode(envGroup));

        return CamUtil.concat(appIdHash, appIdBytes, envGroupHash,
                Bytes.toBytes(AggregateUtil.getHourPart(time)));
    }
}
