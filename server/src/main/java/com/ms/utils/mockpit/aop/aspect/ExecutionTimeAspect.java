package com.ms.utils.mockpit.aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ExecutionTimeAspect {

    private final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(com.ms.utils.mockpit.aop.interceptor.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();

        Object result = joinPoint.proceed();

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        LOGGER.info(
                joinPoint.getSignature() + " executed in " + TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " milliseconds"
        );

        return result;
    }
}
