package com.jump.utils.property;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Jump
 * @date 2020/3/9 14:05
 */
@Component("bizEnumProperty")
public class BizEnumProperty {

    public static String CHINESE = "cn";
    public static String TRADITIONAL_CHINESE = "tra";
    public static String ENGLISH = "en";

    private static CnBizEnumProperty cnEnumProperty;
    private static TraBizEnumProperty traEnumProperty;
    private static EnBizEnumProperty enEnumProperty;

    public static Map getBizPropertys(String mapKey, String locale) {
        Map properties = null;
        boolean b = StringUtils.isBlank(locale) || CHINESE.equals(locale);
        if(b && cnEnumProperty != null){
            properties =  cnEnumProperty.getBizPropertys(mapKey);
        }else if(TRADITIONAL_CHINESE.equals(locale) && traEnumProperty != null){
            properties =  traEnumProperty.getBizPropertys(mapKey);
        }else if(ENGLISH.equals(locale) && enEnumProperty != null){
            properties =  enEnumProperty.getBizPropertys(mapKey);
        }else if(cnEnumProperty != null){
            properties =  cnEnumProperty.getBizPropertys(mapKey);
        }
        return properties;
    }

    public  void setCnEnumProperty(CnBizEnumProperty cnEnumProperty) {
        BizEnumProperty.cnEnumProperty = cnEnumProperty;
    }
    public  void setTraEnumProperty(TraBizEnumProperty traEnumProperty) {
        BizEnumProperty.traEnumProperty = traEnumProperty;
    }
    public  void setEnEnumProperty(EnBizEnumProperty enEnumProperty) {
        BizEnumProperty.enEnumProperty = enEnumProperty;
    }
}
