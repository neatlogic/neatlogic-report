package codedriver.module.report.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dto.SelectVo;
import codedriver.module.report.util.ReportXmlUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;

@Service
public class GetReportApi extends PrivateApiComponentBase {

	/** 匹配表格Id */
	private static Pattern pattern = Pattern.compile("drawTable.*\\)");
	/** 匹配表格中文名 */
	private static Pattern namePattern = Pattern.compile("title.*\"");

	@Autowired
	private ReportService reportService;

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getToken() {
		return "report/get";
	}

	@Override
	public String getName() {
		return "获取报表定义详细信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, desc = "报表定义id", isRequired = true) })
	@Output({ @Param(explode = ReportVo.class) })
	@Description(desc = "获取报表定义详细信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// 权限判断：如果是管理员
		boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());

		ReportVo reportVo = reportService.getReportDetailById(jsonObj.getLong("id"));
		/** 查找表格 */
		getTableList(reportVo);

		if (!hasAuth) {
			String userUuid = UserContext.get().getUserUuid(true);
			List<String> userRoleList = UserContext.get().getRoleUuidList();
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
			if (reportVo.getReportAuthList() != null) {
				for (ReportAuthVo auth : reportVo.getReportAuthList()) {
					if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_USER)) {
						if (auth.getAuthUuid().equals(userUuid)) {
							hasAuth = true;
							break;
						}
					} else if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_ROLE)) {
						if (userRoleList.contains(auth.getAuthUuid())) {
							hasAuth = true;
							break;
						}
					} else if (auth.getAuthType().equals(ReportAuthVo.AUTHTYPE_TEAM)) {
						if (teamUuidList.contains(auth.getAuthUuid())) {
							hasAuth = true;
							break;
						}
					}
				}
			}
		}
		if (!hasAuth) {
			throw new PermissionDeniedException();
		}
		return reportVo;
	}

	private void getTableList(ReportVo reportVo) throws DocumentException {
		String content = reportVo.getContent();
		String sql = reportVo.getSql();
		if(StringUtils.isNotBlank(content) && StringUtils.isNotBlank(sql)){
			Map<String,String> tables = new HashMap<>();
			Matcher matcher = pattern.matcher(content);
			/** 寻找是表格的图表，生成[包含id的字符串->title]的map */
			while (matcher.find()){
				String e = matcher.group();
				Matcher m = namePattern.matcher(e);
				/** 寻找表格title */
				if(m.find()){
					String name = m.group();
					if(name.contains(",")){
						name = name.substring(name.indexOf("\"") + 1,name.indexOf(",") - 1);
					}else{
						name = name.substring(name.indexOf("\"") + 1,name.lastIndexOf("\""));
					}
					tables.put(e,name);
				}else{
					tables.put(e,null);
				}
			}
			/** tableColumnsMap中的key为表格ID与中文名组合而成的字符串,value为表格字段
			 * e.g:"tableData-工单上报列表"
			 */
			List<Map<String,Object>> tableColumnsMapList = null;
			if(MapUtils.isNotEmpty(tables)){
				tableColumnsMapList = new ArrayList<>();
				/** 从SQL中获取所有图表
				 * 从中寻找表格，记录下其id、title与column
				 */
				Map<String,Object> map = ReportXmlUtil.analyseSql(sql);
				List<SelectVo> selectList = (List<SelectVo>)map.get("select");
				for(Map.Entry<String,String> entry : tables.entrySet()){
					for(SelectVo vo : selectList){
						if(entry.getKey().contains(vo.getId())){
							Map<String,Object> tableColumnsMap = new HashMap<>();
							tableColumnsMap.put("id",vo.getId());
							tableColumnsMap.put("title",entry.getValue());
							tableColumnsMap.put("columnList",vo.getResultMap().getPropertyList());
							tableColumnsMapList.add(tableColumnsMap);
							break;
						}
					}
				}
			}
			reportVo.setTableList(tableColumnsMapList);
		}
	}
}
