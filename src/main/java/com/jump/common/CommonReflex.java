package com.jump.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

/**
 * @author Jump
 * @date 2020/3/9 10:03
 */
public class CommonReflex {

    /**
     * 反射时获取所有父类成员
     *
     * @param tempClass tempClass
     * @return List<Field>
     */
    public static List<Field> getParentMember(Class<?> tempClass) {
        List<Field> fieldList = Lists.newArrayList();
        Set<String> fieldSet = Sets.newHashSet();
        //当父类为null的时候说明到达了最上层的父类(Object类).
        while (tempClass != null) {
            Field[] declaredFields = tempClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    //子类覆盖父类属性
                    if (!fieldSet.contains(field.getName())) {
                        fieldSet.add(field.getName());
                        fieldList.add(field);
                    }
                }
            }
            //得到父类,然后赋给自己
            tempClass = tempClass.getSuperclass();
        }
        return fieldList;
    }
}
