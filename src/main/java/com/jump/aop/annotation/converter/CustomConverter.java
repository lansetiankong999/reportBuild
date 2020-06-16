package com.jump.aop.annotation.converter;

import com.jump.aop.constant.ConvertRegister;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Jump
 * @date 2020/3/9 11:56
 */
@Inherited
@Documented
@Target({FIELD})
@Retention(RUNTIME)
public @interface CustomConverter {

    String fieldName() default "";

    String formatFieldName() default "";

    Class<? extends ConvertRegister> parser() default ConvertRegister.class;

    Class<? extends ConvertRegister> formatter() default ConvertRegister.class;
}
