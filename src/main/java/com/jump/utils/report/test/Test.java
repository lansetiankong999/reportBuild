package com.jump.utils.report.test;

import com.jump.utils.report.generate.ReportBuilder;

import java.io.File;
import java.io.IOException;

/**
 * @author Jump
 * @date 2020/6/17 9:05
 */
public class Test {

    public static void main(String[] args) {
        try {
            ReportInfo reportInfo = new ReportInfo();
            reportInfo.setSampleNo("19B88889734");
            String templatePath = System.getProperty("user.dir") + File.separator + "src/static/template" + File.separator + "DX-SDP-B92 V1.0 血友希望专项倒位阳性报告模板.docx";
            ReportBuilder.build(reportInfo, templatePath, "D:\\aaa\\test.docx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
