package codedriver.module.report.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;

@Service
public class GetReportApi extends ApiComponentBase {

	@Autowired
	private ReportService reportService;

	@Autowired
	private TeamMapper teamMapper;

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

		ReportVo reportVo = reportService.getReportDetailById(jsonObj.getLong("id"));

		if (!hasAuth) {
			String userUuid = UserContext.get().getUserUuid(true);
			List<String> userRoleList = UserContext.get().getRoleUuidList();
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
			if (reportVo.getReportAuthList() != null) {
				for (ReportAuthVo auth : reportVo.getReportAuthList()) {
					if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_USER)) {
						if (auth.getAuthUuid().equals(userUuid)) {
							hasAuth = true;
							break;
						}
					} else if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_ROLE)) {
						if (userRoleList.contains(auth.getAuthUuid())) {
							hasAuth = true;
							break;
						}
					} else if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_TEAM)) {
						if (teamUuidList.contains(auth.getAuthUuid())) {
							hasAuth = true;
							break;
						}
					}
				}
			}
		}
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}
		return reportVo;
	}
}
