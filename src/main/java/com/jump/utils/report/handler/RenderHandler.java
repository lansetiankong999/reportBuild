package com.jump.utils.report.handler;

import com.jump.utils.report.anno.Render;
import com.jump.utils.report.meta.RenderMeta;
import com.jump.utils.report.render.*;

/**
 * @author Jump
 * @date 2020/3/9 10:21
 */
public class RenderHandler {

    /**
     * 表格模式（行渲染）
     */
    public static final int TABLE = 1;
    /**
     * 表格模式(列渲染)
     */
    public static final int VERTICAL_TABLE = 2;
    /**
     * 组模式
     */
    public static final int GROUP = 3;
    /**
     * 图片模式
     */
    public static final int IMAGE = 4;
    /**
     * 段落列表模式
     */
    public static final int NUMBERIC = 5;
    /**
     * 属性模式
     */
    public static final int FILED = 6;
    /**
     * 表单模式(最后被渲染)
     */
    public static final int FORM = 7;

    public static void handle(RenderMeta renderMeta) {
        Render render = renderMeta.getRender();
        int value = render.value();
        switch (value) {
            case TABLE:
                new TableRender().execute(renderMeta);
                break;
            case GROUP:
                new GroupRender().execute(renderMeta);
                break;
            case IMAGE:
                new ImageRender().execute(renderMeta);
                break;
            case NUMBERIC:
                new NumberRender().execute(renderMeta);
                break;
            case FILED:
                new FieldRender().execute(renderMeta);
                break;
            case VERTICAL_TABLE:
                new VerticalTableRender().execute(renderMeta);
                break;
            default:
                return;
        }


    }
}
