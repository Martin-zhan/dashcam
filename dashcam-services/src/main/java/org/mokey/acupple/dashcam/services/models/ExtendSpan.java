package org.mokey.acupple.dashcam.services.models;


import org.mokey.acupple.dashcam.common.models.thrift.Span;

import java.util.List;

public class ExtendSpan {
	private boolean isRoot;
	private long spanId;
	private long parentId;
	private Span span;
	private List<byte[]> rowkeys;

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public long getSpanId() {
		return spanId;
	}

	public void setSpanId(long spanId) {
		this.spanId = spanId;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public Span getSpan() {
		return span;
	}

	public void setSpan(Span span) {
		this.span = span;
	}

	public List<byte[]> getRowkeys() {
		return rowkeys;
	}

	public void setRowkeys(List<byte[]> rowkeys) {
		this.rowkeys = rowkeys;
	}
}
