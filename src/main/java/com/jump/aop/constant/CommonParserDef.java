package com.jump.aop.constant;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * @author Jump
 * @date 2020/3/9 11:37
 */
public enum CommonParserDef {

    /**
     * 默认的转换
     */
    defaultParser(l -> l),

    @Deprecated
    decimalParser(l -> {
        if (null == l) {
            return null;
        }
        String r = "";
        try {
            return r + new BigDecimal(l + "").movePointRight(6);
        } catch (Exception e) {
            return null;
        }
    }),


    @Deprecated
    pointParser(l -> null == l ? "" : decimalParser.function.apply((l + "").replaceAll("%$", ""))),

    @Deprecated
    mafDecimalParser(l -> {
        if (null == l) {
            return Constant.EMPTY_NUMBER_STR;
        }
        String r = "";
        try {
            return r + new BigDecimal(l + "").movePointRight(10);
        } catch (Exception e) {
            return Constant.ERROR_NUMBER_STR;
        }
    });

    private final Function function;

    private CommonParserDef(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
