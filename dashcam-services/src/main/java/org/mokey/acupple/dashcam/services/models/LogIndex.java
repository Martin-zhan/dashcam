package org.mokey.acupple.dashcam.services.models;

import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;

import java.util.Map;

public class LogIndex {
	private String envGroup;
	private String env;
	private int appId;
	private long timestamp;
	private String traceId;
	private String spanId;
	private LogType logType;
	private LogLevel logLevel;
    private String source;
	private String title;
	private String hostIp;
	private String hostName;
	private byte[] rowkey;
	private Map<String, String> tags;

	public LogIndex() {
	}

	public LogIndex(int appId, String envGroup, String env, byte[] rowkey) {
		this.appId = appId;
		this.envGroup = envGroup;
		this.env = env;
		this.rowkey = rowkey;
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

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public LogType getLogType() {
		return logType;
	}

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public byte[] getRowkey() {
		return rowkey;
	}

	public void setRowkey(byte[] rowkey) {
		this.rowkey = rowkey;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

}
