package org.mokey.acupple.dashcam.agent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Yuan on 2015/7/14.
 */
public class Metrics {
    private AtomicInteger putQueueCounter = new AtomicInteger();
    private AtomicInteger pollQueueCounter = new AtomicInteger();
    private AtomicInteger sendLogEventCounter = new AtomicInteger();
    private AtomicInteger spanCounter = new AtomicInteger();
    private AtomicInteger sendSpanLogEventCounter = new AtomicInteger();

    private Metrics(){}
    private static Metrics metrics = new Metrics();

    public static Metrics instance(){
        return metrics;
    }

    public AtomicInteger getPutQueueCounter() {
        return putQueueCounter;
    }

    public AtomicInteger getPollQueueCounter() {
        return pollQueueCounter;
    }

    public AtomicInteger getSendLogEventCounter() {
        return sendLogEventCounter;
    }

    public AtomicInteger getSpanCounter(){return spanCounter; }

    public AtomicInteger getSendSpanLogEventCounter(){
        return sendSpanLogEventCounter;
    }


    public String toString(){
        return String.format("put=%s, poll=%s, logEvent=%s, span=%s, spanLogEvent=%s",
                Metrics.instance().getPutQueueCounter().get(),
                Metrics.instance().getPollQueueCounter().get(),
                Metrics.instance().getSendLogEventCounter().get(),
                Metrics.instance().getSpanCounter().get(),
                Metrics.instance().getSendSpanLogEventCounter().get());
    }
}
