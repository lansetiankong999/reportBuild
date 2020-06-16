package com.jump.utils.property;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author Jump
 * @date 2020/3/9 14:09
 */
public class EnBizEnumProperty extends AbstractBizEnumProperty {
    private static Map<String,Map> propertyMap = Maps.newHashMap();
    private static Map<String,List<Map<String,String>>> propertiesList = Maps.newHashMap();

    @Override
    Map<String, Map> getPropertiesMap() {
        return propertyMap;
    }


    @Override
    Map<String, List<Map<String, String>>> getPropertiesList() {
        return propertiesList;
    }
}
