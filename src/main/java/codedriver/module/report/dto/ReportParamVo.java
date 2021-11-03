package codedriver.module.report.dto;

import codedriver.framework.common.dto.BasePageVo;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;

public class ReportParamVo extends BasePageVo {
	@EntityField(name = "报表id", type = ApiParamType.LONG)
	private Long reportId;
	@EntityField(name = "id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "参数名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "参数标签", type = ApiParamType.STRING)
	private String label;
	@EntityField(name = "控件类型", type = ApiParamType.STRING)
	private String type;
	@EntityField(name = "配置", type = ApiParamType.JSONOBJECT)
	private JSONObject config;
	@EntityField(name = "宽度", type = ApiParamType.INTEGER)
	private Integer width;
	@JSONField(serialize = false)
	private String configStr;
	@JSONField(serialize = false)
	private Integer sort;

	private String reportName;

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public JSONObject getConfig() {
		return config;
	}

	public void setConfig(String config) {
		if (StringUtils.isNotBlank(config)) {
			try {
				this.config = JSONObject.parseObject(config);
			} catch (Exception ex) {

			}
		}
	}

	public String getConfigStr() {
		if (StringUtils.isBlank(configStr) && this.config != null) {
			configStr = this.config.toJSONString();
		}
		return configStr;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Long getId() {
		if (id == null) {
			id = SnowflakeUtil.uniqueLong();
		}
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
}
