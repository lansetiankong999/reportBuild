package com.jump.utils.report.test;

import com.jump.utils.report.anno.Render;
import com.jump.utils.report.base.BasePaddingPlaceholder;
import com.jump.utils.report.handler.RenderHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jump
 * @date 2020/2/26 13:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ReportInfo extends BasePaddingPlaceholder {

    /**
     * 样本编号
     */
    @Render(value = RenderHandler.FILED, anchor = "sampleNo")
    private String sampleNo;

    @Render(value = RenderHandler.IMAGE, anchor = "readsUrl")
    private String readsUrl;
}
