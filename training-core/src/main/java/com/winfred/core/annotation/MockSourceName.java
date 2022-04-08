package com.winfred.core.annotation;

import java.lang.annotation.*;

/**
 * @author kevin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MockSourceName {

    String name() default "";
}
