package org.mokey.acupple.dashcam.services.hbase.models;

import org.mokey.acupple.dashcam.hbase.HBase;
import org.mokey.acupple.dashcam.hbase.annotations.Entity;

/**
 * Created by enousei on 3/12/16.
 */
public abstract class CounterInfo implements HBase{
    @Entity(family = "count")
    protected int appId;

    @Entity(family = "count")
    protected String envGroup;

    @Entity(family = "count")
    protected long time;

    @Entity(family = "count", increment = true)
    protected long total;

    @Entity(family = "count", increment = true)
    protected long debug;

    @Entity(family = "count", increment = true)
    protected long info;

    @Entity(family = "count", increment = true)
    protected long warn;

    @Entity(family = "count", increment = true)
    protected long error;

    @Entity(family = "count", increment = true)
    protected long fatal;

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

    public void add(CounterInfo info){
        this.total += info.getTotal();
        this.info += info.getInfo();
        this.debug += info.getDebug();
        this.error += info.getError();
        this.warn += info.getWarn();
        this.fatal += info.getFatal();
    }

    public void setCountInfo(int appId, String envGroup, long time){
        this.appId = appId;
        this.envGroup = envGroup;
        this.time = time;
    }
}
