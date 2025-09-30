package com.sun.av.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLogAspect {

    /**
     * 定義切入點：攔截所有 com.sun.av.service 包下的方法
     * 匹配 com.sun.av.service 套件及以下套件下的所有方法
     */
    @Pointcut("execution(* com.sun.av.service..*(..))")
    public void serviceMethods() {}

    // 前置通知
//    @Before("serviceMethods()")
//    public void beforeAdvice(JoinPoint joinPoint) {
//        log.info("執行前：{}", joinPoint.getSignature());
//    }

    // 後置通知
//    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
//    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
//        log.info("執行後：{}，結果：{}", joinPoint.getSignature(), result);
//    }

    // 例外通知
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable ex) {
        log.info("發生例外：{}，例外：{}", joinPoint.getSignature(), ex.getMessage());
    }

    // 環繞通知
    @Around("serviceMethods()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        log.info("➡️ Around Service：{}，Start:{}", methodName, startTime);
        Object result = pjp.proceed(); // 執行原方法
        long endTime = System.currentTimeMillis();
        log.info("✅ Around Service：{}，End:{}，Cost：{} ms", methodName, endTime, (endTime - startTime));
        return result;
    }
}
