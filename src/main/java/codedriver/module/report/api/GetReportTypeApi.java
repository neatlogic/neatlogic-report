package codedriver.module.report.api;

import java.util.List;

import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportTypeVo;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportTypeApi extends PrivateApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Override
	public String getToken() {
		return "report/type/get";
	}

	@Override
	public String getName() {
		return "获取报表分类";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "获取报表分类")
	@Output(@Param(explode = ReportTypeVo.class))
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}
		JSONArray returnList = new JSONArray();
		List<ReportTypeVo> reportTypeList = reportMapper.getAllReportType();
		for (ReportTypeVo reportTypeVo : reportTypeList) {
			JSONObject obj = new JSONObject();
			if (reportTypeVo.getReportCount() > 0) {
				obj.put("name", reportTypeVo.getName() + "(" + reportTypeVo.getReportCount() + ")");
			}
			obj.put("id", reportTypeVo.getName());
			returnList.add(obj);
		}
		return returnList;
	}
}
