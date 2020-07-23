package codedriver.module.report.api;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.exception.ReportNotFoundException;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ReportFreemarkerUtil;
import codedriver.module.report.util.ExportUtil;

@Service
public class ExportReportDetailApi extends BinaryStreamApiComponentBase {
	private static final Log logger = LogFactory.getLog(ExportReportDetailApi.class);

	@Autowired
	private ReportMapper reportMapper;

	@Autowired
	private ReportService reportService;

	@Override
	public String getToken() {
		return "report/export/{id}/{type}";
	}

	@Override
	public String getName() {
		return "展示报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", desc = "报表id", type = ApiParamType.LONG, isRequired = true), @Param(name = "type", desc = "文件类型", type = ApiParamType.ENUM, rule = "pdf,word", isRequired = true) })
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Long reportId = paramObj.getLong("id");
		String type = paramObj.getString("type");
		// 统计使用次数
		reportMapper.updateReportVisitCount(reportId);

		OutputStream os = null;
		try {
			ReportVo reportVo = reportService.getReportDetailById(reportId);
			if (reportVo == null) {
				throw new ReportNotFoundException(reportId);
			}
			Map<String, Long> timeMap = new HashMap<>();
			Map<String, Object> returnMap = reportService.getQueryResult(reportId, paramObj, timeMap, false);
			Map<String, Object> tmpMap = new HashMap<>();
			Map<String, Object> commonMap = new HashMap<>();
			tmpMap.put("report", returnMap);
			tmpMap.put("param", paramObj);
			tmpMap.put("common", commonMap);

			String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, reportVo.getContent());
			if ("pdf".equals(type)) {
				os = response.getOutputStream();
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".pdf\"");
				ExportUtil.getPdfFileByHtml(content, true, os);
			} else if ("word".equals(type)) {
				os = response.getOutputStream();
				response.setContentType("application/x-download");
				response.setHeader("Content-Disposition", "attachment;filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".docx\"");
				ExportUtil.getWordFileByHtml(content, true, os);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
		return null;
	}

}
