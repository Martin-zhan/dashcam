package org.mokey.acupple.dashcam.agent.log;

import org.mokey.acupple.dashcam.common.models.thrift.LogEvent;

/**
 * Log sender will send log or metric event to a target
 * Created by Yuan on 2015/6/17.
 */
public interface ILogSender {
    /**
     * Send log event
     * @param logEvent
     */
    void send(LogEvent logEvent);
}
