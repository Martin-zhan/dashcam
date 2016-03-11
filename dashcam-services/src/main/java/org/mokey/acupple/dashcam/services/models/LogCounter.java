package org.mokey.acupple.dashcam.services.models;

import java.util.concurrent.atomic.AtomicLong;

public class LogCounter {
	private int appId;
	private String envGroup;
	private long time;
	private AtomicLong total;
	private AtomicLong debug;
	private AtomicLong info;
	private AtomicLong warn;
	private AtomicLong error;
	private AtomicLong fatal;
	
	public LogCounter(){
		total = new AtomicLong(0);
		debug = new AtomicLong(0);
		info = new AtomicLong(0);
		warn = new AtomicLong(0);
		error = new AtomicLong(0);
		fatal = new AtomicLong(0);
	}

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

	public AtomicLong getTotal() {
		return total;
	}

	public void setTotal(AtomicLong total) {
		this.total = total;
	}

	public AtomicLong getDebug() {
		return debug;
	}

	public void setDebug(AtomicLong debug) {
		this.debug = debug;
	}

	public AtomicLong getInfo() {
		return info;
	}

	public void setInfo(AtomicLong info) {
		this.info = info;
	}

	public AtomicLong getWarn() {
		return warn;
	}

	public void setWarn(AtomicLong warn) {
		this.warn = warn;
	}

	public AtomicLong getError() {
		return error;
	}

	public void setError(AtomicLong error) {
		this.error = error;
	}

	public AtomicLong getFatal() {
		return fatal;
	}

	public void setFatal(AtomicLong fatal) {
		this.fatal = fatal;
	}

	public LogCounter add(LogCounter toBeAdd){
		this.total.addAndGet(toBeAdd.getTotal().get());
		this.debug.addAndGet(toBeAdd.getDebug().get());
		this.info.addAndGet(toBeAdd.getInfo().get());
		this.warn.addAndGet(toBeAdd.getWarn().get());
		this.error.addAndGet(toBeAdd.getError().get());
		this.fatal.addAndGet(toBeAdd.getFatal().get());
		return this;
	}

	@Override
	public String toString() {
		return "total=" + total.get() + ",debug=" + debug.get() + ",info=" + info.get() + ",warn=" + warn.get() + ",error=" + error.get() + ",fatal=" + fatal.get();
	}
}
