package com.jump.utils.report.base;

import com.jump.utils.report.meta.RenderMeta;

/**
 * @author Jump
 * @date 2020/3/9 10:36
 */
public interface BaseRender {

    /**
     * 执行
     *
     * @param renderMeta meta
     */
    void execute(RenderMeta renderMeta);
}
