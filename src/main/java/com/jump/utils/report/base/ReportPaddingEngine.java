package com.jump.utils.report.base;

import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.render.processor.Visitor;
import com.deepoove.poi.template.MetaTemplate;
import com.jump.common.CommonReflex;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.handler.RenderHandler;
import com.jump.utils.report.meta.RenderMeta;
import com.jump.utils.report.policy.ReportPolicy;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.template.ElementTemplate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Jump
 */
public class ReportPaddingEngine {

    private static final Logger logger = LoggerFactory.getLogger(ReportPaddingEngine.class);

    public static void run(BasePaddingPlaceholder paddingPlaceholder, File template, File outFile) throws IOException {
        ConfigureBuilder builder = Configure.newBuilder();
        Map<String, Object> paddingMap = Maps.newHashMap();
        builder.buildGramer("${", "}");
        List<Field> fields = CommonReflex.getParentMember(paddingPlaceholder.getClass());
        Map<String, Integer> renderValMap = Maps.newHashMap();
        fields.forEach(field -> getFieldConsumer(paddingPlaceholder, builder, paddingMap, renderValMap, field));
        Configure configure = builder.build();
        XWPFTemplate compile = XWPFTemplate.compile(template, configure);
        /*List<MetaTemplate> elementTemplates = compile.getElementTemplates();
        List<MetaTemplate> collect = elementTemplates.stream().filter(x -> {
            String variable = x.variable();
            String tagName = variable.replace("${", "").replace("}", "");
            return paddingMap.containsKey(tagName);
        }).collect(Collectors.toList());
        elementTemplates.clear();
        collect.sort(Comparator.comparingInt(o -> renderValMap.get(o.variable())));
        elementTemplates.addAll(collect);*/
        compile.render(paddingMap);

        FileOutputStream out = new FileOutputStream(outFile);
        compile.write(out);
        out.flush();
        out.close();
        compile.close();
    }

    private static void getFieldConsumer(BasePaddingPlaceholder paddingPlaceholder, ConfigureBuilder builder, Map<String, Object> paddingMap, Map<String, Integer> renderValMap, Field field) {
        try {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object data = field.get(paddingPlaceholder);
            boolean renderPresent = field.isAnnotationPresent(Render.class);
            if (renderPresent) {
                Render render = field.getDeclaredAnnotation(Render.class);
                if (render.value() != RenderHandler.FORM) {
                    RenderMeta<Object> renderMeta = new RenderMeta<>();
                    renderMeta.setRender(render);
                    renderMeta.setData(data);
                    String[] anchorArr = render.anchor();
                    Set<String> anchorSet = Sets.newHashSet(anchorArr);
                    for (String anchor : anchorSet) {
                        builder.customPolicy(anchor, new ReportPolicy(renderMeta));
                        paddingMap.put(anchor, data);
                        renderValMap.put(anchor, render.value());
                    }
                } else if (data != null) {
                    Field[] declaredFields = data.getClass().getDeclaredFields();
                    List<Field> formFieldList = Lists.newArrayList(declaredFields);
                    formFieldList.stream().filter(y -> !Modifier.isStatic(field.getModifiers())).forEach(y -> {
                        try {
                            y.setAccessible(true);
                            String formFieldName = y.getName();
                            Object fieldData = y.get(data);
                            boolean formRenderPresent = y.isAnnotationPresent(Render.class);
                            if (formRenderPresent) {
                                Render fieldRender = y.getDeclaredAnnotation(Render.class);
                                RenderMeta<Object> renderMeta = new RenderMeta<>();
                                renderMeta.setRender(fieldRender);
                                renderMeta.setData(fieldData);
                                String[] anchorArr = fieldRender.anchor();
                                Set<String> anchorSet = Sets.newHashSet(anchorArr);
                                for (String anchor : anchorSet) {
                                    builder.customPolicy(anchor, new ReportPolicy(renderMeta));
                                    renderValMap.put(anchor, fieldRender.value());
                                    paddingMap.put(anchor, fieldData);
                                }
                            } else {
                                RenderMeta<Object> renderMeta = new RenderMeta<>();
                                renderMeta.setData(fieldData);
                                builder.customPolicy(formFieldName, new ReportPolicy(renderMeta));
                                paddingMap.put(formFieldName, fieldData);
                                renderValMap.put(formFieldName, Integer.MAX_VALUE);
                            }
                        } catch (Exception e) {
                            logger.error("ReportPaddingEngine form field[{}] init error!", y.getName(), e);
                        }
                    });
                }
            } else {
                RenderMeta<Object> renderMeta = new RenderMeta<>();
                renderMeta.setData(data);
                builder.customPolicy(fieldName, new ReportPolicy(renderMeta));
                renderValMap.put(fieldName, Integer.MAX_VALUE);
                paddingMap.put(fieldName, data);
            }
        } catch (Exception e) {
            logger.error("ReportPaddingEngine field[{}] init error!", field.getName(), e);
        }
    }
}
