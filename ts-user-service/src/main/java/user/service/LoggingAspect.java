
package user.service;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
// import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Arrays;
import java.util.Enumeration;
import javax.persistence.NonUniqueResultException;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LogManager.getLogger(LoggingAspect.class);

    // 拦截 service 实现层的所有方法
    @Around("execution(* user.service.*.*Impl.*(..))")
    public Object logServiceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            long startTime = System.currentTimeMillis();

            if (RequestContextHolder.getRequestAttributes() == null) {
                Object result = joinPoint.proceed(); //执行实际的方法
                return result;
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Entering in Method: ").append(joinPoint.getSignature().getName());
            logBuilder.append(", Class: ").append(joinPoint.getTarget().getClass().getName());
            logBuilder.append(", Arguments: ").append(Arrays.toString(joinPoint.getArgs()));
    
            // Only log headers if request is not null
            
            if (request != null) {
                logBuilder.append(", Request Headers: {");
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    logBuilder.append(headerName).append(": ").append(request.getHeader(headerName));
                    if (headerNames.hasMoreElements()) {
                        logBuilder.append(", ");
                    }
                }
                logBuilder.append("}");
            }
    
            Object result = joinPoint.proceed(); //执行实际的方法
    
            long elapsedTime = System.currentTimeMillis() - startTime;
            logBuilder.append(", Execution Time: ").append(elapsedTime).append(" milliseconds");
            logBuilder.append(", Return: ").append(result);
    
            // 打印一条日志
            logger.info(logBuilder.toString());
    
            return result;
        }   catch (NonUniqueResultException e) {
            logger.error("Non-unique result occurred for " + joinPoint.getSignature(), e);
            Object result = joinPoint.proceed(); //执行实际的方法
            return result;
        }
        
    }

    @Around("execution(* user.repository.*Repository.*(..))")
    public Object logRepositoryAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        logger.info("Before execution of repository method: {}", methodName);
    
        try {
            Object result = joinPoint.proceed(); // 执行方法
            long elapsedTime = System.currentTimeMillis() - startTime;
    
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Execution of repository method: ").append(methodName);
            logBuilder.append(", Execution Time: ").append(elapsedTime).append(" milliseconds");
            logBuilder.append(", Result: ").append(result);
    
            logger.info(logBuilder.toString());
    
            return result;
        } catch (Exception e) {
            logger.error("Exception in repository method: {}", methodName, e);
            throw e; // 重新抛出异常，让其能够被上游处理器捕获
        }
    }
}
