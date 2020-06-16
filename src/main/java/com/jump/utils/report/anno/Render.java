package com.jump.utils.report.anno;

import java.lang.annotation.*;

/**
 * @author Jump
 * @date 2020/3/9 10:08
 */
@Inherited
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Render {

    /**
     * 渲染实现
     *
     * @return int
     */
    int value();

    /**
     * 锚点识别符，用于占位符定位（支持多个配置）
     *
     * @return String[]
     */
    String[] anchor() default {};

    /**
     * 锚点结束识别符,配合anchor用位定位占位符区间（支持多个配置）
     *
     * @return String[]
     */
    String[] lastAnchor() default {};

    /**
     * 跨行单元格合并（纵向单元格）的列索引数组（从0开始，TableRender 专用）
     *
     * @return int[]
     */
    int[] colMerges() default {};

    /**
     * 跨列单元格合并（横向单元格）的行索引数组（从0开始，VerticalTableRender 专用）
     *
     * @return int[]
     */
    int[] rowMerges() default {};

    /**
     * 默认值（为空时）
     *
     * @return String
     */
    String defVal() default "";

}
