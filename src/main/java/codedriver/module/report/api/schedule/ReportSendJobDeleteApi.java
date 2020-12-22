package codedriver.module.report.api.schedule;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ReportSendJobDeleteApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
	private SchedulerMapper schedulerMapper;

	@Autowired
	private SchedulerManager schedulerManager;

	@Override
	public String getToken() {
		return "report/sendjob/delete";
	}

	@Override
	public String getName() {
		return "删除报表发送计划";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表发送计划ID")})
	@Output({})
	@Description(desc = "删除报表发送计划")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		ReportSendJobVo job = reportSendJobMapper.getJobBaseInfoById(id);
		if(job == null){
			throw new ReportSendJobNotFoundException(id);
		}
		/** 清除定时任务 */
		IJob handler = SchedulerManager.getHandler("codedriver.module.report.schedule.plugin.ReportSendJob");
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("sendJobId",job.getId()).build();
		schedulerManager.unloadJob(newJobObject);
		reportSendJobMapper.deleteReportReceiver(id);
		reportSendJobMapper.deleteReportRelation(id);
		schedulerMapper.deleteJobAuditByJobUuid(id.toString());
		reportSendJobMapper.deleteJobById(id);
		return null;
	}
}
