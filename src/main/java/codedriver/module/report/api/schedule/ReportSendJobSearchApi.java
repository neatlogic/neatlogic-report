package codedriver.module.report.api.schedule;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobSearchApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Override
	public String getToken() {
		return "report/sendjob/search";
	}

	@Override
	public String getName() {
		return "查询报表发送计划";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param( name = "keyword",
					type = ApiParamType.STRING,
					desc = "关键词",
					xss = true),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					desc = "当前页"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					desc = "每页数据条目"),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true")
	})
	@Output({
			@Param(name = "jobList",
					type = ApiParamType.JSONARRAY,
					explode = ReportSendJobVo[].class,
					desc = "发送计划列表"),
			@Param(explode = BaseEditorVo.class)
	})
	@Description(desc = "查询报表发送计划")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ReportSendJobVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>(){});
		JSONObject returnObj = new JSONObject();
		if(vo.getNeedPage()){
			int rowNum = reportSendJobMapper.searchJobCount(vo);
			returnObj.put("pageSize", vo.getPageSize());
			returnObj.put("currentPage", vo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
		}
		List<ReportSendJobVo> jobList = reportSendJobMapper.searchJob(vo);
		returnObj.put("jobList",jobList);
		return returnObj;
	}
}
