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
public class SensitiveLogAspect {

    @PostConstruct
    public void init() {
        log.info("✅ SensitiveLogAspect 已載入");
    }

    @Pointcut("@annotation(com.sun.av.aspect.annotation.SensitiveLog)") // 要指向實際 @interface 的位置
    public void sensitiveMethods() {}

    @Around("sensitiveMethods()")
    public Object handleSensitiveLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SensitiveLog annotation = method.getAnnotation(SensitiveLog.class);

        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        log.info("🔒 敏感方法執行：{}", methodName);

        if (!annotation.hideParams()) {
            log.info("📦 參數：{}", Arrays.toString(args));
        } else {
            log.info("📦 參數：已隱藏（敏感）");
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("❌ 錯誤：{}，訊息：{}", methodName, throwable.getMessage());
            throw throwable;
        }

        long endTime = System.currentTimeMillis();
        log.info("✅ 完成：{}，耗時：{} ms", methodName, (endTime - startTime));

        return result;
    }
}

