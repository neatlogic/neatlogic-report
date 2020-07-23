package codedriver.module.report.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportVo;

@Service
public class SearchReportApi extends ApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "report/search";
	}

	@Override
	public String getName() {
		return "查询报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true), @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页"), @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"), @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"), })
	@Output({ @Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "报表列表", explode = ReportVo[].class) })
	@Description(desc = "查询报表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
		String userUuid = UserContext.get().getUserUuid(true);
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
		if (!hasAuth) {
			// 如果不是管理员，则校验报表权限
			List<ReportAuthVo> reportAuthList = new ArrayList<>();
			reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_USER, userUuid));
			for (String roleUuid : UserContext.get().getRoleUuidList()) {
				reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_ROLE, roleUuid));
			}
			for (String teamUuid : teamMapper.getTeamUuidListByUserUuid(userUuid)) {
				reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_TEAM, teamUuid));
			}
			reportVo.setReportAuthList(reportAuthList);
		} else {
			reportVo.setSearchMode("admin");
		}

		List<ReportVo> reportList = reportMapper.searchReport(reportVo);
		JSONObject returnObj = new JSONObject();
		returnObj.put("tbodyList", reportList);
		if (reportList.size() > 0 && reportVo.getNeedPage()) {
			int rowNum = reportMapper.searchReportCount(reportVo);
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageSize", reportVo.getPageSize());
			returnObj.put("currentPage", reportVo.getCurrentPage());
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, reportVo.getPageSize()));
		}

		return returnObj;
	}
}
