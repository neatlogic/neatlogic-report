package codedriver.module.report.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codedriver.module.report.dto.SelectVo;
import codedriver.module.report.util.ReportXmlUtil;
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
		boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");

		ReportVo reportVo = reportService.getReportDetailById(jsonObj.getLong("id"));
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
		/** 匹配表格 */
		String content = reportVo.getContent();
		Map<String,String> tables = new HashMap<>();
//            List<String> tables = new ArrayList<>();
		List<String> tableNames = new ArrayList<>();
		/** 匹配表格Id */
		Pattern pattern = Pattern.compile("drawTable.*\\)");
		/** 匹配表格中文名 */
		Pattern namePattern = Pattern.compile("title.*\"");
		Matcher matcher = pattern.matcher(content);
		/** 寻找是表格的图表id */
		while (matcher.find()){
			String e = matcher.group();
//                tables.add(e);
			Matcher m = namePattern.matcher(e);
			if(m.find()){
				String name = m.group();
				name = name.substring(name.indexOf("\""),name.lastIndexOf("\""));
				tableNames.add(name);
				tables.put(e,name);
			}else{
				tables.put(e,null);
			}
		}
		/** tableColumnsMapList中的key为表格ID与中文名的map,value为表格字段list */
		List<Map<Map<String,String>,List<String>>> tableColumnsMapList = new ArrayList<>();
		Map<String, Object> map = ReportXmlUtil.analyseSql(reportVo.getSql());
		List<SelectVo> selectList = (List<SelectVo>)map.get("select");
		for(Map.Entry<String,String> entry : tables.entrySet()){
			for(SelectVo vo : selectList){
				if(entry.getKey().contains(vo.getId())){
					Map<String,String> nameMap = new HashMap<>();
					nameMap.put(vo.getId(),entry.getValue());
					Map<Map<String,String>,List<String>> tableMap = new HashMap<>();
					tableMap.put(nameMap,vo.getResultMap().getPropertyList());
//                        Map<String,List<String>> tableMap = new HashMap<>();
//                        tableMap.put(vo.getId(),vo.getResultMap().getPropertyList());
					tableColumnsMapList.add(tableMap);
					break;
				}
			}
		}

		reportVo.setTableList(tableColumnsMapList);
	}
}
