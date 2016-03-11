package org.mokey.acupple.dashcam.services.models;

import org.elasticsearch.search.SearchHit;

/**
 * Created by Yuan on 2015/7/3.
 */
public class LogIndexResult {
	private SearchHit[] hits;
	private boolean timeout = false;
    private long totalCount;

	public SearchHit[] getHits() {
		return hits;
	}

	public void setHits(SearchHit[] hits) {
		this.hits = hits;
	}

	public boolean isTimeout() {
		return timeout;
	}

	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
}
