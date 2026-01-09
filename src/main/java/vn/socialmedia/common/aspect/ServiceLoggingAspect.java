package vn.socialmedia.common.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging the execution of service layer methods.
 * <p>
 * This aspect intercepts all public methods in classes under
 * {@code org.example.iedservernew.service} package and logs:
 * <ul>
 *     <li>When a method starts execution</li>
 *     <li>When a method finishes execution</li>
 *     <li>When a method throws an exception</li>
 * </ul>
 * <p>
 * It is useful for debugging, monitoring, and tracking service method execution flow.
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ServiceLoggingAspect {

    /**
     * Pointcut that matches all public methods in the service package.
     * <p>
     * Specifically matches methods in:
     * {@code org.example.iedservernew.service.*}
     * </p>
     */
    @Pointcut("execution(public * vn.socialmedia.service.*.*(..))")
    public void allServiceMethods() {
        // Pointcut definition
    }

    /**
     * Logs before a service method starts execution.
     *
     * @param joinPoint provides reflective access to the method being invoked
     */
    @Before("allServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[Service method] {} started.", joinPoint.getSignature().getName());
    }

    /**
     * Logs after a service method has finished execution.
     *
     * @param joinPoint provides reflective access to the method being invoked
     */
    @After("allServiceMethods()")
    public void logAfter(JoinPoint joinPoint) {
        log.info("[Service method] {} finished.", joinPoint.getSignature().getName());
    }

    /**
     * Logs when a service method throws an exception during execution.
     *
     * @param joinPoint provides reflective access to the method being invoked
     * @param error     the exception that was thrown
     */
    @AfterThrowing(pointcut = "allServiceMethods()", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        log.error("[Service method] {} threw exception: {}", joinPoint.getSignature().getName(), error.getMessage());
        log.error(error.getMessage(), error);
    }
}
