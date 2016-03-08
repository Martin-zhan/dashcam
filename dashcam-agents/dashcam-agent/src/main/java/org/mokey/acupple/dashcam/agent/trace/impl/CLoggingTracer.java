package org.mokey.acupple.dashcam.agent.trace.impl;

import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.trace.ITraceSender;
import org.mokey.acupple.dashcam.agent.utils.LogEventUtil;
import org.mokey.acupple.dashcam.agent.AgentManager;
import org.mokey.acupple.dashcam.agent.works.producers.ChunkEventProducer;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;
import org.mokey.acupple.dashcam.common.models.thrift.Span;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuan on 2015/6/17.
 */
public class CLoggingTracer extends CommonTracer {
    public CLoggingTracer(String name) {
        super(name);
        this.setSender(new CLoggingTraceSender());
    }

    static class CLoggingTraceSender implements ITraceSender {
        @Override
        public void send(Span span) {
            if (DashcamProperties.GET().getAppId() <= 0) {
                return;
            }
            // lazy initialization
            ChunkEventProducer messageProducer = AgentManager.getInstance().getMessageProducer();

            // switch on or off logging by global setting
            boolean traceEnabled = DashcamProperties.GET().isTraceEnabled();

            if (traceEnabled && messageProducer != null) {
                filterLogEventByLevel(span);
                for (LogEvent logEvent : span.getLogEvents()) {
                    LogEventUtil.truncateLogSize(logEvent, DashcamProperties.GET().getMaxMessageSize());
                }
                messageProducer.put(span);
            } else { // APP log should be sent out since it is a separate log type
                for (LogEvent logEvent : span.getLogEvents()) {
                    if (logEvent.getLogType() == LogType.APP) {
                        // truncate log event size
                        LogEventUtil.truncateLogSize(logEvent, DashcamProperties.GET().getMaxMessageSize());

                        // remove trace id since tracing is disabled
                        logEvent.setTraceId(0);
                        messageProducer.put(logEvent);
                    }
                }
            }
        }

        @Override
        public void send(LogEvent logEvent) {
            if (DashcamProperties.GET().getAppId() <= 0) {
                return;
            }

            // lazy initialization
            ChunkEventProducer messageProducer = AgentManager.getInstance().getMessageProducer();

            // switch on or off logging by global setting
            boolean traceEnabled = DashcamProperties.GET().isTraceEnabled();
            if (logEvent.getLogType() != LogType.APP) {
                if (traceEnabled) {
                    if (!this.isLogLevelEnabled(logEvent.getLogLevel())) {
                        return;
                    }
                } else {
                    return;
                }
            }

            if (!isLogLevelEnabled(logEvent.getLogLevel())) {
                return;
            }

            // truncate log event size
            LogEventUtil.truncateLogSize(logEvent, DashcamProperties.GET().getMaxMessageSize());
            messageProducer.put(logEvent);
        }

        private void filterLogEventByLevel(Span span) {
            if (span.getLogEvents() != null && span.getLogEvents().size() > 0) {
                List<LogEvent> tobeRemovedList = new ArrayList<LogEvent>();
                for (LogEvent logEvent : span.getLogEvents()) {
                    // app log has already been filtered by app logger, so we don't filter them again here
                    if (logEvent.getLogType() != LogType.APP &&
                            !isLogLevelEnabled(logEvent.getLogLevel())) {
                        tobeRemovedList.add(logEvent);
                    }
                }
                if (tobeRemovedList.size() > 0) {
                    span.getLogEvents().removeAll(tobeRemovedList);
                }
            }
        }

        private boolean isLogLevelEnabled(LogLevel level) {
            return level.getValue() >= DashcamProperties.GET().getLevel().getValue();
        }
    }
}
