package org.mokey.acupple.dashcam.agent.spring;

import org.mokey.acupple.dashcam.agent.trace.ISpan;
import org.mokey.acupple.dashcam.agent.trace.ITrace;
import org.mokey.acupple.dashcam.agent.trace.TraceManager;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;

import java.util.Map;

/**
 * Created by Forest on 2015/12/15.
 */
public class Tracer {
    private TraceContext context;
    private ITrace trace;
    private ISpan span;
    private boolean isTrace;

    public Tracer(TraceContext context){
        this.context = context;
        this.trace = TraceManager.getTracer(context.getTraceName());
        if(!this.trace.isTracing()){
            this.isTrace = true;
        }
        this.span = this.trace.startSpan(context.getSpanName(), context.getServiceName(), context.getSpanType());
    }

    public void log(String message, Map<String, String> tags){
        try {
            if (message == null) {
                return;
            }
            if (this.trace == null) {
                return;
            }
            if (tags == null || tags.isEmpty()) {
                this.trace.log(context.getLogType(), context.getLevel(), context.getTraceName(), message);
            } else {
                this.trace.log(context.getLogType(), context.getLevel(), context.getTraceName(), message, tags);
            }
        }catch (Throwable ex){}
    }

    public void error(String message, Map<String, String> tags, Throwable e){
        try {
            if (this.trace == null) {
                return;
            }
            if (tags == null || tags.isEmpty()) {
                if (message == null) {
                    this.trace.log(context.getLogType(), LogLevel.ERROR, e);
                } else {
                    this.trace.log(context.getLogType(), LogLevel.ERROR, message, e);
                }
            } else {
                if (message == null) {
                    this.trace.log(context.getLogType(), LogLevel.ERROR, e, tags);
                } else {
                    this.trace.log(context.getLogType(), LogLevel.ERROR, message, e, tags);
                }
            }
        }catch (Throwable ex){}
    }

    public void stop(){
        if(span != null){
            span.stop();
            span = null; //必须值空
        }
        if(this.isTrace){
            trace.clear();
        }
    }
}
