package com.jump.utils.report.policy;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.exception.RenderException;
import com.deepoove.poi.policy.AbstractRenderPolicy;
import com.deepoove.poi.render.RenderContext;
import com.deepoove.poi.resolver.TemplateResolver;
import com.deepoove.poi.template.ElementTemplate;
import com.jump.utils.report.RenderUtils;
import com.jump.utils.report.anno.Render;
import com.jump.utils.report.handler.RenderHandler;
import com.jump.utils.report.meta.RenderMeta;
import org.apache.poi.xwpf.usermodel.XWPFRun;


/**
 * @author Jump
 * @date 2020/3/9 10:30
 */
public class ReportPolicy<T> extends AbstractRenderPolicy<T> {

    private final RenderMeta<T> renderMeta;

    public ReportPolicy(RenderMeta<T> renderMeta) {
        this.renderMeta = renderMeta;
    }

    @Override
    protected boolean validate(Object data) {
        return true;
    }


    @Override
    public void render(ElementTemplate eleTemplate, Object data, XWPFTemplate template) {

        // type safe
        T model;
        try {
            model = (T) data;
        } catch (ClassCastException e) {
            throw new RenderException("Error Render Data format for template: " + eleTemplate.getSource(), e);
        }

        // validate
        RenderContext<T> context = new RenderContext<T>(eleTemplate, model, template);
        if (!validate(model)) {
            postValidError(context);
            return;
        }

        // do render
        try {
            beforeRender(context);
            doRender(context);
            afterRender(context);
        } catch (Exception e) {
            reThrowException(context, e);
        }
    }

    @Override
    public void doRender(RenderContext renderContext) {
        renderMeta.setRun(renderContext.getRun());
        renderMeta.setXwpfTemplate(renderContext.getTemplate());
        renderMeta.setConfig(renderContext.getConfig());
        TemplateResolver resolver = (TemplateResolver) renderContext.getTemplate().getResolver();
        renderMeta.setTemplatePattern(resolver.getTemplatePattern());
        renderMeta.setGramerPattern(resolver.getGramerPattern());
        Render render = renderMeta.getRender();
        XWPFRun run = renderContext.getRun();
        try {
            run.getText(0);
        } catch (Exception e) {
            //run.getText(0);抛异常表示该占位符对应的run已被其它render处理了，无需再进行渲染
            logger.info(String.format("%s该占位符对应的run已被其它render处理了，无需再进行渲染", run.getText(0)));
            return;
        }
        if (render == null) {
            RenderUtils.handlePlaceholder(run, renderContext.getData(), renderMeta.getTemplatePattern(), renderMeta.getGramerPattern());
            return;
        }
        RenderHandler.handle(renderMeta);
    }

}
