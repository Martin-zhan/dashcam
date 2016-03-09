package org.mokey.acupple.dashcam.agent.spring.annotations;

import org.mokey.acupple.dashcam.common.models.thrift.LogLevel;
import org.mokey.acupple.dashcam.common.models.thrift.LogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Forest on 2015/12/14.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {
    /**
     * Tracer name
     * @return
     */
    String traceName() default "";

    /**
     * Trace type
     * @return
     */
    LogType type() default LogType.WEB_SERVICE;

    /**
     * Log level
     * @return
     */
    LogLevel level() default LogLevel.INFO;
}