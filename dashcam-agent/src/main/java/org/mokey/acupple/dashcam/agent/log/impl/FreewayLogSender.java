package org.mokey.acupple.dashcam.agent.log.impl;


import org.mokey.acupple.dashcam.agent.conf.DashcamProperties;
import org.mokey.acupple.dashcam.agent.log.ILogSender;
import org.mokey.acupple.dashcam.agent.trace.ITrace;
import org.mokey.acupple.dashcam.agent.trace.impl.CLoggingTracer;
import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;

/**
 * Created by Yuan on 2015/6/18.
 */
public class FreewayLogSender implements ILogSender {
    private ITrace _tracer;

    public FreewayLogSender(ITrace tracer) {
        this._tracer = tracer;
    }

    @Override
    public void send(LogEvent logEvent) {
        // switch on or off logging by global setting
        if (DashcamProperties.GET().isAppLogEnabled() && isLogLevelEnabled(logEvent.getLogLevel())) {
            ((CLoggingTracer) _tracer).log(logEvent);
        }
    }

    private boolean isLogLevelEnabled(LogLevel level) {
        return level.getValue() >= DashcamProperties.GET().getLevel().getValue();
    }
}
