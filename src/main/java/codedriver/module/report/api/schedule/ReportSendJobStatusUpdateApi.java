package codedriver.module.report.api.schedule;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ReportSendJobStatusUpdateApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
	private SchedulerManager schedulerManager;

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
		IJob handler = SchedulerManager.getHandler("codedriver.module.report.schedule.plugin.ReportSendJob");
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("sendJobId",job.getId()).build();
		if(jobVo.getIsActive().intValue() == 1){
			schedulerManager.loadJob(newJobObject);
		}else{
			schedulerManager.unloadJob(newJobObject);
		}

		return null;
	}
}
