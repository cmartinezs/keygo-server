package io.cmartinezs.keygo.run.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to exclude a method or an entire class from the
 * {@link KeyGoTracingAspect} input/output trace logging.
 *
 * <p>Use it on individual methods when only one operation in a class should be hidden:
 * <pre>{@code
 * @NoLog
 * public void sensitiveOperation(String rawSecret) { ... }
 * }</pre>
 *
 * <p>Use it on the class when the whole class must be excluded:
 * <pre>{@code
 * @NoLog
 * public class InternalCryptoHelper { ... }
 * }</pre>
 *
 * @author cmartinezs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface NoLog {
}

