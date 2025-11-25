package ru.abramov.FinFlow.FinFlow.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Around("execution(public * ru.abramov.FinFlow.FinFlow.service..*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String methodName = ((MethodSignature) joinPoint.getSignature()).getName();

        long start = System.currentTimeMillis();
        logger.info("Вход в метод: {}", methodName);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            logger.error("Исключение в методе {}: {}", methodName, e.getMessage());
            throw e;
        }

        long elapsedTime = System.currentTimeMillis() - start;
        logger.info("Выход из метода: {}. Время выполнения: {} ms", methodName, elapsedTime);

        return result;
    }
}
