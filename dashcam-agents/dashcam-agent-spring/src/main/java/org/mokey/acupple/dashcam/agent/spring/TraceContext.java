package org.mokey.acupple.dashcam.agent.spring;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.mokey.acupple.dashcam.agent.spring.annotations.Trace;
import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;
import org.mokey.acupple.dashcam.common.models.thrift.SpanType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Forest on 2015/12/15.
 */
public class TraceContext {
    private static Map<String, TraceContext> cache = new HashMap<String, TraceContext>();
    private String traceName;
    private String spanName;
    private String serviceName;
    private LogType logType;
    private SpanType spanType;
    private LogLevel level;

    private TraceContext(){}

    /**
     * For mybatis trace interceptor
     * @param invocation
     * @return
     */
    public static TraceContext create(Invocation invocation){
        if(invocation == null || invocation.getMethod() == null){
           return new TraceContext();
        }

        TraceContext context = null;
        try{
            MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
            String sqlId = mappedStatement.getId();
            if(cache.containsKey(sqlId)) {
                context = cache.get(sqlId);
            }else {
                context = new TraceContext();
                context.setTraceName(sqlId);
                context.setServiceName(sqlId);
                context.setSpanName(invocation.getMethod().getName());
                context.setSpanType(SpanType.SQL);
                context.setLogType(LogType.SQL);
                context.setLevel(LogLevel.INFO);
                cache.put(sqlId, context);
            }
        }catch (Throwable ex){ }

        return context;
    }

    /**
     * For spring trace interceptor
     * @param methodInvocation
     * @return
     */
    public static TraceContext create( MethodInvocation methodInvocation){
        if(methodInvocation == null || methodInvocation.getMethod() == null){
            return new TraceContext();
        }

        TraceContext context = null;
        try{
            if(cache.containsKey(methodInvocation.toString())){
                context = cache.get(methodInvocation.toString());
            }else {
                context = new TraceContext();
                context.setTraceName(methodInvocation.getThis().getClass().getName());
                context.setServiceName(methodInvocation.getThis().getClass().getName());
                context.setSpanName(methodInvocation.getMethod().getName());

                Trace traceAnnotation = methodInvocation.getMethod().getAnnotation(Trace.class);
                if(traceAnnotation == null){
                    traceAnnotation = methodInvocation.getThis().getClass().getAnnotation(Trace.class);
                }
                if(traceAnnotation != null){
                    if(!traceAnnotation.traceName().isEmpty()) {
                        context.setTraceName(traceAnnotation.traceName());
                    }
                    context.setLogType(traceAnnotation.type());
                    context.setLevel(traceAnnotation.level());
                }

                if(context.getSpanType() == null){
                    if(context.getLogType() == LogType.WEB_SERVICE){
                        context.setSpanType(SpanType.WEB_SERVICE);
                    }else if(context.getLogType() == LogType.MEMCACHED){
                        context.setSpanType(SpanType.MEMCACHED);
                    }else if(context.getLogType() == LogType.SQL){
                        context.setSpanType(SpanType.SQL);
                    }else if(context.getLogType() == LogType.URL){
                        context.setSpanType(SpanType.SQL);
                    }else {
                        context.setSpanType(SpanType.OTHER);
                    }
                }

                cache.put(methodInvocation.toString(), context);
            }

        }catch (Throwable e){}

        return context;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public String getTraceName() {
        return traceName;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public void setTraceName(String traceName) {
        this.traceName = traceName;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public SpanType getSpanType() {
        return spanType;
    }

    public void setSpanType(SpanType spanType) {
        this.spanType = spanType;
    }
}
