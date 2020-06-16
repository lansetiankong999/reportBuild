package com.jump.utils.report.render;

import com.deepoove.poi.resolver.DefaultRunTemplateFactory;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.jump.common.CommonUtils;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.poi.xwpf.usermodel.BodyType.TABLECELL;

/**
 * @author Jump
 * @date 2020/3/9 10:35
 */
public class NumberRender implements BaseRender {
    private final static Logger logger = LoggerFactory.getLogger(TableRender.class);

    @Override
    public void execute(RenderMeta renderMeta) {
        XWPFTemplate xwpfTemplate = renderMeta.getXwpfTemplate();
        XWPFRun placeholderRun = renderMeta.getRun();
        Pattern templatePattern = renderMeta.getTemplatePattern();
        Pattern gramerPattern = renderMeta.getGramerPattern();
        Render render = renderMeta.getRender();
        String defVal = render.defVal();
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


        XWPFParagraph targetParagraph = (XWPFParagraph) placeholderRun.getParent();
        NiceXWPFDocument document = (NiceXWPFDocument) targetParagraph.getDocument();
        BodyType partType = targetParagraph.getPartType();
        IBody body = targetParagraph.getBody();
        List<XWPFParagraph> paragraphs = body.getParagraphs();
        CTP ctp = targetParagraph.getCTP();

        //如果没有数据，进行移除
        if (CollectionUtils.isEmpty(dataList)) {
            if (partType == TABLECELL) {
                XWPFTableCell tableCell = (XWPFTableCell) body;
                List<XWPFParagraph> cellParagraphs = tableCell.getParagraphs();
                if (cellParagraphs.size() == 1) {
                    //单元格只有该一个段落时，给该段落设置一个空字符串，避免表格发生位移，造成排版混乱
                    if (StringUtils.isBlank(defVal)) {
                        RenderUtils.restParagraph(targetParagraph, " ");
                    } else {
                        RenderUtils.restParagraph(targetParagraph, defVal);
                    }
                    return;
                }
                tableCell.removeParagraph(cellParagraphs.indexOf(targetParagraph));

            } else {
                if (StringUtils.isNotBlank(defVal)) {
                    RenderUtils.restParagraph(targetParagraph, defVal);
                    return;
                }
                int posOfParagraph = document.getPosOfParagraph(targetParagraph);
                document.removeBodyElement(posOfParagraph);
            }
            return;
        }

        //找出待渲染对象中有Render标识的字段
        List<Pair<Field, Render>> allRenderAnno = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(dataList)) {
            Object o = dataList.get(0);
            if (!CommonUtils.isPrimitive(o)) {
                Field[] declaredFields = o.getClass().getDeclaredFields();
                for (Field field : declaredFields) {
                    if (field.isAnnotationPresent(Render.class)) {
                        Render declaredAnnotation = field.getDeclaredAnnotation(Render.class);
                        allRenderAnno.add(ImmutablePair.of(field, declaredAnnotation));
                    }
                }
            }

        }
        //排序
        allRenderAnno.sort(Comparator.comparingInt(o -> o.getRight().value()));


        for (Object rowData : dataList) {
            List<XWPFParagraph> copyParagraphList = Lists.newArrayList();
            XWPFParagraph paragraph = body.insertNewParagraph(ctp.newCursor());
            RenderUtils.copyParagraph(paragraph, targetParagraph);
            copyParagraphList.add(paragraph);

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
            tableCell.removeParagraph(paragraphs.indexOf(targetParagraph));

        } else {
            int posOfParagraph = document.getPosOfParagraph(targetParagraph);
            document.removeBodyElement(posOfParagraph);
        }

    }
}
