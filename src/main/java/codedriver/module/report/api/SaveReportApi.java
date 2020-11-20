package codedriver.module.report.api;

import codedriver.module.report.auth.label.REPORT_MODIFY;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportVo;

@Service
@Transactional
public class SaveReportApi extends PrivateApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Override
	public String getToken() {
		return "report/save";
	}

	@Override
	public String getName() {
		return "保存报表定义";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表定义id"), 
		@Param(name = "name", type = ApiParamType.STRING, desc = "报表定义名称"), 
		@Param(name = "type", type = ApiParamType.STRING, desc = "报表定义类型"),
		@Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活"), 
		@Param(name = "sql", type = ApiParamType.STRING, desc = "报表定义数据源配置"), 
		@Param(name = "condition", type = ApiParamType.STRING, desc = "报表定义条件配置"), 
		@Param(name = "content", type = ApiParamType.STRING, desc = "报表定义内容配置"), 
		@Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "报表定义授权列表") })
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "保存报表定义")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);

		if (jsonObj.getLong("id") != null) {
			reportMapper.deleteReportAuthByReportId(reportVo.getId());
			reportMapper.deleteReportParamByReportId(reportVo.getId());
			reportMapper.updateReport(reportVo);
		} else {
			reportMapper.insertReport(reportVo);
		}
		if (CollectionUtils.isNotEmpty(reportVo.getParamList())) {
			int i = 0;
			for (ReportParamVo paramVo : reportVo.getParamList()) {
				paramVo.setReportId(reportVo.getId());
				paramVo.setSort(i);
				reportMapper.insertReportParam(paramVo);
				i += 1;
			}
		}
		if (CollectionUtils.isNotEmpty(reportVo.getAuthList())) {
			for (String auth : reportVo.getAuthList()) {
				ReportAuthVo reportAuthVo = new ReportAuthVo(reportVo.getId(), auth.split("#")[0], auth.split("#")[1]);
				reportMapper.insertReportAuth(reportAuthVo);
			}
		}
		return reportVo.getId();
	}
}
