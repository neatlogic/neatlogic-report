package codedriver.module.report.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;

@Service
public class GetReportApi extends ApiComponentBase {

	@Autowired
	private ReportService reportService;

	@Override
	public String getToken() {
		return "report/get";
	}

	@Override
	public String getName() {
		return "获取报表详细信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表id") })
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "获取报表详细信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportVo reportVo = reportService.getReportDetailById(jsonObj.getLong("id"));
		return reportVo;
	}
}
