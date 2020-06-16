package com.jump.aop.constant;

/**
 * @author Jump
 * @date 2020/3/9 11:57
 */
public interface ConvertRegister<T, F, R> {

    /**
     * 转化
     *
     * @param targetObj targetObj
     * @param filedName filedName
     * @param filedVale filedVale
     * @return R
     */
    R convert(T targetObj, String filedName, F filedVale);
}
