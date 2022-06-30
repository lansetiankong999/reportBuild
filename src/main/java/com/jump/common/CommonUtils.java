package com.jump.common;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jump
 * @date 2020/3/9 11:28
 */
public class CommonUtils {

    /**
     * 判断一个对象是否是基本类型或基本类型的封装类型
     */
    public static boolean isPrimitive(Object obj) {
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Date) {
            return true;
        }
        if (obj instanceof BigDecimal) {
            return true;
        }

        try {
            return ((Class<?>) obj.getClass().getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Integer> numberCompose(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("非法参数");
        }
        int base = 2;
        List<Integer> ret = Lists.newArrayList();
        int power = (int) (Math.log(number) / Math.log(base));
        ret.add(power);
        while ((number = number - (int) Math.pow(base, power)) > 0) {
            power = (int) (Math.log(number) / Math.log(base));
            ret.add(power);
        }
        Collections.reverse(ret);
        ret = ret.stream().map(x -> (int) Math.pow(base, x)).collect(Collectors.toList());
        return ret;
    }
}
