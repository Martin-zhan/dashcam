package org.mokey.acupple.dashcam.services.models;

import org.mokey.acupple.dashcam.services.hbase.models.RawLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 2015/6/24.
 */
public class LogSearchResult {
	private List<RawLog> logs = new ArrayList<>();
	private long totalCount;
	private boolean isTimeOut = false;
	private boolean hasMoreResult = false;
	private int lastResultIndex;

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public List<RawLog> getLogs() {
		return logs;
	}

	public void setLogs(List<RawLog> logs) {
		this.logs = logs;
	}

	public boolean isTimeOut() {
		return isTimeOut;
	}

	public void setTimeOut(boolean isTimeOut) {
		this.isTimeOut = isTimeOut;
	}

	public boolean isHasMoreResult() {
		return hasMoreResult;
	}

	public void setHasMoreResult(boolean hasMoreResult) {
		this.hasMoreResult = hasMoreResult;
	}

	public int getLastResultIndex() {
		return lastResultIndex;
	}

	public void setLastResultIndex(int lastResultIndex) {
		this.lastResultIndex = lastResultIndex;
	}

}
