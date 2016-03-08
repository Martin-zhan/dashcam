package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.trace.ISpan;
import org.mokey.acupple.dashcam.agent.trace.ITrace;
import org.mokey.acupple.dashcam.agent.utils.RandomUtil;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.Span;
import org.mokey.acupple.dashcam.common.models.thrift.SpanType;
import org.mokey.acupple.dashcam.common.utils.HostUtil;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.util.Collections;
import java.util.List;

/**
 *  A Span implementation that stores its information in milliseconds since the epoch.
 * Created by Yuan on 2015/6/17.
 */
public class MilliSpan implements ISpan {
    private final Span innerSpan;
    private final ISpan parent;
    private final CommonTracer tracer;

    public MilliSpan(String name, String serviceName, long id, ISpan parent, SpanType spanType, CommonTracer tracer) {
        this.innerSpan = new Span();
        if (Strings.isNullOrEmpty(name)) {
            this.innerSpan.setName("NoNameSpan");
        } else {
            this.innerSpan.setName(name);
        }
        if (Strings.isNullOrEmpty(serviceName)) {
            this.innerSpan.setServiceName("NoNameService");
        } else {
            this.innerSpan.setServiceName(serviceName);
        }
        this.innerSpan.setSpanId(id);
        this.innerSpan.setAppId(Integer.toString(DashcamProperties.GET().getAppId()));
        this.innerSpan.setSpanType(spanType);
        this.parent = parent;
        this.innerSpan.setParentId(getParentId());
        this.innerSpan.setTraceId(getTraceId());
        this.innerSpan.setStartTime(System.currentTimeMillis());
        this.innerSpan.setStopTime(0);
        this.innerSpan.setHostIp(HostUtil.getHostIp());
        this.innerSpan.setHostName(HostUtil.getHostName());
        this.innerSpan.setUnfinished(false);
        this.innerSpan.setThreadId(Thread.currentThread().getId());

        this.tracer = tracer;
    }

    @Override
    public void stop() {
        if (this.isStopped()) {
            // no effect, has been stopped
            return;
        }
        this.innerSpan.setStopTime(System.currentTimeMillis());
        tracer.pop(this);
    }

    @Override
    public boolean isStopped() {
        return this.innerSpan.getStopTime() != 0L;
    }

    @Override
    public long getAccumulateMillis() {
        if (this.innerSpan.getStartTime() == 0) {
            return 0;
        }
        if (this.innerSpan.getStopTime() > 0) {
            return this.innerSpan.getStopTime() - this.innerSpan.getStartTime();
        }
        return System.currentTimeMillis() - this.innerSpan.getStartTime();
    }

    @Override
    public boolean isRunning() {
        return this.innerSpan.getStartTime() != 0L && this.innerSpan.getStopTime() == 0L;
    }

    @Override
    public String toString() {
        long parentId = this.getParentId();
        return "\"" + this.getDescription() + "\" trace:" + this.getTraceId()
                + " span:" + this.innerSpan.getSpanId() + (parentId > 0 ? " parent:" + parentId : "")
                + " start:" + this.innerSpan.getStartTime() + " ms " + Long.toString(this.getAccumulateMillis()) + (this.isRunning() ? "..." : "");
    }

    @Override
    public String getDescription() {
        return "[" + this.innerSpan.getServiceName() + " : " + this.innerSpan.getName() + "]";
    }

    @Override
    public long getSpanId() {
        return this.innerSpan.getSpanId();
    }

    @Override
    public ISpan getParent() {
        return parent;
    }

    @Override
    public long getTraceId() {
        return this.parent.getTraceId();
    }

    @Override
    public ISpan createChild(String name, String serviceName, SpanType spanType, ITrace tracer) {
        return new MilliSpan(name, serviceName, RandomUtil.nextLong(), this, spanType, (CommonTracer) tracer);
    }

    @Override
    public long getParentId() {
        return this.parent.getSpanId();
    }

    @Override
    public List<LogEvent> getLogEvents() {
        return Collections.unmodifiableList(this.innerSpan.getLogEvents());
    }

    public void addLogEvent(LogEvent logEvent) {
        if (logEvent == null) return;
        logEvent.setTraceId(this.getTraceId());
        this.innerSpan.addToLogEvents(logEvent);
    }

    @Override
    public SpanType getSpanType() {
        return this.innerSpan.getSpanType();
    }

    @Override
    public Span getInnerSpan() {
        return this.innerSpan;
    }

    @Override
    public long getStartTimeMillis() {
        return this.getInnerSpan().getStartTime();
    }

    @Override
    public long getStopTimeMillis() {
        return this.getInnerSpan().getStopTime();
    }
}
