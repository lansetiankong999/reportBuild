package com.jump.aop.annotation.converter;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Jump
 * @date 2020/3/9 11:59
 */
@Inherited
@Documented
@Target({FIELD})
@Retention(RUNTIME)
public @interface DateConverter {

    String fieldName() default "";

    String formatFieldName() default "";

    String value() ;

    String formatter() default "";
}
