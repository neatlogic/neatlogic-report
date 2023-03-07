/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.util;

import neatlogic.framework.util.javascript.JavascriptUtil;
import neatlogic.module.report.constvalue.ActionType;
import neatlogic.module.report.widget.*;
import com.alibaba.fastjson.JSONObject;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class ReportFreemarkerUtil {
    private static final Log logger = LogFactory.getLog(ReportFreemarkerUtil.class);

    public static boolean evaluateExpression(String expression, Map<String, Object> paramMap) {
        ScriptEngine engine = JavascriptUtil.getEngine();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            engine.put(entry.getKey(), entry.getValue());
        }
        try {
            return Boolean.parseBoolean(engine.eval(expression).toString());
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }


    public static void getFreemarkerContent(Map<String, Object> paramMap, Map<String, Object> reportMap, Map<String, Map<String, Object>> pageMap, JSONObject filter, String content, Writer out) throws Exception {
        if (StringUtils.isNotBlank(content)) {
            Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            cfg.setNumberFormat("0.##");
            cfg.setClassicCompatible(true);
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            stringLoader.putTemplate("template", content);
            cfg.setTemplateLoader(stringLoader);
            Template temp;
            paramMap.put("drawTable", new DrawTable(reportMap, pageMap, filter));
            paramMap.put("drawBar", new DrawBar(reportMap, ActionType.VIEW.getValue()));
            paramMap.put("drawBarH", new DrawBarH(reportMap, ActionType.VIEW.getValue()));
            paramMap.put("drawLine", new DrawLine(reportMap, ActionType.VIEW.getValue()));
            paramMap.put("drawPie", new DrawPie(reportMap, ActionType.VIEW.getValue()));
            paramMap.put("drawStackedBar", new DrawStackedBar(reportMap, ActionType.VIEW.getValue()));
            paramMap.put("drawStackedBarH", new DrawStackedBarH(reportMap, ActionType.VIEW.getValue()));
//			paramMap.put("drawStackedBarLineH", new DrawStackedBarLineH(reportMap, ActionType.VIEW.getValue()));
//			paramMap.put("drawPagination", new DrawPagination(reportMap, true));

            try {
                temp = cfg.getTemplate("template", "utf-8");
                temp.process(paramMap, out);
            } catch (IOException | TemplateException e) {
                logger.error("freeMarker Code：" + content);
                logger.error("JSON Code：" + JSONObject.toJSONString(paramMap));
                logger.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    /*
     * "string":获取htm "print":下载
     */
    public static String getFreemarkerExportContent(Map<String, Object> paramMap, Map<String, Object> reportMap, Map<String, Map<String, Object>> pageMap, JSONObject filter, String content, String actionType) throws IOException {
        StringWriter out = new StringWriter();
        out.write("<html xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">\n");
        out.write("<head>\n");
        out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta>\n");
        out.write("<style type=\"text/css\">\n");
        out.write("html {font-family: \"PingFang SC\", \"Helvetica Neue\", \"思源黑体\", \"Microsoft YaHei\", \"黑体\", Helvetica;line-height: 1.42857143; color: #666666;font-size: 14px;}\n");
        out.write("table{width: 100%; max-width: 100%; margin-bottom: 10px; margin-top: 0;border-collapse:collapse;border-spacing:0;border-top:1px solid #ddd;}\n");
        out.write("th,td{padding: 8px; line-height: 1.42857143;  vertical-align: top; border-top: 1px solid #dddddd;}\n");
        out.write("th{text-align: left;color: #999999;}\n");
        out.write(".table-condensed th,.table-condensed td{padding: 5px;}\n");
        out.write("div.well {  min-height: 20px; padding: 19px; line-height: 1.8; border-radius: 4px;  background: #fffdf2; border: 1px solid #ffd821;box-shadow: 0 0 5px 0 rgba(0,0,0,0.10); border-radius: 5px;}\n");
        out.write(".text-primary { color: #336eff;}\n");
        out.write("</style>\n");
        out.write("</head>\n");
        out.write("<body>\n");
        try {
            if (StringUtils.isNotBlank(content)) {
                Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                cfg.setNumberFormat("0.##");
                cfg.setClassicCompatible(true);
                StringTemplateLoader stringLoader = new StringTemplateLoader();
                stringLoader.putTemplate("template", content);
                cfg.setTemplateLoader(stringLoader);
                Template temp;
                paramMap.put("drawTable", new DrawTable(reportMap, pageMap, filter));
                paramMap.put("drawBar", new DrawBar(reportMap, actionType));
                paramMap.put("drawBarH", new DrawBarH(reportMap, actionType));
                paramMap.put("drawLine", new DrawLine(reportMap, actionType));
                paramMap.put("drawPie", new DrawPie(reportMap, actionType));
                paramMap.put("drawStackedBar", new DrawStackedBar(reportMap, actionType));
                paramMap.put("drawStackedBarH", new DrawStackedBarH(reportMap, actionType));
//				paramMap.put("drawStackedBarLineH", new DrawStackedBarLineH(reportMap, actionType));
//				paramMap.put("drawPagination", new DrawPagination(reportMap, false));
                try {
                    temp = cfg.getTemplate("template", "utf-8");
                    temp.process(paramMap, out);
                } catch (IOException | TemplateException e) {
                    logger.error("freeMarker Code：" + content);
                    logger.error("JSON Code：" + JSONObject.toJSONString(paramMap));
                    logger.error(e.getMessage(), e);
                    throw e;
                }
            }
        } catch (Exception ex) {
            out.write("<div class=\"ivu-alert ivu-alert-error ivu-alert-with-icon ivu-alert-with-desc\">" + "<span class=\"ivu-alert-icon\"><i class=\"ivu-icon ivu-icon-ios-close-circle-outline\"></i></span>" + "<span class=\"ivu-alert-message\">异常：</span> <span class=\"ivu-alert-desc\"><span>" + ex.getMessage() + "</span></span></div>");
        }
        out.write("\n</body>\n</html>");
        out.flush();
        out.close();
        return out.toString();
    }

}