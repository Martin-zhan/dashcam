package org.mokey.acupple.dashcam.services.models;

import java.io.Serializable;

public class PrimitiveLogCounter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4718601966406388599L;
	private int appId;
	private String envGroup;
	private long time;
	private long total;
	private long debug;
	private long info;
	private long warn;
	private long error;
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
	public String toString() {
		// TODO Auto-generated method stub
		return new StringBuilder("[appId:").append(appId).append(",")
				.append("envGroup:").append(envGroup).append(",")
				.append("total:").append(total).append(",").append("error:")
				.append(error).append(",").append("fatal:").append(fatal).append("]").toString();
	}
}
