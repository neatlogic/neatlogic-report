package codedriver.module.report.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;

@Service
public class UpdateReportActiveApi extends ApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Override
	public String getToken() {
		return "report/updatereportactive";
	}

	@Override
	public String getName() {
		return "更改报表激活状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表id"), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活") })
	@Description(desc = "更改报表激活状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
		reportMapper.updateReportActive(reportVo);
		return null;
	}
}
