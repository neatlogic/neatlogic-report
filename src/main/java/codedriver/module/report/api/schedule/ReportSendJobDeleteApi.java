package codedriver.module.report.api.schedule;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ReportSendJobDeleteApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
	private SchedulerMapper schedulerMapper;

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
		if(reportSendJobMapper.checkJobExists(id) == 0){
			throw new ReportSendJobNotFoundException(id);
		}
		reportSendJobMapper.deleteReportReceiver(id);
		reportSendJobMapper.deleteReportRelation(id);
		// TODO 删除定时作业&&发送记录
		schedulerMapper.deleteJobAuditByJobUuid(id.toString());
		reportSendJobMapper.deleteJobById(id);
		return null;
	}
}
