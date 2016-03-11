package org.mokey.acupple.dashcam.services.hbase.models;

import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import org.mokey.acupple.dashcam.common.utils.CamUtil;
import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.HFxClient;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;
import org.mokey.acupple.dashcam.hbase.annotations.Table;
import org.mokey.acupple.dashcam.services.utils.LogId;

import java.util.Map;

/**
 * Created by enousei on 3/11/16.
 */
@Table(name = "dashcam_rawlog")
public class RawLog implements HBase{
    @Entity(family = "content")
    private int appId;

    @Entity(family = "content")
    private String envGroup;

    @Entity(family = "content")
    private String env;

    @Entity(family = "content")
    private String title;

    @Entity(family = "content")
    private long logtime;

    @Entity(family = "content")
    private long traceid;

    @Entity(family = "content")
    private long spanid;

    @Entity(family = "content")
    private int logtype;

    @Entity(family = "content")
    private int loglevel;

    @Entity(family = "content")
    private String source;

    @Entity(family = "content")
    private String message;

    @Entity(family = "content")
    private String hostip;

    @Entity(family = "content")
    private String hostname;

    @Entity(family = "tag")
    private Map<String, String> tags;

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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLogtime() {
        return logtime;
    }

    public void setLogtime(long logtime) {
        this.logtime = logtime;
    }

    public long getTraceid() {
        return traceid;
    }

    public void setTraceid(long traceid) {
        this.traceid = traceid;
    }

    public long getSpanid() {
        return spanid;
    }

    public void setSpanid(long spanid) {
        this.spanid = spanid;
    }

    public int getLogtype() {
        return logtype;
    }

    public void setLogtype(int logtype) {
        this.logtype = logtype;
    }

    public int getLoglevel() {
        return loglevel;
    }

    public void setLoglevel(int loglevel) {
        this.loglevel = loglevel;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public byte[] getRowKey() {
        byte[] appIdHash = Bytes.toBytes(CamUtil.getHashCode(String.valueOf(appId)));
        byte[] appIdBytes = Bytes.toBytes(appId);
        byte[] dayBytes = Bytes.toBytes(CamUtil.getRelativeDay(logtime));
        byte[] timeBytes = Bytes.toBytes(CamUtil.getRelativeMillSeconds(logtime));
        byte[] logIdBytes = Bytes.toBytes(LogId.getLogId().nextId());
        return CamUtil.concat(appIdHash, appIdBytes, dayBytes, timeBytes, logIdBytes);
    }

    public static void main(String[] args) throws Exception {
        HFxClient hFxClient = new HFxClient("zk3.s1.np.fx.dcfservice.com:2181,zk2.s1.np.fx.dcfservice.com:2181,zk1.s1.np.fx.dcfservice.com:2181", "/hbase");
        String ke = "NjwmwAAAA/YAAAErAkH30f///rANpoW3";
        byte[] rowkey = Base64.decode(ke);

        RawLog rawLog = hFxClient.get(rowkey, RawLog.class);

        System.out.println(rawLog.getMessage());
    }
}
