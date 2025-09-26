package com.sun.av.aspect.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveLog {

    // 可擴充屬性，例如是否完全不記錄參數
    boolean hideParams() default true;

}
