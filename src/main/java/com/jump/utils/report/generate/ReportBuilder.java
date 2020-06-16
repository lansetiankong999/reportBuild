package com.jump.utils.report.generate;

import com.jump.utils.report.base.BasePaddingPlaceholder;
import com.jump.utils.report.base.ReportPaddingEngine;

import java.io.File;
import java.io.IOException;


/**
 * @author Jump
 */
public class ReportBuilder {

    public static void build(BasePaddingPlaceholder placeholder, String templatePath, String out) throws IOException {
        File template = new File(templatePath);
        File outFile = new File(out);
        ReportPaddingEngine.run(placeholder, template, outFile);
    }
}


