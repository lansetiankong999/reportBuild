package com.jump.utils.report;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.util.StyleUtils;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jump.common.CommonUtils;
import com.jump.utils.EntityFormatter;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.base.DocumentPicEnum;
import com.jump.utils.report.handler.RenderHandler;
import com.jump.utils.report.style.RunStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.poi.xwpf.usermodel.BodyType.TABLECELL;

/**
 * @author Jump
 * @date 2020/3/9 10:42
 */
@Slf4j
public class RenderUtils {

    public static final int EMU = 9525;

    private static final int INT_RESTART = 2;

    private static final String TEMP_SPAN_END = "</span>";

    private static void copyRun(XWPFRun target, XWPFRun source) {
        StyleUtils.styleRun(target, source);
        target.getCTR().setRPr(source.getCTR().getRPr());
        // 设置文本
        target.setText(source.text());
    }

    public static void handlePlaceholder(XWPFRun run, Object data, Pattern templatePattern, Pattern gramerPattern) {
        if (data == null) {
            data = StringUtils.EMPTY;
        }
        String text = run.getText(0);
        Matcher matcher = templatePattern.matcher(text);
        if (matcher.matches()) {
            if (CommonUtils.isPrimitive(data)) {
                run.setText(data.toString(), 0);
                if (data.toString().contains(TEMP_SPAN_END)) {
                    renderTagRun(run);
                }
                return;
            }
            run.setText(StringUtils.EMPTY, 0);
            String placeholder = gramerPattern.matcher(text).replaceAll(StringUtils.EMPTY).trim();
            Class<?> dataClass = data.getClass();
            try {
                Field placeholderField = dataClass.getDeclaredField(placeholder);
                placeholderField.setAccessible(true);
                Object placeholderValue;
                placeholderValue = placeholderField.get(data);
                if (null != placeholderValue) {
                    placeholderValue = EntityFormatter.formatProperty(data, placeholderField.getName());
                    if (placeholderValue != null) {
                        String runText = placeholderValue.toString();
                        run.setText(runText, 0);
                        if (runText.contains(TEMP_SPAN_END)) {
                            renderTagRun(run);
                        }
                    }

                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //找不到类
                log.warn("render: " + e.getMessage() + "找不到，类名：" + dataClass.getName());
            }
        }

    }

    /**
     * 标签渲染
     *
     * @param run run
     */
    private static void renderTagRun(XWPFRun run) {
        String text = run.text();
        XWPFParagraph parentParagraph = (XWPFParagraph) run.getParent();
        List<XWPFRun> runs = parentParagraph.getRuns();
        int index = runs.indexOf(run);
        List<RunStyle> runStyleList = RunStyle.createRunStyles(text);
        Optional<RunStyle> target = runStyleList.stream().filter(
                x -> (x.getLeftcellmergerestart() != null && x.getLeftcellmergerestart())
                        || (x.getLeftcellmergecontinue() != null && x.getLeftcellmergecontinue())
                        || (x.getUpcellmergerestart() != null && x.getUpcellmergerestart())
                        || (x.getUpcellmergecontinue() != null && x.getUpcellmergecontinue())).findFirst();
        if (target.isPresent()) {
            RunStyle runStyle = target.get();
            XWPFParagraph paragraph = (XWPFParagraph) run.getParent();
            BodyType partType = paragraph.getPartType();
            if (partType == TABLECELL) {
                XWPFTableCell tableCell = (XWPFTableCell) paragraph.getBody();
                if (runStyle.getLeftcellmergerestart() != null && runStyle.getLeftcellmergerestart()) {
                    tableCell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
                }
                if (runStyle.getLeftcellmergecontinue() != null && runStyle.getLeftcellmergecontinue()) {
                    tableCell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
                }
                if (runStyle.getUpcellmergerestart() != null && runStyle.getUpcellmergerestart()) {
                    tableCell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
                }
                if (runStyle.getUpcellmergecontinue() != null && runStyle.getUpcellmergecontinue()) {
                    tableCell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
                }
            }
        }
        //先置空
        run.setText(StringUtils.EMPTY, 0);
        for (int i = runStyleList.size() - 1; i > -1; i--) {
            RunStyle runStyle = runStyleList.get(i);
            XWPFRun xwpfRun = parentParagraph.insertNewRun(index);
            RenderUtils.copyRun(xwpfRun, xwpfRun);
            if (runStyle.getBold() != null) {
                xwpfRun.setBold(runStyle.getBold());
            }
            if (runStyle.getFontfamily() != null) {
                xwpfRun.setFontFamily(runStyle.getFontfamily());
            }
            if (runStyle.getColor() != null) {
                xwpfRun.setColor(runStyle.getColor());
            }
            if (runStyle.getFontsize() != null) {
                xwpfRun.setFontSize(runStyle.getFontsize());
            }
            if (runStyle.getHighlightcolor() != null) {
                xwpfRun.setTextHighlightColor(runStyle.getHighlightcolor());
            }
            if (runStyle.getItalic() != null) {
                xwpfRun.setItalic(runStyle.getItalic());
            }

            if (runStyle.getStrike() != null) {
                xwpfRun.setStrikeThrough(runStyle.getStrike());
            }
            if (runStyle.getUnderline() != null) {
                xwpfRun.setUnderline(UnderlinePatterns.valueOf(runStyle.getUnderline()));
            }
            xwpfRun.setText(runStyle.getRunText(), 0);
        }
        parentParagraph.removeRun(runs.indexOf(run));
    }

    public static void restParagraph(XWPFParagraph paragraph, String defVal) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (CollectionUtils.isEmpty(runs)) {
            return;
        }
        int size = runs.size();
        if (size > 1) {
            for (int i = size - 1; i > 0; i--) {
                paragraph.removeRun(i);
            }
        }
        XWPFRun xwpfRun = runs.get(0);
        xwpfRun.setText(defVal, 0);
        if (StringUtils.isNotBlank(defVal) && defVal.contains(TEMP_SPAN_END)) {
            RenderUtils.renderTagRun(xwpfRun);
        }
    }


    public static void copyParagraph(XWPFParagraph target, XWPFParagraph source) {
        // 设置段落样式
        target.getCTP().setPPr(source.getCTP().getPPr());
        // 添加Run标签
        for (int pos = 0; pos < target.getRuns().size(); pos++) {
            target.removeRun(pos);
        }
        for (XWPFRun s : source.getRuns()) {
            XWPFRun targetrun = target.createRun();
            copyRun(targetrun, s);
        }
    }

    public static XWPFRun findRenderEffectRun(Render render, List<XWPFParagraph> paragraphList, Pattern gramerPattern) {
        String[] anchor = render.anchor();
        HashSet<String> anchorSet = Sets.newHashSet(anchor);
        int value = render.value();
        if (value == RenderHandler.FILED || value == RenderHandler.NUMBERIC) {
            return getXwpfRun(paragraphList, gramerPattern, anchorSet);
        } else if (value == RenderHandler.GROUP) {
            return getXwpfRun(paragraphList, gramerPattern, anchorSet);
        }
        return null;
    }

    private static XWPFRun getXwpfRun(List<XWPFParagraph> paragraphList, Pattern gramerPattern, HashSet<String> anchorSet) {
        for (XWPFParagraph paragraph : paragraphList) {
            List<XWPFRun> runs = paragraph.getRuns();
            for (XWPFRun run : runs) {
                String placeholder = gramerPattern.matcher(run.getText(0)).replaceAll(StringUtils.EMPTY).trim();
                if (anchorSet.contains(placeholder)) {
                    return run;
                }
            }
        }
        return null;
    }

    /**
     * 找出当前render作用域所有段落
     *
     * @return List
     */
    public static List<XWPFParagraph> findRenderEffectParagraph(Render render, List<XWPFParagraph> paragraphList, Pattern gramerPattern) {
        List<XWPFParagraph> retList = Lists.newArrayList();
        int value = render.value();
        String[] anchor = render.anchor();
        String[] lastAnchor = render.lastAnchor();
        HashSet<String> anchorSet = Sets.newHashSet(anchor);
        HashSet<String> lastaAnchorSet = Sets.newHashSet(lastAnchor);
        if (value == RenderHandler.FILED || value == RenderHandler.NUMBERIC) {
            paragraphLoop:
            for (XWPFParagraph paragraph : paragraphList) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String placeholder = gramerPattern.matcher(run.getText(0)).replaceAll(StringUtils.EMPTY).trim();
                    if (anchorSet.contains(placeholder)) {
                        retList.add(paragraph);
                        break paragraphLoop;
                    }
                }
            }
        } else if (value == RenderHandler.GROUP) {
            int startIndex = -1;
            int endIndex = -1;
            paragraphLoop:
            for (int i = 0; i < paragraphList.size(); i++) {
                XWPFParagraph paragraph = paragraphList.get(i);
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    String placeholder = gramerPattern.matcher(run.getText(0)).replaceAll(StringUtils.EMPTY).trim();
                    if (anchorSet.contains(placeholder)) {
                        startIndex = i;
                    }
                    if (lastaAnchorSet.contains(placeholder)) {
                        endIndex = i;
                        break paragraphLoop;
                    }
                }
            }
            if (startIndex > 0 && endIndex > startIndex) {
                return paragraphList.subList(startIndex, endIndex + 1);
            }
        }
        return retList;
    }

    public static int suggestFileType(String imgFile) {
        String[] imgFileSplit = imgFile.split("\\.");
        DocumentPicEnum documentPicEnum = DocumentPicEnum.find(imgFileSplit[imgFileSplit.length - 1]);
        if (ObjectUtils.isEmpty(documentPicEnum)) {
            log.error("Unsupported picture: " + imgFile + ". Expected emf|wmf|pict|jpg|jpeg|png|dib|gif|tiff|eps|bmp|wpg");
            return 0;
        }
        return documentPicEnum.getPictureType();
    }

    public static XWPFTable getxwpfTable(XWPFTemplate xwpfTemplate, XWPFRun run) {
        NiceXWPFDocument doc = xwpfTemplate.getXWPFDocument();
        // w:tbl-w:tr-w:tc-w:p-w:tr
        XmlCursor newCursor = ((XWPFParagraph) run.getParent()).getCTP().newCursor();
        newCursor.toParent();
        newCursor.toParent();
        newCursor.toParent();
        XmlObject object = newCursor.getObject();
        return doc.getTable((CTTbl) object);
    }

    public static XWPFTableRow getxwpftablerow(XWPFTemplate xwpfTemplate, XWPFRun run) {
        NiceXWPFDocument doc = xwpfTemplate.getXWPFDocument();
        // w:tbl-w:tr-w:tc-w:p-w:tr
        XmlCursor newCursor = ((XWPFParagraph) run.getParent()).getCTP().newCursor();
        newCursor.toParent();
        newCursor.toParent();
        XmlObject rowObejct = newCursor.getObject();
        newCursor.toParent();
        XmlObject tableObject = newCursor.getObject();
        XWPFTable table = doc.getTable((CTTbl) tableObject);
        return table.getRow((CTRow) rowObejct);
    }

    public static void restTableRow(XWPFTableRow tableRow, String defVal) {
        List<XWPFTableCell> tableCells = tableRow.getTableCells();
        tableCells.forEach(x -> restTableCell(x, defVal));
    }

    public static void restTableCol(XWPFTable table, XWPFTableCell tableCell, String defVal) {
        int index = getTargetColIndex(table, tableCell);
        table.getRows().forEach(x -> {
            XWPFTableCell tableCell1 = x.getTableCells().get(index);
            restTableCell(tableCell1, defVal);
        });
    }

    public static void removeTableCol(XWPFTable table, Integer index) {
        table.getRows().forEach(x -> x.removeCell(index));
    }

    private static void restTableCell(XWPFTableCell tableCell, String defVal) {
        int paragraphSize = tableCell.getParagraphs().size();
        for (int i = paragraphSize - 1; i > 0; i--) {
            tableCell.removeParagraph(i);
        }
        XWPFParagraph paragraph = tableCell.getParagraphs().get(0);
        restParagraph(paragraph, defVal);
    }

    public static int getTargetColIndex(XWPFTable table, XWPFTableCell source) {
        List<XWPFTableRow> rows = table.getRows();
        Optional<XWPFTableRow> first = rows.stream().filter(x -> x.getTableCells().contains(source)).findFirst();
        AtomicInteger index = new AtomicInteger();
        first.ifPresent(row -> index.set(row.getTableCells().indexOf(source)));
        return index.get();
    }


    public static void newAndCopyTableRow(XWPFTable table, XWPFTableRow sourceRow, int rowIndex) {
        //在表格指定位置新增一行
        XWPFTableRow targetRow = table.insertNewTableRow(rowIndex);
        List<XWPFTableCell> cellList = sourceRow.getTableCells();
        if (null == cellList) {
            return;
        }
        for (XWPFTableCell ignored : cellList) {
            targetRow.addNewTableCell();
        }
        copyTableRow(targetRow, sourceRow);

    }

    /**
     * 复制表格行
     *
     * @param target target
     * @param source source
     */
    private static void copyTableRow(XWPFTableRow target, XWPFTableRow source) {
        // 复制样式
        target.getCtRow().setTrPr(source.getCtRow().getTrPr());
        // 复制单元格
        for (int i = 0; i < target.getTableCells().size(); i++) {
            copyTableCell(target.getCell(i), source.getCell(i));
        }
    }

    private static void copyTableCell(XWPFTableCell target, XWPFTableCell source) {
        CTTcPr sourceTcPr = source.getCTTc().getTcPr();
        CTTcPr targetTcPr = target.getCTTc().getTcPr();
        if (sourceTcPr != null && targetTcPr != null) {
            CTVMerge merge = targetTcPr.getVMerge();
            CTVMerge sourceTcPrMerge = sourceTcPr.getVMerge();
            if (sourceTcPrMerge != null && merge != null) {
                STMerge.Enum targetMergeVal = merge.getVal();
                //目标单元格为行合并单元格的起始单元格时不做处理
                if (targetMergeVal.intValue() == INT_RESTART) {
                    return;
                }
            }
        }


        // 列属性
        target.getCTTc().setTcPr(source.getCTTc().getTcPr());
        // 删除目标 targetCell 所有单元格
        for (int pos = 0; pos < target.getParagraphs().size(); pos++) {
            target.removeParagraph(pos);
        }
        // 添加段落
        for (XWPFParagraph sp : source.getParagraphs()) {
            XWPFParagraph targetP = target.addParagraph();
            copyParagraph(targetP, sp);
        }
    }

    /**
     * 复制表格列
     *
     * @param table  table
     * @param source source
     */
    public static void newAndCopyTableCol(XWPFTable table, XWPFTableCell source) {
        List<XWPFTableRow> rows = table.getRows();
        int index = getTargetColIndex(table, source);
        rows.forEach(x -> {
            List<XWPFTableCell> tableCells = x.getTableCells();
            XWPFTableCell tableCell = x.addNewTableCell();
            copyTableCell(tableCell, tableCells.get(index));
        });
    }

    public static List<XWPFTableCell> getColCells(XWPFTable table, int index) {
        List<XWPFTableCell> tableCellList = Lists.newArrayList();
        List<XWPFTableRow> rows = table.getRows();
        rows.forEach(x -> tableCellList.add(x.getTableCells().get(index)));
        return tableCellList;
    }

}
