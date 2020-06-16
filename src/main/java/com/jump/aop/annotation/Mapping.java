package com.jump.aop.annotation;

import java.lang.annotation.*;

/**
 * 映射注解类
 *
 * @author Jump
 */
@Documented
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Mapping {

    String key();

    int length() default -1;

    String rex() default "";

    boolean delNull() default false;

    String sheetName() default "";
}
