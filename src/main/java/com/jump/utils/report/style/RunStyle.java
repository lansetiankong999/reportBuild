package com.jump.utils.report.style;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.lang.reflect.Field;
import java.util.List;

/**
 * html标签属性因不区分大小写，因些属性命名定义全小写
 *
 * @author Jump
 * @date 2019/10/26
 */
@Data
public class RunStyle {

    /**
     * 内容
     */
    private String runText;

    /**
     * 高亮颜色
     */
    private String highlightcolor;


    /**
     * 字体
     */
    private String fontfamily;


    /**
     * 字体颜色
     * the desired color, in the hex form "RRGGBB"
     */
    private String color;

    /**
     * 字体大小
     */
    private Integer fontsize;


    /**
     * 加粗
     */
    private Boolean bold;

    /**
     * 斜体
     */
    private Boolean italic;

    /**
     * 删除线
     */
    private Boolean strike;

    /**
     * 下划线
     * UnderlinePatterns
     */
    private String underline;

    /**
     * 下划线颜色
     * An RGB color value (e.g, "a0C6F3") or "auto".
     */
    private String underlinecolor;

    /**
     * 列单元格合并，开始
     */
    private Boolean leftcellmergerestart;

    /**
     * 列单元格合并，继续
     */
    private Boolean leftcellmergecontinue;

    /**
     * 行单元格合并，开始
     */
    private Boolean upcellmergerestart;

    /**
     * 行单元格合并，继续
     */
    private Boolean upcellmergecontinue;


    public RunStyle(String runText) {
        this.runText = runText;
    }

    public static List<RunStyle> createRunStyles(String runText) {
        List<RunStyle> runStyleList = Lists.newArrayList();
        Class<RunStyle> runStyleClass = RunStyle.class;
        Document doc = Jsoup.parse(runText);
        List<Node> nodeList = doc.getElementsByTag("body").get(0).childNodes();
        nodeList.forEach(x -> {
            if (x instanceof TextNode) {
                runStyleList.add(new RunStyle(((TextNode) x).text()));
            } else {
                Element element = (Element) x;
                RunStyle runStyle = new RunStyle(element.text());
                Attributes attributes = element.attributes();
                for (Attribute next : attributes) {
                    try {
                        BeanUtils.setProperty(runStyle, next.getKey(), next.getValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                runStyleList.add(runStyle);

            }
        });

        return runStyleList;
    }

    @Override
    public String toString() {
        Document doc = Jsoup.parse("<span></span>");
        Elements span = doc.getElementsByTag("span");
        Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                Object o = field.get(this);
                if (o != null) {
                    String fieldName = field.getName();
                    if ("runText".equals(fieldName)) {
                        span.html(o.toString());
                    } else {
                        span.attr(fieldName, o.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return span.toString();
    }
}
