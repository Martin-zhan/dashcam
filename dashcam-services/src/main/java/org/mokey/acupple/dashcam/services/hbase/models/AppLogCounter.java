package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.AggregateUtil;

/**
 * the count of logs per minute of a specified appId
 * Created by enousei on 3/10/16.
 */
@Table(name = "dashcam_app_counter")
public class AppLogCounter implements HBase{
    @Entity(family = "app_mcount")
    private int appId;

    @Entity(family = "app_mcount")
    private String envGroup;

    @Entity(family = "app_mcount")
    private long time;

    @Entity(family = "app_mcount", increment = true)
    private long total;

    @Entity(family = "app_mcount", increment = true)
    private long debug;

    @Entity(family = "app_mcount", increment = true)
    private long info;

    @Entity(family = "app_mcount", increment = true)
    private long warn;

    @Entity(family = "app_mcount", increment = true)
    private long error;

    @Entity(family = "app_mcount", increment = true)
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
                Bytes.toBytes(AggregateUtil.getMinutePart(time)));
    }

    public static void main(String[] args) throws Exception{
        HFxClient client = new HFxClient("127.0.0.1:2181", "/hbase");
        client.createTable(AppLogCounter.class);

        AppLogCounter counter = new AppLogCounter();
        counter.setAppId(9000);
        counter.setEnvGroup("FTA");
        counter.setTime(AggregateUtil.getMinutePart(System
                .currentTimeMillis()));
        counter.setInfo(100);

        client.insert(counter);

        AppLogCounter counter1 = client.get(counter.getRowKey(), AppLogCounter.class);
        System.out.println(counter1.getInfo());

        counter.setInfo(200);
        client.increment(counter);

        counter1 = client.get(counter.getRowKey(), AppLogCounter.class);
        System.out.println(counter1.getInfo());
    }
}
