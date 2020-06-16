package com.jump.utils.report.render;

import com.deepoove.poi.resolver.DefaultRunTemplateFactory;
import com.deepoove.poi.resolver.TemplateResolver;
import com.jump.pojo.placeholder.BaseMeta;
import com.jump.utils.report.RenderUtils;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.base.BaseRender;
import com.jump.utils.report.handler.RenderHandler;
import com.jump.utils.report.meta.RenderMeta;
import com.jump.utils.report.style.RunStyle;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.template.run.RunTemplate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.poi.xwpf.usermodel.BodyType.TABLECELL;

/**
 * @author Jump
 * @date 2020/3/9 10:34
 */
public class GroupRender implements BaseRender {

    private final static Logger logger = LoggerFactory.getLogger(GroupRender.class);

    @Override
    public void execute(RenderMeta renderMeta) {
        Pattern templatePattern = renderMeta.getTemplatePattern();
        Pattern gramerPattern = renderMeta.getGramerPattern();
        Render render = renderMeta.getRender();
        String defVal = render.defVal();
        String[] lastAnchor = render.lastAnchor();
        HashSet<String> lastaAnchorSet = Sets.newHashSet(lastAnchor);
        Object data = renderMeta.getData();
        List dataList = Lists.newArrayList();

        if (data instanceof List) {
            dataList = (List) data;
        } else if (data != null) {
            dataList.add(data);
        }
        if (CollectionUtils.isNotEmpty(dataList) && dataList.size() == 1) {
            Object o = dataList.get(0);
            if (o instanceof BaseMeta) {
                BaseMeta meta = (BaseMeta) o;
                RunStyle emptyElement = meta.getEmptyElement();
                if (emptyElement != null) {
                    String runText = emptyElement.getRunText();
                    dataList.clear();
                    if (StringUtils.isBlank(runText) && StringUtils.isNotBlank(defVal)) {
                        emptyElement.setRunText(defVal);
                    }
                    defVal = emptyElement.toString();
                }
            }
        }

        XWPFTemplate xwpfTemplate = renderMeta.getXwpfTemplate();
        XWPFRun placeholderRun = renderMeta.getRun();
        XWPFParagraph startParagraph = (XWPFParagraph) placeholderRun.getParent();
        XWPFDocument document = startParagraph.getDocument();
        BodyType partType = startParagraph.getPartType();
        IBody body = startParagraph.getBody();
        List<XWPFParagraph> paragraphs = body.getParagraphs();
        int startIndex = paragraphs.indexOf(startParagraph);
        XWPFParagraph lastParagraph = null;
        int lastIndex = startIndex;
        for (int i = startIndex; i < paragraphs.size(); i++) {
            XWPFParagraph tmpParagraph = paragraphs.get(i);
            List<XWPFRun> runs = tmpParagraph.getRuns();
            boolean anyMatch = runs.stream().anyMatch(x -> {
                String text = x.getText(0);
                Matcher matcher = templatePattern.matcher(text);

                if (matcher.matches()) {
                    String placeholder = gramerPattern.matcher(text).replaceAll("").trim();
                    return lastaAnchorSet.contains(placeholder);
                }
                return false;
            });

            if (anyMatch) {
                lastParagraph = tmpParagraph;
                lastIndex = paragraphs.indexOf(lastParagraph);
                break;
            }
        }

        XWPFParagraph insertPointParagraph = paragraphs.get(lastIndex + 1);
        CTP ctp = insertPointParagraph.getCTP();

        //找出待渲染对象中有Render标识的字段
        List<Pair<Field, Render>> allRenderAnno = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(dataList)) {
            Field[] declaredFields = dataList.get(0).getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Render.class)) {
                    Render declaredAnnotation = field.getDeclaredAnnotation(Render.class);
                    allRenderAnno.add(ImmutablePair.of(field, declaredAnnotation));
                }
            }
        }
        //排序
        allRenderAnno.sort(Comparator.comparingInt(o -> o.getRight().value()));

        if (CollectionUtils.isEmpty(dataList)) {
            if (partType == TABLECELL) {
                XWPFTableCell tableCell = (XWPFTableCell) body;
                if (StringUtils.isNotBlank(defVal)) {
                    for (int j = lastIndex; j > startIndex; j--) {
                        tableCell.removeParagraph(paragraphs.indexOf(paragraphs.get(j)));
                    }
                    XWPFParagraph paragraph = paragraphs.get(startIndex);
                    RenderUtils.restParagraph(paragraph, defVal);

                } else {
                    for (int j = lastIndex; j > startIndex - 1; j--) {
                        tableCell.removeParagraph(paragraphs.indexOf(paragraphs.get(j)));
                    }
                }

            } else {
                if (StringUtils.isNotBlank(defVal)) {
                    for (int j = lastIndex; j > startIndex; j--) {
                        int posOfParagraph = document.getPosOfParagraph(paragraphs.get(j));
                        document.removeBodyElement(posOfParagraph);
                    }
                    XWPFParagraph paragraph = paragraphs.get(startIndex);
                    RenderUtils.restParagraph(paragraph, defVal);

                } else {
                    for (int j = lastIndex; j > startIndex - 1; j--) {
                        int posOfParagraph = document.getPosOfParagraph(paragraphs.get(j));
                        document.removeBodyElement(posOfParagraph);
                    }
                }


            }
            return;

        }
        for (int i = 0; i < dataList.size(); i++) {
            Object rowData = dataList.get(i);

            //先复制完所需要段落
            List<XWPFParagraph> copyParagraphList = Lists.newArrayList();
            for (int j = 0; j < lastIndex - startIndex + 1; j++) {
                XmlCursor xmlCursor = ctp.newCursor();
                XWPFParagraph tmpParagraph = body.insertNewParagraph(xmlCursor);
                RenderUtils.copyParagraph(tmpParagraph, paragraphs.get(startIndex + j));
                copyParagraphList.add(tmpParagraph);
            }

            allRenderAnno.forEach(x -> {
                Render right = x.getRight();
                XWPFRun renderEffectRun = RenderUtils.findRenderEffectRun(right, copyParagraphList, gramerPattern);
                if (renderEffectRun == null) {
                    return;
                }
                List<XWPFParagraph> renderEffectParagraph = RenderUtils.findRenderEffectParagraph(right, copyParagraphList, gramerPattern);
                RenderMeta anchorRenderMeta = new RenderMeta();
                anchorRenderMeta.setTemplatePattern(templatePattern);
                anchorRenderMeta.setGramerPattern(gramerPattern);
                anchorRenderMeta.setRender(x.getRight());
                anchorRenderMeta.setXwpfTemplate(xwpfTemplate);
                anchorRenderMeta.setConfig(renderMeta.getConfig());
                //String tag = gramerPattern.matcher(renderEffectRun.getText(0)).replaceAll("").trim();
                //DefaultRunTemplateFactory defaultRunTemplateFactory = new DefaultRunTemplateFactory(renderMeta.getConfig());
                //RunTemplate anchorRunTemplate = defaultRunTemplateFactory.createRunTemplate(tag, renderEffectRun);
                //anchorRenderMeta.setRunTemplate(anchorRunTemplate);
                anchorRenderMeta.setRun(anchorRenderMeta.getRun());
                try {
                    Field anchorfield = x.getLeft();
                    anchorfield.setAccessible(true);
                    anchorRenderMeta.setData(anchorfield.get(rowData));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.error("error", e);
                }
                if (right.value() != RenderHandler.FILED) {
                    copyParagraphList.removeAll(renderEffectParagraph);
                }
                RenderHandler.handle(anchorRenderMeta);

            });

            //未使用render的段落
            for (XWPFParagraph copyParagraph : copyParagraphList) {
                List<XWPFRun> copyRuns = copyParagraph.getRuns();
                List<XWPFRun> runs = new ArrayList<>(copyRuns);
                for (XWPFRun x : runs) {
                    RenderUtils.handlePlaceholder(x, rowData, templatePattern, gramerPattern);
                }
            }
        }

        //删除多余的段落
        if (partType == TABLECELL) {
            XWPFTableCell tableCell = (XWPFTableCell) body;
            for (int j = lastIndex; j > startIndex - 1; j--) {
                tableCell.removeParagraph(paragraphs.indexOf(paragraphs.get(j)));
            }
        } else {
            for (int j = lastIndex; j > startIndex - 1; j--) {
                int posOfParagraph = document.getPosOfParagraph(paragraphs.get(j));
                document.removeBodyElement(posOfParagraph);
            }
        }
    }
}
