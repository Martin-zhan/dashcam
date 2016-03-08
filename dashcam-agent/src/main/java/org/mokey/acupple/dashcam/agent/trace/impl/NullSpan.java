package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.agent.trace.ISpan;
import org.mokey.acupple.dashcam.agent.trace.ITrace;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.Span;
import org.mokey.acupple.dashcam.common.models.thrift.SpanType;

import java.util.ArrayList;
import java.util.List;

/**
 * A Span that does nothing. Used to avoid returning and checking for nulls when we are not tracing.
 * Created by Yuan on 2015/6/17.
 */
public class NullSpan implements ISpan {
    private static NullSpan instance = new NullSpan();

    // No need to ever have more than one NullSpan.
    public static NullSpan getInstance() {
        return instance;
    }

    private NullSpan() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public long getStartTimeMillis() {
        return 0;
    }

    @Override
    public long getStopTimeMillis() {
        return 0;
    }

    @Override
    public long getAccumulateMillis() {
        return 0;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public String getDescription() {
        return "NullSpan";
    }

    @Override
    public long getSpanId() {
        return -1;
    }

    @Override
    public ISpan getParent() {
        return null;
    }

    @Override
    public long getTraceId() {
        return -1;
    }

    @Override
    public ISpan createChild(String name, String serviceName,
                             SpanType spanType, ITrace tracer) {
        return this;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    @Override
    public List<LogEvent> getLogEvents() {
        return new ArrayList<LogEvent>();
    }

    @Override
    public SpanType getSpanType() {
        return SpanType.OTHER;
    }

    @Override
    public Span getInnerSpan() {
        return null;
    }
}
