package com.jump.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jump.aop.annotation.converter.*;
import com.jump.aop.constant.CommonFormatterDef;
import com.jump.aop.constant.ConvertRegister;
import com.jump.common.CommonReflex;
import com.jump.common.CommonUtils;
import com.jump.utils.property.BizEnumProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jump
 */
@Slf4j
public class EntityFormatter {

    private static final Map<String, Function<Object, Object>> FORMATTERS = Maps.newHashMap();


    public static void registerFormatHandler(String formatterName, Function<Object, Object> formatterHandler) {
        FORMATTERS.put(formatterName, formatterHandler);

    }

    public static void registerFormatHandlers(Map<String, Function<Object, Object>> maps) {
        FORMATTERS.putAll(maps);

    }

    public static Map<String, Function<Object, Object>> getFormatters() {
        return FORMATTERS;
    }


    public static <T> String formatStringProperty(T t, String propertyName) {

        return formatStringProperty(t, propertyName, null);
    }

    public static <T> Object formatProperty(T t, String propertyName) {

        return formatProperty(t, propertyName, null);
    }

    public static <T> String formatStringProperty(T t, String propertyName, String locale) {
        Object o = formatProperty(t, propertyName, locale);
        if (o != null) {
            return o + "";
        }
        return null;
    }

    public static <T> Object formatProperty(T t, String propertyName, String locale) {
        if (t == null) {
            return null;
        }
        Field field = getField(t.getClass(), propertyName);
        field.setAccessible(true);
        Object value = null;

        try {
            value = field.get(t);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return getConvertValue(t, true, field, value, locale);
    }

    public static <T> Map<String, Object> format(T t) {
        return format(t, true, null);
    }

    public static <T> Map<String, Object> format(T t, boolean keepOrigin) {
        return format(t, keepOrigin, null);
    }

    public static <T> Map<String, Object> format(T t, String locale) {
        return format(t, true, locale);
    }

    public static <T> Map<String, Object> format(T t, boolean keepOrigin, String locale) {
        return format(t, keepOrigin, null, null, locale);
    }


    public static <T> List<Map<String, Object>> format(List<T> list) {

        return format(list, true, null);

    }

    public static <T> List<Map<String, Object>> format(List<T> list, boolean keepOrigin) {

        return format(list, keepOrigin, null);
    }

    public static <T> List<Map<String, Object>> format(List<T> list, String locale) {

        return format(list, true, locale);
    }


    public static <T> List<Map<String, Object>> format(List<T> list, boolean keepOrigin, String locale) {

        List<Map<String, Object>> retList = Lists.newArrayList();
        if (list == null) {
            return retList;
        }
        list.forEach(x -> {
            retList.add(format(x, keepOrigin, null, null, locale));
        });
        return retList;
    }


    public static <T> Map<String, Object> formatInclude(T t, Set<String> includeSet) {
        return formatInclude(t, true, includeSet, null);
    }

    public static <T> Map<String, Object> formatInclude(T t, boolean keepOrigin, Set<String> includeSet) {
        return formatInclude(t, keepOrigin, includeSet, null);
    }

    public static <T> Map<String, Object> formatInclude(T t, Set<String> includeSet, String locale) {
        return formatInclude(t, true, includeSet, locale);
    }

    public static <T> Map<String, Object> formatInclude(T t, boolean keepOrigin, Set<String> includeSet, String locale) {
        return format(t, keepOrigin, includeSet, null, locale);
    }


    public static <T> Map<String, Object> formatExclude(T t, Set<String> excludeSet) {
        return formatExclude(t, true, excludeSet, null);
    }

    public static <T> Map<String, Object> formatExclude(T t, boolean keepOrigin, Set<String> excludeSet) {
        return formatExclude(t, keepOrigin, excludeSet, null);
    }

    public static <T> Map<String, Object> formatExclude(T t, Set<String> excludeSet, String locale) {
        return formatExclude(t, true, excludeSet, locale);
    }


    public static <T> Map<String, Object> formatExclude(T t, boolean keepOrigin, Set<String> excludeSet, String locale) {
        return format(t, keepOrigin, null, excludeSet, locale);
    }


    public static <T> List<Map<String, Object>> formatInclude(List<T> list, Set<String> includeSet) {

        return formatInclude(list, true, includeSet, null);
    }

    public static <T> List<Map<String, Object>> formatInclude(List<T> list, boolean keepOrigin, Set<String> includeSet) {

        return formatInclude(list, keepOrigin, includeSet, null);
    }

    public static <T> List<Map<String, Object>> formatInclude(List<T> list, Set<String> includeSet, String locale) {
        return formatInclude(list, true, includeSet, locale);
    }

    public static <T> List<Map<String, Object>> formatInclude(List<T> list, boolean keepOrigin, Set<String> includeSet, String locale) {
        List<Map<String, Object>> retList = Lists.newArrayList();
        list.forEach(x -> retList.add(format(x, true, includeSet, null, locale)));
        return retList;
    }


    public static <T> List<Map<String, Object>> formatExclude(List<T> list, Set<String> excludeSet) {
        return formatExclude(list, true, excludeSet, null);
    }

    public static <T> List<Map<String, Object>> formatExclude(List<T> list, boolean keepOrigin, Set<String> excludeSet) {
        return formatExclude(list, keepOrigin, excludeSet, null);
    }

    public static <T> List<Map<String, Object>> formatExclude(List<T> list, Set<String> excludeSet, String locale) {
        return formatExclude(list, true, excludeSet, locale);
    }


    public static <T> List<Map<String, Object>> formatExclude(List<T> list, boolean keepOrigin, Set<String> excludeSet, String locale) {
        List<Map<String, Object>> retList = Lists.newArrayList();
        list.forEach(x -> {
            retList.add(format(x, keepOrigin, null, excludeSet, locale));
        });
        return retList;
    }


    /**
     * @param t          t
     * @param includeSet 不能同时存在 includeSet 和 includeSet，如同时存在则只有includeSet生效，不直接对外提供该接口
     * @param excludeSet excludeSet
     * @param locale     locale
     * @param <T>        <T>
     * @return <T>
     */
    private static <T> Map<String, Object> format(T t, boolean keepOrigin, Set<String> includeSet, Set<String> excludeSet, String locale) {
        Map<String, Object> ret = Maps.newHashMap();
        if (t == null) {
            return ret;
        }
        if (t instanceof Collection) {
            throw new IllegalArgumentException("不支持该类型！");
        }

        List<Field> fieldList = CommonReflex.getParentMember(t.getClass());
        fieldList.forEach(x -> {
            x.setAccessible(true);
            String fieldName = x.getName();
            Object originalValue = null;
            Object value = null;

            try {
                originalValue = x.get(t);
                value = originalValue;

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (includeSet == null && excludeSet == null) {
                value = getConvertValue(t, keepOrigin, x, value, locale);
            } else if (includeSet != null) {
                value = includeSet.contains(fieldName) ? getConvertValue(t, keepOrigin, x, value, includeSet, null, locale) : value;
            } else {
                value = !excludeSet.contains(fieldName) ? getConvertValue(t, keepOrigin, x, value, null, excludeSet, locale) : value;
            }

            //转换key
            fieldName = getFieldTransferName(x);
            //判断是否为同一个对象
            if (keepOrigin && originalValue != value) {
                ret.put("original_" + fieldName, originalValue);
            }
            ret.put(fieldName, value);
        });

        if (keepOrigin) {
            ret.put("_formatter_", true);
        }
        return ret;

    }

    private static <T> Object getConvertValue(T t, boolean keepOrigin, Field field, Object value, String locale) {
        return getConvertValue(t, keepOrigin, field, value, null, null, locale);

    }

    private static <T> Object getConvertValue(T t, boolean keepOrigin, Field field, Object value, Set<String> includeSet, Set<String> excludeSet, String locale) {
        if (value == null) {
            return null;
        }
        Object retList = insertRetList(keepOrigin, value, includeSet, excludeSet, locale);
        if (retList != null) {
            return retList;
        } ;
        Object targetVal = value;
        field.setAccessible(true);
        String fieldName = field.getName();
        if (field.isAnnotationPresent(CommonConverter.class)) {
            CommonFormatterDef formatter = field.getDeclaredAnnotation(CommonConverter.class).formatter();
            Function<Object, Object> function = formatter.getFunction();
            if (function != null) {
                try {
                    targetVal = function.apply(value);
                } catch (Exception e) {
                    log.info("[{}.{}] 格式化异常,将忽略该属性！", t.getClass().getName(), field.getName(), e);
                }
            }
        } else if (field.isAnnotationPresent(CustomConverter.class)) {
            Class<? extends ConvertRegister> customConverter = field.getDeclaredAnnotation(CustomConverter.class).formatter();
            try {
                if (!customConverter.getName().equals(ConvertRegister.class.getName())) {
                    ConvertRegister convertRegister = customConverter.getDeclaredConstructor().newInstance();
                    targetVal = convertRegister.convert(t, fieldName, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("[{}.{}] 格式化异常,将忽略该属性！", t.getClass().getName(), field.getName(), e);
            }
        } else if (field.isAnnotationPresent(EnumConverter.class)) {
            String name = field.getDeclaredAnnotation(EnumConverter.class).formatter();
            if (StringUtils.isBlank(name)) {
                name = field.getDeclaredAnnotation(EnumConverter.class).value();
            }
            Map<String, Object> bizProperty = BizEnumProperty.getBizProperty(name, locale);
            if (bizProperty != null) {
                Object o = bizProperty.get(value + "");
                if (o != null) {
                    targetVal = o;
                }
            }
        } else if (field.isAnnotationPresent(DateConverter.class)) {
            String pattern = field.getDeclaredAnnotation(DateConverter.class).formatter();
            if (StringUtils.isBlank(pattern)) {
                pattern = field.getDeclaredAnnotation(DateConverter.class).value();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            try {
                if (value instanceof String) {
                    targetVal = dateFormat.parse((String) value);
                } else {
                    targetVal = dateFormat.format(value);
                }
            } catch (Exception e) {
                log.info("[{}.{}] 格式化异常,将忽略该属性！", t.getClass().getName(), field.getName(), e);
            }

        } else if (field.isAnnotationPresent(MultEnumConverter.class)) {

            String name = field.getDeclaredAnnotation(MultEnumConverter.class).formatter();
            if (StringUtils.isBlank(name)) {
                name = field.getDeclaredAnnotation(MultEnumConverter.class).value();
            }
            try {
                String separator = field.getDeclaredAnnotation(MultEnumConverter.class).separator();
                Map<String, Object> bizProperty = BizEnumProperty.getBizProperty(name, locale);
                List<Integer> integers = CommonUtils.numberCompose((Integer) value);
                List<Object> collect = integers.stream().map(x -> bizProperty.get(x + "")).collect(Collectors.toList());
                targetVal = StringUtils.join(collect, separator);
            } catch (Exception e) {
                log.info("[{}.{}] 格式化异常,将忽略该属性！", t.getClass().getName(), field.getName(), e);
            }
        }
        return targetVal;

    }

    /**
     * @param keepOrigin keepOrigin
     * @param value      value
     * @param includeSet includeSet
     * @param excludeSet excludeSet
     * @param locale     locale
     * @param <T>        <T>
     * @return <T>
     */
    private static <T> Object insertRetList(boolean keepOrigin, Object value, Set<String> includeSet, Set<String> excludeSet, String locale) {
        //对象是否是基本类型或基本类型的封装类型
        if (!CommonUtils.isPrimitive(value)) {
            //value为list类型
            if (value instanceof List) {
                List retList = Lists.newArrayList();
                List valueList = (List) value;
                valueList.forEach(x -> {
                    if (CommonUtils.isPrimitive(x)) {
                        retList.add(x);
                    } else {
                        retList.add(includeSet != null ? formatInclude(x, keepOrigin, includeSet, locale) : (excludeSet != null ? formatExclude(x, keepOrigin, excludeSet, locale) : format(x, keepOrigin, locale)));
                    }
                });
                return retList;
            }
            //value 为其他类型
            else {
                return includeSet != null ? formatInclude(value, keepOrigin, includeSet, locale) : (excludeSet != null ? formatExclude(value, keepOrigin, excludeSet, locale) : format(value, keepOrigin, locale));
            }
        }
        return null;
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        Field declaredField = null;
        Class<?> aClass = clazz;
        try {
            declaredField = aClass.getDeclaredField(fieldName);
        } catch (Exception ignored) {
        }
        while (declaredField == null && aClass != null) {
            aClass = aClass.getSuperclass();
            try {
                declaredField = aClass == null ? null : aClass.getDeclaredField(fieldName);
            } catch (Exception ignored) {
            }
        }
        return declaredField;
    }

    private static String getFieldTransferName(Field field) {
        if (field.isAnnotationPresent(CommonConverter.class)) {
            CommonConverter converter = field.getDeclaredAnnotation(CommonConverter.class);
            return StringUtils.isNotBlank(converter.formatFieldName()) ? converter.formatFieldName() : (StringUtils.isNotBlank(converter.fieldName()) ? converter.fieldName() : field.getName());
        } else if (field.isAnnotationPresent(DateConverter.class)) {
            DateConverter converter = field.getDeclaredAnnotation(DateConverter.class);
            return StringUtils.isNotBlank(converter.formatFieldName()) ? converter.formatFieldName() : (StringUtils.isNotBlank(converter.fieldName()) ? converter.fieldName() : field.getName());
        } else if (field.isAnnotationPresent(EnumConverter.class)) {
            EnumConverter converter = field.getDeclaredAnnotation(EnumConverter.class);
            return StringUtils.isNotBlank(converter.formatFieldName()) ? converter.formatFieldName() : (StringUtils.isNotBlank(converter.fieldName()) ? converter.fieldName() : field.getName());
        } else if (field.isAnnotationPresent(MultEnumConverter.class)) {
            MultEnumConverter converter = field.getDeclaredAnnotation(MultEnumConverter.class);
            return StringUtils.isNotBlank(converter.formatFieldName()) ? converter.formatFieldName() : (StringUtils.isNotBlank(converter.fieldName()) ? converter.fieldName() : field.getName());
        } else if (field.isAnnotationPresent(CustomConverter.class)) {
            CustomConverter converter = field.getDeclaredAnnotation(CustomConverter.class);
            return StringUtils.isNotBlank(converter.formatFieldName()) ? converter.formatFieldName() : (StringUtils.isNotBlank(converter.fieldName()) ? converter.fieldName() : field.getName());
        }
        return field.getName();
    }
}
