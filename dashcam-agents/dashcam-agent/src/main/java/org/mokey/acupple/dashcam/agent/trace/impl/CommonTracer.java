package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.agent.trace.ISampler;
import org.mokey.acupple.dashcam.agent.trace.ISpan;
import org.mokey.acupple.dashcam.agent.trace.ITrace;
import org.mokey.acupple.dashcam.agent.trace.ITraceSender;
import org.mokey.acupple.dashcam.agent.utils.RandomUtil;
import org.mokey.acupple.dashcam.common.models.thrift.*;
import org.mokey.acupple.dashcam.common.utils.IdentityUtil;
import org.mokey.acupple.dashcam.common.utils.Strings;

import java.util.Map;

/**
 * Created by Yuan on 2015/6/17.
 */
public class CommonTracer implements ITrace {
    private ITraceSender sender;

    private static final ThreadLocal<ISpan> currentSpan = new ThreadLocal<ISpan>();
    private static final long ROOT_SPAN_ID = 0l;
    private String name;

    public CommonTracer(String name) {
        if (Strings.isNullOrEmpty(name)) {
            this.name = "defaultTraceName";
        } else {
            this.name = name;
        }
    }

    @Override
    public ISpan startSpan(String spanName, String serviceName, SpanType spanType, ISampler sampler) {
        if (!sampler.next()) {
            currentSpan.set(NullSpan.getInstance());
            return currentSpan.get();
        }
        ISpan parent = currentSpan.get();
        ISpan root;
        if (parent == null || parent instanceof NullSpan) {
            root = new RootMilliSpan(spanName, serviceName, RandomUtil.nextLong(),
                    RandomUtil.nextLong(), ROOT_SPAN_ID, spanType, this);
        } else {
            root = parent.createChild(spanName, serviceName, spanType, this);
        }
        push(root);

        return root;
    }

    @Override
    public ISpan startSpan(String spanName, String serviceName,
                           SpanType spanType) {
        return this.startSpan(spanName, serviceName, spanType, AlwaysSampler.getInstance());
    }

    @Override
    public boolean isTracing() {
        return currentSpan.get() != null && !(currentSpan.get() instanceof NullSpan);
    }

    private void push(ISpan span) {
        if (span != null) {
            currentSpan.set(span);

            LogEvent logEvent = new LogEvent();
            logEvent.setId(IdentityUtil.getUniqueID());
            logEvent.setLogType(spanToLogType(span.getSpanType()));
            logEvent.setTitle(span.getSpanType() + " Trace Span Start");
            logEvent.setMessage(span.getDescription());
            logEvent.setLogLevel(LogLevel.DEBUG);
            logEvent.setSource(name);
            logEvent.setThreadId(Thread.currentThread().getId());
            logEvent.setCreatedTime(System.currentTimeMillis());
            ((MilliSpan) span).addLogEvent(logEvent);
        }
    }

    private LogType spanToLogType(SpanType spanType) {
        if (spanType == SpanType.URL) {
            return LogType.URL;
        }
        if (spanType == SpanType.SQL) {
            return LogType.SQL;
        }
        if (spanType == SpanType.WEB_SERVICE) {
            return LogType.WEB_SERVICE;
        }
        if (spanType == SpanType.MEMCACHED) {
            return LogType.MEMCACHED;
        }
        return LogType.OTHER;
    }

    // check if span is on parent path of currSpan
    private boolean isOnParentPath(ISpan currSpan, ISpan span) {
        if (span == null)
            return false;
        int i = 0;
        while (currSpan != span && currSpan != null) {
            i++;
            if (i > 50) {
                log(LogType.OTHER, LogLevel.WARN, "possible unlimited loop in unfinished span handling.");
                break; // prevent possible unlimited loop
            }
            currSpan = currSpan.getParent();
        }

        if (currSpan == span)
            return true;

        return false;
    }

    protected void pop(ISpan span) {
        if (span != null) {
            if (span != currentSpan.get()) {
                log(LogType.OTHER, LogLevel.WARN, "Stopped span: " + span
                        + " was not the current span. current span is: "
                        + currentSpan.get());
                if (this.isOnParentPath(currentSpan.get(), span)) {
                    while (currentSpan.get() != span) {
                        // deliver unfinished span
                        ISpan currSpan = currentSpan.get();
                        currSpan.getInnerSpan().setStopTime(System.currentTimeMillis());
                        currSpan.getInnerSpan().setUnfinished(true);
                        deliver(currSpan);
                        currentSpan.set(currSpan.getParent());
                    }
                }
            }

            LogEvent logEvent = new LogEvent();
            logEvent.setId(IdentityUtil.getUniqueID());
            logEvent.setLogType(spanToLogType(span.getSpanType()));
            logEvent.setTitle(span.getSpanType() + " Trace Span Stop");
            logEvent.setMessage(span.getDescription());
            logEvent.setLogLevel(LogLevel.DEBUG);
            logEvent.setSource(name);
            logEvent.setThreadId(Thread.currentThread().getId());
            logEvent.setCreatedTime(System.currentTimeMillis());
            ((MilliSpan) span).addLogEvent(logEvent);

            currentSpan.set(span.getParent());

            deliver(span);
        } else {
            currentSpan.set(null);
        }
    }

    @Override
    public void clear() {
        int i = 0;
        while (currentSpan.get() != null && !(currentSpan.get() instanceof NullSpan)) {
            i++;
            if (i > 50) {
                log(LogType.OTHER, LogLevel.WARN, "possible unlimited loop in unfinished span handling");
                break; // prevent possible unlimited loop
            }
            // deliver unfinished span
            ISpan currSpan = currentSpan.get();
            currSpan.getInnerSpan().setStopTime(System.currentTimeMillis()); // mark the span as stopped
            currSpan.getInnerSpan().setUnfinished(true);
            deliver(currSpan);
            currentSpan.set(currSpan.getParent());
        }

        currentSpan.set(null);
    }

    @Override
    public ISpan continueSpan(String spanName, String serviceName,
                              long traceId, long parentId, SpanType spanType) {
        ISpan rootSpan = new RootMilliSpan(spanName, serviceName, traceId, RandomUtil.nextLong(), parentId, spanType, this);
        push(rootSpan);
        return rootSpan;
    }

    @Override
    public ISpan getCurrentSpan() {
        return currentSpan.get();
    }

    public void log(LogEvent logEvent) {
        if (logEvent.getId() <= 0) {
            logEvent.setId(IdentityUtil.getUniqueID());
        }
        if (logEvent.getLogType() == null) {
            logEvent.setLogType(LogType.APP);
        }
        if (logEvent.getCreatedTime() <= 0) {
            logEvent.setCreatedTime(System.currentTimeMillis());
        }
        if (logEvent.getThreadId() <= 0) {
            logEvent.setThreadId(Thread.currentThread().getId());
        }
        if (Strings.isNullOrEmpty(logEvent.getTitle())) {
            logEvent.setTitle("NA");
        }
        if (Strings.isNullOrEmpty(logEvent.getMessage())) {
            logEvent.setMessage("NA");
        }
        if (Strings.isNullOrEmpty(logEvent.getSource())) {
            logEvent.setSource(name);
        }
        if (this.isTracing()) {
            // attach the log event to current span
            Span innerSpan = currentSpan.get().getInnerSpan();
            logEvent.setTraceId(innerSpan.getTraceId());
            logEvent.setSpanId(innerSpan.getSpanId());

            ((MilliSpan)currentSpan.get()).addLogEvent(logEvent);
        } else {
            // deliver the logEvent directly if tracing is not enabled.
            deliver(logEvent);
        }
    }

    private void deliver(ISpan span) {
        if (sender != null) {
            sender.send(span.getInnerSpan());
        }
    }

    private void deliver(LogEvent logEvent) {
        if (sender != null) {
            sender.send(logEvent);
        }
    }

    private void log(LogType type, String title, String message, LogLevel logLevel, Map<String, String> attrs) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(IdentityUtil.getUniqueID());
        logEvent.setLogType(type);
        logEvent.setTitle(title);

        logEvent.setMessage(message);
        logEvent.setSource(name);
        logEvent.setAttributes(attrs);
        logEvent.setLogLevel(logLevel);

        log(logEvent);
    }

    @Override
    public void log(LogType type, LogLevel level, String title, String message) {
        this.log(type, title, message, level, null);
    }

    @Override
    public void log(LogType type, LogLevel level, String title, Throwable t) {
        String msg = "NullThrowable";
        if (t != null) {
            msg = Strings.toString(t);
        }
        this.log(type, title, msg, level, null);
    }

    @Override
    public void log(LogType type, LogLevel level, String title, String message, Map<String, String> attrs) {
        this.log(type, title, message, level, attrs);
    }

    @Override
    public void log(LogType type, LogLevel level, String message) {
        this.log(type, null, message, level, null);
    }

    @Override
    public void log(LogType type, LogLevel level, Throwable t) {
        String msg = "NullThrowable";
        if (t != null) {
            msg = Strings.toString(t);
        }
        this.log(type, null, msg, level, null);
    }

    @Override
    public void log(LogType type, LogLevel level, String message, Map<String, String> attrs) {
        this.log(type, null, message, level, attrs);
    }

    /**
     * Get a span or log event sender
     *
     * @return ITraceSender instance
     */
    public ITraceSender getSender() {
        return sender;
    }

    /**
     * Set a span or log event sender
     *
     * @param sender ITranceSender instance
     */
    public void setSender(ITraceSender sender) {
        this.sender = sender;
    }

    @Override
    public void log(LogType type, LogLevel level, String title, Throwable throwable,
                    Map<String, String> attrs) {
        String msg = "NullThrowable";
        if (throwable != null) {
            msg = Strings.toString(throwable);
        }
        this.log(type, title, msg, level, attrs);
    }

    @Override
    public void log(LogType type, LogLevel level, Throwable throwable,
                    Map<String, String> attrs) {
        String msg = "NullThrowable";
        if (throwable != null) {
            msg = Strings.toString(throwable);
        }
        this.log(type, null, msg, level, attrs);

    }
}
