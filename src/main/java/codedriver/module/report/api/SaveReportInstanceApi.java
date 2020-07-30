package codedriver.module.report.api;

import org.apache.commons.collections.CollectionUtils;
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
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceVo;
import codedriver.module.report.dto.ReportVo;

@Service
@Transactional
public class SaveReportInstanceApi extends ApiComponentBase {

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Override
	public String getToken() {
		return "reportinstance/save";
	}

	@Override
	public String getName() {
		return "保存报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表id"), @Param(name = "name", type = ApiParamType.STRING, desc = "报表名称", isRequired = true, xss = true), @Param(name = "reportId", type = ApiParamType.LONG, desc = "报表定义id", isRequired = true), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true), @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "报表授权列表"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "报表参数配置") })
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "保存报表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportInstanceVo reportInstanceVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);

		if (jsonObj.getLong("id") != null) {
			reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(reportInstanceVo.getId());
			reportInstanceMapper.updateReportInstance(reportInstanceVo);
		} else {
			reportInstanceMapper.insertReportInstance(reportInstanceVo);
		}
		if (CollectionUtils.isNotEmpty(reportInstanceVo.getAuthList())) {
			for (String auth : reportInstanceVo.getAuthList()) {
				ReportInstanceAuthVo reportInstanceAuthVo = new ReportInstanceAuthVo(reportInstanceVo.getId(), auth.split("#")[0], auth.split("#")[1]);
				reportInstanceMapper.insertReportInstanceAuth(reportInstanceAuthVo);
			}
		}
		return reportInstanceVo.getId();
	}
}
