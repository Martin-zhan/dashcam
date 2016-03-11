package org.mokey.acupple.dashcam.services.models;


import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2015/7/2.
 */
public class LogSearchParam {
	private String envGroup;
	private String env;
	private int appId;
	private String hostIp;
	private String hostName;
	private Date fromDate;
	private Date toDate;
	private String source;
	private String title;
	private String message;
	private Map<String, String> tags;
	private List<LogType> logTypes;
	private List<LogLevel> logLevels;
	private boolean traceOnly;

	public LogSearchParam() {
	}

	public LogSearchParam(String envGroup, String env, int appId,
			Date fromDate, Date toDate) {
		this.envGroup = envGroup;
		this.env = env;
		this.appId = appId;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.tags = new HashMap<>();
	}

	public boolean isTraceOnly() {
		return traceOnly;
	}

	public void setTraceOnly(boolean traceOnly) {
		this.traceOnly = traceOnly;
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

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public int getAppId() {
		return appId;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public Map<String, String> getTags() {
		return tags;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<LogType> getLogTypes() {
		return logTypes;
	}

	public void setLogTypes(List<LogType> logTypes) {
		this.logTypes = logTypes;
	}

	public List<LogLevel> getLogLevels() {
		return logLevels;
	}

	public void setLogLevels(List<LogLevel> logLevels) {
		this.logLevels = logLevels;
	}
}
