package org.mokey.acupple.dashcam.agent.spring;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.mokey.acupple.dashcam.agent.TagBuilder;
import org.mokey.acupple.dashcam.agent.spring.annotations.Trace;
import org.mokey.acupple.dashcam.agent.spring.utils.PrettyFormat;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Forest on 2015/12/14.
 */
public class SpringTraceAopInterceptor extends CustomizableTraceInterceptor {
    private static final long serialVersionUID = 1L;

    public static final String ENTER_MESSAGE = "[$[targetType]] $[targetClassShortName].$[methodName]($[arguments])";
    public static final String EXIT_MESSAGE = ENTER_MESSAGE + " [$[returnValueCustom]] $[invocationTime]ms.";
    public static final String EXCEPTION_MESSAGE = ENTER_MESSAGE + " Exception! $[exception] $[invocationTime]ms.";

   /* private String enterMessage = ENTER_MESSAGE;*/
    private String exitMessage = EXIT_MESSAGE;
    private String exceptionMessage = EXCEPTION_MESSAGE;

    private static final Pattern PATTERN = Pattern.compile("\\$\\[\\p{Alpha}+\\]");

    public static final String PLACEHOLDER_RETURN_VALUE_CUSTOM = "$[returnValueCustom]";
    public static final String PLACEHOLDER_TARGET_TYPE = "$[targetType]";

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    protected String replacePlaceholders(String message, MethodInvocation methodInvocation,
                                         Object returnValue, Throwable throwable, long invocationTime) {

        Matcher matcher = PATTERN.matcher(message);

        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            if (PLACEHOLDER_METHOD_NAME.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(methodInvocation.getMethod().getName()));
            }
            else if (PLACEHOLDER_TARGET_TYPE.equals(match)) {
                String targetType = getTargetType(methodInvocation);
                matcher.appendReplacement(output, Matcher.quoteReplacement(targetType));
            }
            else if (PLACEHOLDER_TARGET_CLASS_NAME.equals(match)) {
                String className = getClassForLogging(methodInvocation.getThis()).getName();
                matcher.appendReplacement(output, Matcher.quoteReplacement(className));
            }
            else if (PLACEHOLDER_TARGET_CLASS_SHORT_NAME.equals(match)) {
                String shortName = ClassUtils.getShortName(getClassForLogging(methodInvocation.getThis()));
                matcher.appendReplacement(output, Matcher.quoteReplacement(shortName));
            }
            else if (PLACEHOLDER_ARGUMENTS.equals(match)) {
                matcher.appendReplacement(output,
                        Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(methodInvocation.getArguments())));
            }
            else if (PLACEHOLDER_ARGUMENT_TYPES.equals(match)) {
                appendArgumentTypes(methodInvocation, matcher, output);
            }
            else if (PLACEHOLDER_RETURN_VALUE_CUSTOM.equals(match)) {
                appendReturnValueCustom(methodInvocation, matcher, output, returnValue);
            }
            else if (PLACEHOLDER_RETURN_VALUE.equals(match)) {
                appendReturnValue(methodInvocation, matcher, output, returnValue);
            }
            else if (throwable != null && PLACEHOLDER_EXCEPTION.equals(match)) {
                matcher.appendReplacement(output, Matcher.quoteReplacement(throwable.toString()));
            }
            else if (PLACEHOLDER_INVOCATION_TIME.equals(match)) {
                matcher.appendReplacement(output, Long.toString(invocationTime));
            }
            else {
                throw new IllegalArgumentException("Unknown placeholder [" + match + "]");
            }
        }
        matcher.appendTail(output);

        return output.toString();
    }

    private String getTargetType(MethodInvocation methodInvocation) {
        Class<?> targetType = methodInvocation.getThis().getClass();
        if (hasAnnotation(targetType, Controller.class)) {
            return "Controller";
        } else if (hasAnnotation(targetType, Service.class)) {
            return "Service";
        } else if (hasAnnotation(targetType, Repository.class)) {
            return "Repository";
        } else {
            return "Trace";
        }
    }

    private boolean hasAnnotation(Class<?> targetType, Class<? extends Annotation> annotationType) {
        return AnnotationUtils.findAnnotation(targetType, annotationType) != null;
    }

    private void appendArgumentTypes(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output) {
        Class<?>[] argumentTypes = methodInvocation.getMethod().getParameterTypes();
        String[] argumentTypeShortNames = new String[argumentTypes.length];
        for (int i = 0; i < argumentTypeShortNames.length; i++) {
            argumentTypeShortNames[i] = ClassUtils.getShortName(argumentTypes[i]);
        }
        matcher.appendReplacement(output,
                Matcher.quoteReplacement(StringUtils.arrayToCommaDelimitedString(argumentTypeShortNames)));
    }

    private Map<String, String> getArguments(Object[] parameters){
        try {
            if (parameters != null && parameters.length > 0) {
                TagBuilder builder = TagBuilder.create();
                for (int i = 0; i < parameters.length; i++) {
                    builder.append("arg" + i, parameters[i].toString());
                }
                return builder.build();
            }
        }catch (Throwable e){}
        return null;
    }

    private void appendReturnValue(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {

        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        }
        else if (returnValue == null) {
            matcher.appendReplacement(output, "null");
        } else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(returnValue.toString()));
        }
    }

    private void appendReturnValueCustom(MethodInvocation methodInvocation, Matcher matcher, StringBuffer output, Object returnValue) {

        if (methodInvocation.getMethod().getReturnType() == void.class) {
            matcher.appendReplacement(output, "void");
        } else {
            matcher.appendReplacement(output, Matcher.quoteReplacement(PrettyFormat.toString(returnValue)));
        }
    }

    private boolean isTrace(MethodInvocation invocation){
        if(invocation.getMethod().getAnnotation(Trace.class) != null){
            return true;
        }
        if(invocation.getThis().getClass().getAnnotation(Trace.class) != null){
            return true;
        }

        return false;
    }

    @Override
    protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
        if(!isTrace(invocation)){
            return invocation.proceed();
        }
        TraceContext context = TraceContext.create(invocation);
        Tracer tracer = new Tracer(context);
        String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
        StopWatch stopWatch = new StopWatch(name);
        Object returnValue = null;
        Map<String, String> tags = getArguments(invocation.getArguments());
        boolean exitThroughException = false;
        try {
            stopWatch.start(name);
            returnValue = invocation.proceed();
            return returnValue;
        } catch (Throwable ex) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            exitThroughException = true;
            tracer.error(replacePlaceholders(exceptionMessage, invocation, null, ex, stopWatch.getTotalTimeMillis()), tags, ex);
            throw ex;
        } finally {
            if (!exitThroughException) {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
                tracer.log( replacePlaceholders(exitMessage, invocation, returnValue, null, stopWatch.getTotalTimeMillis()), tags);
            }
            tracer.stop();
        }
    }

    @Override
    protected boolean isLogEnabled(Log logger) {
        return logger.isInfoEnabled();
    }
}
