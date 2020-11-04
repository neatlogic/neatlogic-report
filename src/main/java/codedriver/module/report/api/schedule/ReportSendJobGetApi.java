package codedriver.module.report.api.schedule;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobRelationVo;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobGetApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
	private ReportMapper reportMapper;

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

	@Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "报表发送计划ID")})
	@Output({@Param(name = "job", explode = ReportSendJobVo.class, desc = "发送计划")})
	@Description(desc = "获取报表发送计划")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		if(reportSendJobMapper.checkJobExists(id) == 0){
			throw new ReportSendJobNotFoundException(id);
		}
		ReportSendJobVo job = reportSendJobMapper.getJobById(id);
		List<ReportSendJobRelationVo> reportRelationList = job.getReportList();
		if(CollectionUtils.isNotEmpty(reportRelationList)){
			Map<Long,ReportSendJobRelationVo> reportRelationMap = new HashMap<>();
			List<Long> reportIdList = new ArrayList<>();
			for(ReportSendJobRelationVo vo : reportRelationList){
				reportRelationMap.put(vo.getReportId(),vo);
				reportIdList.add(vo.getReportId());
			}
			for(Long reportId : reportIdList){
				ReportVo report = reportMapper.getReportAndParamById(reportId);
				reportRelationMap.get(reportId).setReport(report);
			}
		}

		JSONObject result = new JSONObject();
		result.put("job",job);
		return result;
	}
}
