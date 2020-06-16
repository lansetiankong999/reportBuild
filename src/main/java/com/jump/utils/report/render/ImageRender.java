package com.jump.utils.report.render;

import com.jump.utils.report.RenderUtils;
import com.jump.utils.report.base.BaseRender;
import com.jump.utils.report.meta.RenderMeta;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.template.run.RunTemplate;
import com.google.common.collect.Lists;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDrawing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jump
 * @date 2020/3/9 10:35
 */
public class ImageRender implements BaseRender {
    @Override
    public void execute(RenderMeta renderMeta) {
        Pattern templatePattern = renderMeta.getTemplatePattern();
        Object data = renderMeta.getData();
        List dataList = Lists.newArrayList();
        if (data instanceof List) {
            dataList = (List) data;
        } else if (data != null) {
            dataList.add(data);
        }
        XWPFRun placeholderRun = renderMeta.getRun();
        XWPFParagraph paragraph = (XWPFParagraph) placeholderRun.getParent();
        IBody iBody = paragraph.getBody();
        XWPFDocument document = paragraph.getDocument();

        XmlCursor xmlCursor = paragraph.getCTP().newCursor();
        for (Object o : dataList) {
            XWPFParagraph tmpParagraph = iBody.insertNewParagraph(xmlCursor);
            RenderUtils.copyParagraph(tmpParagraph, paragraph);
            xmlCursor = paragraph.getCTP().newCursor();
            PictureRenderData picture = (PictureRenderData) o;
            List<XWPFRun> runs = new ArrayList<>(tmpParagraph.getRuns());
            runs.forEach(x -> {
                String text = x.getText(0);
                Matcher matcher = templatePattern.matcher(text);
                if (matcher.matches()) {
                    x.setText("", 0);
                    InputStream ins = null;
                    try {
                        int suggestFileType = RenderUtils.suggestFileType(picture.getPath());
                        File pictureFile = new File(picture.getPath());
                        BufferedImage bi = ImageIO.read(pictureFile);
                        int width = picture.getWidth();
                        int height = picture.getHeight();
                        ins = null == picture.getData() ? new FileInputStream(pictureFile) : new ByteArrayInputStream(picture.getData());
                        if (0 == width) {
                            width = 750;
                        }
                        if (0 == height) {
                            double d = (double) width / bi.getWidth();
                            height = (int) (bi.getHeight() * d);
                        }
                        x.addPicture(ins, suggestFileType, pictureFile.getName(), width * RenderUtils.EMU, height * RenderUtils.EMU);
                        List<CTDrawing> drawingList = x.getCTR().getDrawingList();
                        drawingList.forEach(y -> {
                            List<CTInline> inlineList = y.getInlineList();
                            inlineList.forEach(z -> {
                                CTNonVisualDrawingProps docPr = z.getDocPr();
                                long id = docPr.getId();
                                //为防止渲染的图片id与模板图片的id冲突，导致打开word文件报错，将渲染的图片id增加一个偏移量
                                docPr.setId(id + 10000);
                            });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (ins != null) {
                                ins.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        BodyType partType = paragraph.getPartType();
        if (partType == BodyType.TABLECELL) {
            XWPFTableCell tableCell = (XWPFTableCell) iBody;
            int posOfParagraph = tableCell.getParagraphs().indexOf(paragraph);
            tableCell.removeParagraph(posOfParagraph);

        } else {
            int posOfParagraph = document.getPosOfParagraph(paragraph);
            document.removeBodyElement(posOfParagraph);
        }


    }
}
