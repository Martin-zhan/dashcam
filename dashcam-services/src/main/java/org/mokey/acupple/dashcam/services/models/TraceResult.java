package org.mokey.acupple.dashcam.services.models;

import java.util.List;

/**
 * Created by Yuan on 2015/6/24.
 */
public class TraceResult {
	private int appId;
	private String appName;
	private String spanType;
    private String spanName;
	private String hostName;
	private String hostIp;
	private String serviceName;
	private long startTime;
	private long endTime;
	private List<String> rowkeys;
	private List<TraceResult> children;

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getSpanType() {
		return spanType;
	}

	public void setSpanType(String spanType) {
		this.spanType = spanType;
	}

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

    public List<String> getRowkeys() {
        return rowkeys;
    }

    public void setRowkeys(List<String> rowkeys) {
        this.rowkeys = rowkeys;
    }

    public List<TraceResult> getChildren() {
		return children;
	}

	public void setChildren(List<TraceResult> children) {
		this.children = children;
	}
}
