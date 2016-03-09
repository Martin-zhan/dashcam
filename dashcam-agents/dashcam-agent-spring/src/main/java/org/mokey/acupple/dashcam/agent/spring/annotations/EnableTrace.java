package org.mokey.acupple.dashcam.agent.spring.annotations;

import org.mokey.acupple.dashcam.agent.spring.configuration.TraceRegistrar;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by Forest on 2015/12/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(TraceRegistrar.class)
public @interface EnableTrace {

	/**
	 * basePackages alias
	 * @return
	 */
	String[] value() default {};

	String[] basePackages() default {};

	Class<?>[] basePackageClasses() default {};

	boolean proxyTargetClass() default false;

	AdviceMode mode() default AdviceMode.PROXY;

	boolean pretty() default false;
}
