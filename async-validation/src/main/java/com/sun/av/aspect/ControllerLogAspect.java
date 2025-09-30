package com.sun.av.aspect;

import com.sun.av.aspect.annotation.SensitiveLog;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    @PostConstruct
    public void init() {
        log.info("✅ ControllerLogAspect 已載入");
    }

    @Pointcut("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public void allControllers() {
    }

    @Around("allControllers()")
    public Object logExecutionDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        boolean isSensitive = method.isAnnotationPresent(SensitiveLog.class);
        boolean hideParams = isSensitive && method.getAnnotation(SensitiveLog.class).hideParams();

        long startTime = System.currentTimeMillis();
        log.info("➡️ Around Controller：{}，Start :{}", methodName, startTime);
        if (!hideParams) {
            log.info("📦 參數：{}", Arrays.toString(args));
        } else {
            log.info("📦 參數：已隱藏（敏感）");
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("❌ 方法執行錯誤：{}，錯誤訊息：{}", methodName, throwable.getMessage());
            throw throwable;
        }

        long endTime = System.currentTimeMillis();
        log.info("✅ Around Controller：{}，End:{}，Cost：{} ms", methodName, endTime, (endTime - startTime));

        return result;
    }


}
