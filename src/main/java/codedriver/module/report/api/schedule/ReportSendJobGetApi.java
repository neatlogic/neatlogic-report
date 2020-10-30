package codedriver.module.report.api.schedule;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobGetApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Override
	public String getToken() {
		return "report/sendjob/get";
	}

	@Override
	public String getName() {
		return "获取报表发送计划";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表发送计划ID")})
	@Output({@Param(name = "job", explode = ReportSendJobVo.class, desc = "发送计划")})
	@Description(desc = "获取报表发送计划")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		if(reportSendJobMapper.checkJobExists(id) == 0){
			throw new ReportSendJobNotFoundException(id);
		}
		ReportSendJobVo job = reportSendJobMapper.getJobById(id);

		JSONObject result = new JSONObject();
		result.put("job",job);

		return result;
	}
}
