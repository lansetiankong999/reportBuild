package com.jump.utils.property;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jump
 * @date 2020/3/9 14:10
 */
public abstract class AbstractBizEnumProperty implements BeanFactoryPostProcessor {

    private final Pattern specialPattern = Pattern.compile("([^{}=]*)\\.\\{([\\S\\s]*)}=([\\S\\s]*)", Pattern.DOTALL);

    /**
     * 路径
     */
    private Resource[] locations = null;

    /**
     * 获取属性
     *
     * @return Map
     */
    abstract Map<String, Map> getPropertiesMap();

    /**
     * 获取属性
     *
     * @return Map
     */
    abstract Map<String, List<Map<String, String>>> getPropertiesList();


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        this.loadProps();
    }

    private void loadProps() {
        Map<String, Map> propertyMap = getPropertiesMap();
        Map<String, List<Map<String, String>>> propertiesList = getPropertiesList();
        propertyMap.clear();
        propertiesList.clear();

        for (Resource resource : locations) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                String s;
                while ((s = reader.readLine()) != null) {
                    if (StringUtils.isNotBlank(s)) {

                        s = StringEscapeUtils.unescapeJava(StringUtils.trim(s));
                        if (s.startsWith("#")) {
                            continue;
                        }
                        String mapKey;
                        String enumKey;
                        String value;
                        Matcher matcher = specialPattern.matcher(s);
                        if (matcher.find()) {
                            mapKey = matcher.group(1);
                            enumKey = matcher.group(2);
                            value = matcher.group(3);
                        } else {
                            String[] split = s.split("=");
                            String key = split[0];
                            value = split.length > 1 ? split[1] : "";
                            int lastIndex = key.lastIndexOf(".");
                            mapKey = lastIndex > -1 ? key.substring(0, lastIndex) : key;
                            enumKey = key.substring(lastIndex + 1);
                        }

                        Map properties = propertyMap.computeIfAbsent(mapKey, k -> Maps.newLinkedHashMap());
                        properties.put(enumKey, value);

                        List<Map<String, String>> list = propertiesList.computeIfAbsent(mapKey, k -> Lists.newArrayList());

                        Map itemMap = Maps.newHashMap();
                        itemMap.put("key", enumKey);
                        itemMap.put("value", value);
                        list.add(itemMap);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }

    /**
     * 根据key获取对应的properties
     *
     * @param key key
     * @return Map
     */
    Map getBizPropertys(String key) {
        Map<String, Map> propertyMap = getPropertiesMap();
        Map properties = propertyMap.get(key);
        if (properties == null) {
            return Maps.newLinkedHashMap();
        }
        return properties;
    }

    public Resource[] getLocations() {
        return locations;
    }

    public void setLocations(Resource[] locations) {
        this.locations = locations;
    }
}
