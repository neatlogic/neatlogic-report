package codedriver.module.report.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;

public class ReportInstanceVo extends BasePageVo {
	@JSONField(serialize = false)
	private transient String keyword;
	@EntityField(name = "id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "报表定义id", type = ApiParamType.LONG)
	private Long reportId;
	@EntityField(name = "报表定义名称", type = ApiParamType.STRING)
	private String reportName;
	@EntityField(name = "名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "是否激活", type = ApiParamType.INTEGER)
	private Integer isActive;
	@EntityField(name = "访问次数", type = ApiParamType.INTEGER)
	private Integer visitCount;
	@EntityField(name = "创建用户", type = ApiParamType.STRING)
	private String fcu;
	@EntityField(name = "最后修改人", type = ApiParamType.STRING)
	private String lcu;
	@EntityField(name = "创建日期", type = ApiParamType.LONG)
	private Date fcd;
	@EntityField(name = "修改日期", type = ApiParamType.LONG)
	private Date lcd;
	@EntityField(name = "配置", type = ApiParamType.JSONOBJECT)
	private JSONObject config;
	@EntityField(name = "授权列表", type = ApiParamType.JSONARRAY)
	private List<ReportInstanceAuthVo> reportInstanceAuthList;
	@EntityField(name = "授权字符串列表", type = ApiParamType.JSONARRAY)
	private List<String> authList;
	@JSONField(serialize = false) // 搜索模式，默认是按用户搜索，管理员页面无需检查用户权限
	private transient String searchMode = "user";
	@EntityField(name = "报表参数列表", type = ApiParamType.JSONARRAY)
	private List<ReportParamVo> paramList;
	@EntityField(name = "表格列表", type = ApiParamType.JSONARRAY)
	private Map<String,List<String>> tableColumnsMap;

	public Long getId() {
		if (id == null) {
			id = SnowflakeUtil.uniqueLong();
		}
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public Integer getVisitCount() {
		return visitCount;
	}

	public void setVisitCount(Integer visitCount) {
		this.visitCount = visitCount;
	}

	public String getFcu() {
		return fcu;
	}

	public void setFcu(String fcu) {
		this.fcu = fcu;
	}

	public String getLcu() {
		return lcu;
	}

	public void setLcu(String lcu) {
		this.lcu = lcu;
	}

	public Date getFcd() {
		return fcd;
	}

	public void setFcd(Date fcd) {
		this.fcd = fcd;
	}

	public Date getLcd() {
		return lcd;
	}

	public void setLcd(Date lcd) {
		this.lcd = lcd;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<ReportInstanceAuthVo> getReportInstanceAuthList() {
		return reportInstanceAuthList;
	}

	public void setReportInstanceAuthList(List<ReportInstanceAuthVo> reportInstanceAuthList) {
		this.reportInstanceAuthList = reportInstanceAuthList;
	}

	public List<String> getAuthList() {
		if (CollectionUtils.isEmpty(authList) && CollectionUtils.isNotEmpty(reportInstanceAuthList)) {
			this.authList = new ArrayList<>();
			for (ReportInstanceAuthVo reportAuthVo : reportInstanceAuthList) {
				this.authList.add(reportAuthVo.getAuthType() + "#" + reportAuthVo.getAuthUuid());
			}
		}
		return authList;
	}

	public void setAuthList(List<String> authList) {
		this.authList = authList;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public JSONObject getConfig() {
		return config;
	}

	public void setConfig(String config) {
		try {
			this.config = JSONObject.parseObject(config);
		} catch (Exception ex) {

		}
	}

	@JSONField(serialize = false)
	public String getConfigStr() {
		if (config != null) {
			return config.toJSONString();
		}
		return null;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getSearchMode() {
		return searchMode;
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public List<ReportParamVo> getParamList() {
		if (CollectionUtils.isNotEmpty(paramList) && config != null) {
			JSONObject paramObj = config.getJSONObject("param");
			if (paramObj != null) {
				for (ReportParamVo reportParamVo : paramList) {
					if (paramObj.containsKey(reportParamVo.getName())) {
						JSONObject tmpObj = reportParamVo.getConfig();
						if (tmpObj == null) {
							tmpObj = new JSONObject();
						}
						tmpObj.put("defaultValue", paramObj.getJSONObject(reportParamVo.getName()).get("defaultValue"));
						reportParamVo.setConfig(tmpObj.toJSONString());
					}
				}
			}
		}
		return paramList;
	}

	public void setParamList(List<ReportParamVo> paramList) {
		this.paramList = paramList;
	}

	public Map<String, List<String>> getTableColumnsMap() {
		return tableColumnsMap;
	}

	public void setTableColumnsMap(Map<String, List<String>> tableColumnsMap) {
		this.tableColumnsMap = tableColumnsMap;
	}
}
