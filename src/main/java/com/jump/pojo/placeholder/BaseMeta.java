package com.jump.pojo.placeholder;

import com.jump.utils.report.style.RunStyle;

/**
 * @author Jump
 * @date 2020/3/9 14:42
 */
public class BaseMeta {

    /**
     * 特殊空值对象（用于保存业务对象数据为空，但扔需要样式渲染的使用场景）
     */
    protected RunStyle emptyElement;

    public RunStyle getEmptyElement() {
        return emptyElement;
    }

    public void setEmptyElement(RunStyle emptyElement) {
        this.emptyElement = emptyElement;
    }
}
