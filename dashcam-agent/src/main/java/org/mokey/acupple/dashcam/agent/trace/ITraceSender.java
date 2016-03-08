package org.mokey.acupple.dashcam.agent.trace;

import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;
import org.mokey.acupple.dashcam.common.models.thrift.Span;

/**
 * Created by Yuan on 2015/6/17.
 */
public interface ITraceSender {
    /**
     * Send span to collector
     *
     * @param span
     */
    void send(Span span);

    /**
     * Send log event to collector
     *
     * @param logEvent
     */
    void send(LogEvent logEvent);
}
