package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
@Service
public class DeleteReportApi extends PrivateApiComponentBase {

	@Autowired
	private ReportService reportService;

	@Override
	public String getToken() {
		return "report/delete";
	}

	@Override
	public String getName() {
		return "删除报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表id") })
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "删除报表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long reportId = jsonObj.getLong("id");
		return null;
	}
}
