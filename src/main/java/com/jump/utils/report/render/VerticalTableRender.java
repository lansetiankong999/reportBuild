package com.jump.utils.report.render;

import com.deepoove.poi.resolver.DefaultRunTemplateFactory;
import com.jump.utils.report.RenderUtils;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.base.BaseRender;
import com.jump.utils.report.handler.RenderHandler;
import com.jump.utils.report.meta.RenderMeta;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.template.run.RunTemplate;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jump
 * @date 2020/3/9 10:35
 */
public class VerticalTableRender implements BaseRender {
    private final static Logger logger = LoggerFactory.getLogger(TableRender.class);

    @Override
    public void execute(RenderMeta renderMeta) {
        Pattern templatePattern = renderMeta.getTemplatePattern();
        Pattern gramerPattern = renderMeta.getGramerPattern();
        Render render = renderMeta.getRender();
        int[] rowMerges = render.rowMerges();
        String defVal = render.defVal();
        Object data = renderMeta.getData();
        List dataList = Lists.newArrayList();
        if (data instanceof List) {
            dataList = (List) data;
        } else if (data != null) {
            dataList.add(data);
        }
        int dataSize = dataList.size();
        XWPFTemplate xwpfTemplate = renderMeta.getXwpfTemplate();
        XWPFTable xwpfTable = RenderUtils.getxwpftable(xwpfTemplate, renderMeta.getRun());
        XWPFParagraph paragraph = (XWPFParagraph) renderMeta.getRun().getParent();
        XWPFTableCell tableCell = (XWPFTableCell) paragraph.getBody();
        int placeholderIndex = RenderUtils.getTargetColIndex(xwpfTable, tableCell);

        if (CollectionUtils.isEmpty(dataList)) {
            if (StringUtils.isNotBlank(defVal)) {
                RenderUtils.restTableCol(xwpfTable, tableCell, defVal);
                return;
            }
            RenderUtils.removeTableCol(xwpfTable, placeholderIndex);
            return;
        }

        for (int i = 0; i < dataSize - 1; i++) {
            RenderUtils.newAndCopyTableCol(xwpfTable, tableCell);
        }

        //找出待渲染对象中有Render标识的字段
        List<Pair<Field, Render>> allRenderAnno = Lists.newArrayList();
        if (dataSize > 0) {
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

        for (int i = 0; i < dataSize; i++) {
            Object rowData = dataList.get(i);
            List<XWPFTableCell> colTableCells = RenderUtils.getColCells(xwpfTable, placeholderIndex + i);

            for (XWPFTableCell tableCell1 : colTableCells) {
                List<XWPFParagraph> tableCell1Paragraphs = tableCell1.getParagraphs();
                List<XWPFParagraph> paragraphs = new ArrayList<>(tableCell1Paragraphs);
                allRenderAnno.forEach(x -> {
                    Render right = x.getRight();
                    XWPFRun renderEffectRun = RenderUtils.findRenderEffectRun(right, paragraphs, gramerPattern);
                    if (renderEffectRun == null) {
                        return;
                    }
                    List<XWPFParagraph> renderEffectParagraph = RenderUtils.findRenderEffectParagraph(right, paragraphs, gramerPattern);
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
                    anchorRenderMeta.setRun(renderEffectRun);
                    try {
                        Field anchorfield = x.getLeft();
                        anchorfield.setAccessible(true);
                        anchorRenderMeta.setData(anchorfield.get(rowData));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        logger.error("error", e);
                    }
                    if (right.value() != RenderHandler.FILED) {
                        paragraphs.removeAll(renderEffectParagraph);
                    }
                    RenderHandler.handle(anchorRenderMeta);
                });

                //未使用render的段落
                for (XWPFParagraph copyParagraph : paragraphs) {
                    List<XWPFRun> copyRuns = copyParagraph.getRuns();
                    List<XWPFRun> runs = new ArrayList<>(copyRuns);
                    for (XWPFRun x : runs) {
                        RenderUtils.handlePlaceholder(x, rowData, templatePattern, gramerPattern);
                    }
                }

            }

            if (rowMerges.length > 0 && dataList.size() > 1) {
                if (i == 0) {
                    for (int rowIndex : rowMerges) {
                        XWPFTableCell tableCell1 = colTableCells.get(rowIndex);
                        tableCell1.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
                    }
                } else {
                    for (int rowIndex : rowMerges) {
                        XWPFTableCell tableCell1 = colTableCells.get(rowIndex);
                        tableCell1.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
                    }
                }

            }

        }

    }
}
