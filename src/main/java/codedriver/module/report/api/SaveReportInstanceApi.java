package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dto.ReportInstanceTableColumnVo;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceVo;
import codedriver.module.report.dto.ReportVo;

import java.util.ArrayList;
import java.util.List;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Service
@Transactional
public class SaveReportInstanceApi extends PrivateApiComponentBase {

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Override
	public String getToken() {
		return "reportinstance/save";
	}

	@Override
	public String getName() {
		return "保存报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表id"), @Param(name = "name", type = ApiParamType.STRING, desc = "报表名称", isRequired = true, xss = true), @Param(name = "reportId", type = ApiParamType.LONG, desc = "报表定义id", isRequired = true), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true), @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "报表授权列表"),
			@Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "报表参数配置"),
			@Param(name = "tableConfig", type = ApiParamType.JSONARRAY, desc = "表格字段配置")
	})
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "保存报表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}

		ReportInstanceVo reportInstanceVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);

		/** 读取表格配置 */
		JSONArray tableConfig = jsonObj.getJSONArray("tableConfig");
		List<ReportInstanceTableColumnVo> tableColumnList = getTableColumnVoList(reportInstanceVo.getId(), tableConfig);

		if (jsonObj.getLong("id") != null) {
			reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(reportInstanceVo.getId());
			reportInstanceMapper.deleteReportInstanceTableColumn(reportInstanceVo.getId());
			reportInstanceMapper.updateReportInstance(reportInstanceVo);
		} else {
			reportInstanceMapper.insertReportInstance(reportInstanceVo);
		}
		if(CollectionUtils.isNotEmpty(tableColumnList)){
			reportInstanceMapper.batchInsertReportInstanceTableColumn(tableColumnList);
		}
		if (CollectionUtils.isNotEmpty(reportInstanceVo.getAuthList())) {
			for (String auth : reportInstanceVo.getAuthList()) {
				ReportInstanceAuthVo reportInstanceAuthVo = new ReportInstanceAuthVo(reportInstanceVo.getId(), auth.split("#")[0], auth.split("#")[1]);
				reportInstanceMapper.insertReportInstanceAuth(reportInstanceAuthVo);
			}
		}
		return reportInstanceVo.getId();
	}

	private List<ReportInstanceTableColumnVo> getTableColumnVoList(Long reportInstanceId, JSONArray tableConfig) {
		List<ReportInstanceTableColumnVo> tableColumnList = null;
		if(CollectionUtils.isNotEmpty(tableConfig)){
			tableColumnList = new ArrayList<>();
			for(Object o : tableConfig){
				JSONObject obj = JSONObject.parseObject(o.toString());
				String id = obj.getString("tableId");
				JSONArray columnList = obj.getJSONArray("columnList");
				if(CollectionUtils.isNotEmpty(columnList)){
					for(int i = 0;i < columnList.size();i++){
						ReportInstanceTableColumnVo vo = new ReportInstanceTableColumnVo();
						vo.setReportInstanceId(reportInstanceId);
						vo.setTableId(id);
						vo.setColumn(columnList.get(i).toString());
						vo.setSort(i);
						tableColumnList.add(vo);
					}
				}
			}
		}
		return tableColumnList;
	}
}
