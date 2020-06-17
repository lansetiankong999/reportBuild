package com.jump.utils.report.meta;

import com.jump.utils.report.anno.Render;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.template.run.RunTemplate;
import lombok.Data;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.regex.Pattern;

/**
 * @author Jump
 * @date 2020/3/9 10:22
 */
@Data
public class RenderMeta<T> {

    private Render render;

    private T data;

    private Configure config;

    private RunTemplate runTemplate;

    private XWPFTemplate xwpfTemplate;

    Pattern templatePattern;

    Pattern gramerPattern;

    private XWPFRun run;
}
