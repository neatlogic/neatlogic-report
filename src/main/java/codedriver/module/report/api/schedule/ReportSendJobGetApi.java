package codedriver.module.report.api.schedule;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportSendJobRelationVo;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.exception.ReportSendJobNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
		/** 获取报表条件控件回显值 */
		List<ReportSendJobRelationVo> reportRelationList = job.getReportRelationList();
		List<ReportVo> reportList = null;
		if(CollectionUtils.isNotEmpty(job.getReportList())){
			job.getReportList().sort(Comparator.comparing(ReportVo::getSort));
			reportList = job.getReportList();
		}
		if(CollectionUtils.isNotEmpty(reportRelationList) && CollectionUtils.isNotEmpty(reportList)){
			Map<Long,String> configMap = new HashMap<>();
			for(ReportSendJobRelationVo vo : reportRelationList){
				configMap.put(vo.getReportId(),vo.getConfig());
			}
			for(ReportVo vo : reportList){
				List<ReportParamVo> paramList = reportMapper.getReportParamByReportId(vo.getId());
				String config = configMap.get(vo.getId());
				if(CollectionUtils.isNotEmpty(paramList) && StringUtils.isNotBlank(config)){
					JSONObject paramObj = JSONObject.parseObject(config);
					Iterator<ReportParamVo> it = paramList.iterator();
					while (it.hasNext()) {
						ReportParamVo param = it.next();
						if (paramObj.containsKey(param.getName())) {
							String value = paramObj.getString(param.getName());
							if (param.getConfig() != null && StringUtils.isNotBlank(value)) {
								param.getConfig().put("defaultValue", value);
							}
						}
					}
				}
				vo.setParamList(paramList);
			}
		}

		JSONObject result = new JSONObject();
		result.put("job",job);
		return result;
	}
}
