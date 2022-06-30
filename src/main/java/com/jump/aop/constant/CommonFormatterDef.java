package com.jump.aop.constant;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * @author Jump
 * @date 2020/3/9 11:37
 */
public enum CommonFormatterDef {

    /**
     * 默认的格式
     */
    defaultFormatter(l -> l),
    @Deprecated
    decimalFormatter(l -> {
        if (l.equals(Constant.EMPTY_NUMBER_STR) || l.equals(Constant.ERROR_NUMBER_STR)) {
            return "";
        }
        String r = "";
        try {
            return r + new BigDecimal(l + "").movePointLeft(6).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }),

    @Deprecated
    pointFormatter(l -> {
        String x = decimalFormatter.function.apply(l) + "";
        if (x.isEmpty()) {
            return "";
        }
        return x + "%";
    }),

    @Deprecated
    mafDecimalFormatter(l -> {
        if (l.equals(Constant.EMPTY_NUMBER_STR) || l.equals(Constant.ERROR_NUMBER_STR)) {
            return "";
        }
        String r = "";
        try {
            return r + new BigDecimal(l + "").movePointLeft(10).stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    });

    private final Function<Object, Object> function;

    CommonFormatterDef(Function<Object, Object> function) {
        this.function = function;
    }

    public Function<Object, Object> getFunction() {
        return function;
    }
}
