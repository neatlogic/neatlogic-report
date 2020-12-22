package codedriver.module.report.api;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.exception.ReportNotFoundException;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ShowReportDetailApi extends PrivateBinaryStreamApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Autowired
	private ReportService reportService;

	@Override
	public String getToken() {
		return "report/show/{id}";
	}

	@Override
	public String getName() {
		return "展示报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", desc = "报表id", isRequired = true),
			@Param(name = "reportInstanceId", desc = "报表实例id"),
	})
	@Description(desc = "展示报表接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Long reportId = paramObj.getLong("id");
		Long reportInstanceId = paramObj.getLong("reportInstanceId");
		// 统计使用次数
		reportMapper.updateReportVisitCount(reportId);

		Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

		// Map<String, Object> paramMap = ReportToolkit.getParamMap(request);
		PrintWriter out = response.getWriter();
		try {
			ReportVo reportVo = reportService.getReportDetailById(reportId);
			if (reportVo == null) {
				throw new ReportNotFoundException(reportId);
			}
			//out.write("<!DOCTYPE HTML>");
			//out.write("<html lang=\"en\">");
			//out.write("<head>");
			//out.write("<title>" + reportVo.getName() + "</title>");
			//out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
			//out.write("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
			//out.write("</head>");
			//out.write("<body>");
			Map<String, Long> timeMap = new HashMap<>();
			boolean isFirst = false;
			if (request.getHeader("referer") == null || request.getHeader("referer").indexOf("report-show/" + reportId) == -1) {
				isFirst = true;
			}
			Map<String, Object> returnMap = reportService.getQueryResult(reportId, paramObj, timeMap, isFirst,showColumnsMap);
			Map<String, Object> tmpMap = new HashMap<>();
			Map<String, Object> commonMap = new HashMap<>();
			tmpMap.put("report", returnMap);
			tmpMap.put("param", paramObj);
			tmpMap.put("common", commonMap);

			ReportFreemarkerUtil.getFreemarkerContent(tmpMap, reportVo.getContent(), out);
		} catch (Exception ex) {
			out.write("<div class=\"ivu-alert ivu-alert-error ivu-alert-with-icon ivu-alert-with-desc\">" + "<span class=\"ivu-alert-icon\"><i class=\"ivu-icon ivu-icon-ios-close-circle-outline\"></i></span>" + "<span class=\"ivu-alert-message\">异常：</span> <span class=\"ivu-alert-desc\"><span>" + ex.getMessage() + "</span></span></div>");
		}
		//out.write("</body></html>");
		out.flush();
		out.close();
		return null;
	}

}
