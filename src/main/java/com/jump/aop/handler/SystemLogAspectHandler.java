package com.jump.aop.handler;


import com.jump.aop.annotation.SystemLogAspect;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 日志切面处理
 *
 * @author Jump
 */
@Aspect
@Component
public class SystemLogAspectHandler {

    @Pointcut("@annotation(com.jump.aop.annotation.SystemLogAspect)")
    public void userAspect() {
    }

    /**
     * 切面处理方法
     *
     * @param joinPoint  切入点
     * @param annotation 自定义注解
     * @param result     方法boolean返回值
     */
    @AfterReturning(pointcut = "userAspect() && @annotation(annotation)", returning = "result")
    public void userAspectReturning(JoinPoint joinPoint, SystemLogAspect annotation, boolean result) {
        //获取参数，获取切入方法参数
        Object[] args = joinPoint.getArgs();

        //获取切面类型,获取注解值
        String value = annotation.value();
    }
}
