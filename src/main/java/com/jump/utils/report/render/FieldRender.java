package com.jump.utils.report.render;

import com.jump.pojo.placeholder.BaseMeta;
import com.jump.utils.report.RenderUtils;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.base.BaseRender;
import com.jump.utils.report.meta.RenderMeta;
import com.jump.utils.report.style.RunStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;
import java.util.regex.Pattern;

import static org.apache.poi.xwpf.usermodel.BodyType.TABLECELL;

/**
 * @author Jump
 * @date 2020/3/9 10:35
 */
public class FieldRender implements BaseRender {
    @Override
    public void execute(RenderMeta renderMeta) {
        XWPFRun placeholderRun = renderMeta.getRun();
        Pattern templatePattern = renderMeta.getTemplatePattern();
        Pattern gramerPattern = renderMeta.getGramerPattern();
        Render render = renderMeta.getRender();
        String defVal = render.defVal();
        Object data = renderMeta.getData();
        XWPFRun run = renderMeta.getRun();
        XWPFParagraph targetParagraph = (XWPFParagraph) placeholderRun.getParent();
        XWPFDocument document = targetParagraph.getDocument();
        IBody body = targetParagraph.getBody();
        List<XWPFParagraph> paragraphs = body.getParagraphs();
        BodyType partType = targetParagraph.getPartType();

        if (data instanceof BaseMeta) {
            BaseMeta meta = (BaseMeta) data;
            RunStyle emptyElement = meta.getEmptyElement();
            if (emptyElement != null) {
                String runText = emptyElement.getRunText();
                data = null;
                if (StringUtils.isBlank(runText) && StringUtils.isNotBlank(defVal)) {
                    emptyElement.setRunText(defVal);
                }
                defVal = emptyElement.toString();
            }
        }
        if (data == null || StringUtils.isBlank(data + "")) {
            if (StringUtils.isNotBlank(defVal)) {
                RenderUtils.handlePlaceholder(run, defVal, templatePattern, gramerPattern);
                return;
            }
            int size = targetParagraph.getRuns().size();
            if (size == 1) {
                if (partType == TABLECELL) {
                    XWPFTableCell tableCell = (XWPFTableCell) body;
                    tableCell.removeParagraph(paragraphs.indexOf(targetParagraph));
                } else {
                    int posOfParagraph = document.getPosOfParagraph(targetParagraph);
                    document.removeBodyElement(posOfParagraph);
                }
            }
            return;
        }
        RenderUtils.handlePlaceholder(run, data, templatePattern, gramerPattern);
    }
}
