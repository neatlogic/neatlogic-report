package codedriver.module.report.util;

import codedriver.module.report.constvalue.ActionType;
import codedriver.module.report.widget.*;
import com.alibaba.fastjson.JSONObject;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReportFreemarkerUtil {
	private static final Log logger = LogFactory.getLog(ReportFreemarkerUtil.class);

	public static boolean evaluateExpression(String expression, Map<String, Object> paramMap) {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("nashorn");
		Iterator<Map.Entry<String, Object>> iter = paramMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			engine.put(entry.getKey(), entry.getValue());
		}
		try {
			return Boolean.parseBoolean(engine.eval(expression).toString());
		} catch (ScriptException e) {
			// logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return false;
	}


	public static void getFreemarkerContent(Map<String, Object> paramMap, JSONObject filter, String content, Writer out) throws Exception {
		if (StringUtils.isNotBlank(content)) {
			Map<String, Map<String, Object>> pageMap = new HashMap<>();
			Map<String, Object> reportMap = (Map<String, Object>) paramMap.get("report");
			if (MapUtils.isNotEmpty(reportMap)) {
				Map<String, Map<String, Object>> page = (Map<String, Map<String, Object>>) reportMap.get("page");
				if (MapUtils.isNotEmpty(page)) {
					pageMap = page;
				}
			}
			Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
			cfg.setNumberFormat("0.##");
			cfg.setClassicCompatible(true);
			StringTemplateLoader stringLoader = new StringTemplateLoader();
			stringLoader.putTemplate("template", content);
			cfg.setTemplateLoader(stringLoader);
			Template temp;
			paramMap.put("drawTable", new DrawTable(filter, pageMap));
			paramMap.put("drawBar", new DrawBar(ActionType.VIEW.getValue()));
			paramMap.put("drawBarH", new DrawBarH(ActionType.VIEW.getValue()));
			paramMap.put("drawLine", new DrawLine(ActionType.VIEW.getValue()));
			paramMap.put("drawPie", new DrawPie(ActionType.VIEW.getValue()));
			paramMap.put("drawStackedBar", new DrawStackedBar(ActionType.VIEW.getValue()));
			paramMap.put("drawStackedBarH", new DrawStackedBarH(ActionType.VIEW.getValue()));
			paramMap.put("drawStackedBarLineH", new DrawStackedBarLineH(ActionType.VIEW.getValue()));
			paramMap.put("drawPagination", new DrawPagination(true));

			try {
				temp = cfg.getTemplate("template", "utf-8");
				temp.process(paramMap, out);
			} catch (IOException | TemplateException e) {
				logger.error(e.getMessage(), e);
				throw e;
			}
		}
	}

	/*
	 * "string":获取htm "print":下载
	 */
	public static String getFreemarkerExportContent(Map<String, Object> paramMap, String content,String actionType) throws IOException {
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
				paramMap.put("drawTable", new DrawTable());
				paramMap.put("drawBar", new DrawBar(actionType));
				paramMap.put("drawBarH", new DrawBarH(actionType));
				paramMap.put("drawLine", new DrawLine(actionType));
				paramMap.put("drawPie", new DrawPie(actionType));
				paramMap.put("drawStackedBar", new DrawStackedBar(actionType));
				paramMap.put("drawStackedBarH", new DrawStackedBarH(actionType));
				paramMap.put("drawStackedBarLineH", new DrawStackedBarLineH(actionType));
				paramMap.put("drawPagination", new DrawPagination(false));
				try {
					temp = cfg.getTemplate("template", "utf-8");
					temp.process(paramMap, out);
				} catch (IOException | TemplateException e) {
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
