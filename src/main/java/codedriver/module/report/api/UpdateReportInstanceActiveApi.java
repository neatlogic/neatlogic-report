package codedriver.module.report.api;

import codedriver.module.report.auth.label.REPORT_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateReportInstanceActiveApi extends PrivateApiComponentBase {

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Override
	public String getToken() {
		return "reportinstance/toggleactive";
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
		ReportInstanceVo reportVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);
		reportInstanceMapper.updateReportInstanceActive(reportVo);
		return null;
	}
}
