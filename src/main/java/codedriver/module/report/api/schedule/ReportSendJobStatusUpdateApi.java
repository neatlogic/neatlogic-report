package codedriver.module.report.api.schedule;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(name = "REPORT_MODIFY")
@OperationType(type = OperationTypeEnum.UPDATE)
public class ReportSendJobStatusUpdateApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Override
	public String getToken() {
		return "report/sendjob/status/update";
	}

	@Override
	public String getName() {
		return "修改报表发送计划激活状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "报表发送计划ID"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
	})
	@Output({})
	@Description(desc = "修改报表发送计划激活状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ReportSendJobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>(){});
		ReportSendJobVo job = reportSendJobMapper.getJobBaseInfoById(jobVo.getId());
		jobVo.setLcu(UserContext.get().getUserUuid());
		if (job == null) {
			throw new ReportSendJobNotFoundException(jobVo.getId());
		} else {
			reportSendJobMapper.updateJobStatus(jobVo);
		}
		// TODO 启动定时任务

		return null;
	}
}
