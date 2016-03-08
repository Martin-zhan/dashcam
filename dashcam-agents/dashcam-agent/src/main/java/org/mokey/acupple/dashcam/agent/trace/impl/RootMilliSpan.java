package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.common.models.thrift.SpanType;

/**
 * Span that roots the span tree.
 * Created by Yuan on 2015/6/17.
 */
public class RootMilliSpan extends MilliSpan{
    private final long traceId;
    private final long parentId;

    public RootMilliSpan(String name, String serviceName, long traceId, long spanId, long parentId, SpanType spanType, CommonTracer tracer) {
        super(name, serviceName, spanId, null, spanType, tracer);
        this.traceId = traceId;
        this.getInnerSpan().setTraceId(traceId);
        this.parentId = parentId;
        this.getInnerSpan().setParentId(parentId);
    }

    public long getTraceId() {
        return traceId;
    }

    public long getParentId() {
        return parentId;
    }
}
