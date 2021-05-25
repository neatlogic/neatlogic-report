package codedriver.module.report.api;

import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.exception.ReportNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;


@Service
@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateReportActiveApi extends PrivateApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Override
	public String getToken() {
		return "report/toggleactive";
	}

	@Override
	public String getName() {
		return "更改报表定义激活状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表id"), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活") })
	@Description(desc = "更改报表定义激活状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        ReportVo report = reportMapper.getReportById(reportVo.getId());
        if(report == null){
            throw new ReportNotFoundException(report.getId());
        }
		reportMapper.updateReportActive(reportVo);
		return null;
	}
}
