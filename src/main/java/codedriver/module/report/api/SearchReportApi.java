package codedriver.module.report.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;

@Service
public class SearchReportApi extends ApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

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

	@Input({ @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true) })
	@Output({ @Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "报表列表", explode = ReportVo[].class) })
	@Description(desc = "查询报表，不受报表授权限制")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
		List<ReportVo> reportList = reportMapper.searchReport(reportVo);
		JSONObject returnObj = new JSONObject();
		returnObj.put("tbodyList", reportList);
		if (reportList.size() > 0) {
			int rowNum = reportMapper.searchReportCount(reportVo);
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageSize", reportVo.getPageSize());
			returnObj.put("currentPage", reportVo.getCurrentPage());
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, reportVo.getPageSize()));
		}

		return returnObj;
	}
}
