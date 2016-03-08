package org.mokey.acupple.dashcam.agent.conf;

import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;

/**
 * Created by enousei on 3/8/16.
 */
public class DashcamProperties {
    /**
     * TODO: USE clutch
     */
    private static DashcamProperties properties = new LocalDashcamProperties();

    public static DashcamProperties GET(){
        return properties;
    }

    /**
     * Get the app identity
     * @return
     */
    protected int appId = -1;
    public int getAppId(){
        return this.appId;
    }

    /**
     * Message collector(kafka clause) address list
     * @return
     */
    protected String brokerList = "127.0.0.1:9092";
    public String getBrokerList(){
        return this.brokerList;
    }

    /**
     * The lowest log level
     */
    protected volatile LogLevel level = LogLevel.INFO;
    public LogLevel getLevel(){
        return this.level;
    }

    /**
    * The switch of app log, if false, all log will be thrown away
    */
    protected volatile boolean appLogEnabled = true;
    public boolean isAppLogEnabled() {
        return appLogEnabled;
    }

    /**
     * The switch of trace feature.
     */
    protected volatile boolean traceEnabled = true;
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    /**
     * The size of each log total message(KB)
     */
    protected volatile short maxMessageSize = 32; //32K
    public short getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * The messages will be wrapped into a chunk, then send to remote collector.
     * Setting of how many messages one chunk contains
     */
    protected volatile int chunkSize = 50;
    public int getChunkSize() {
        return chunkSize;
    }

}
