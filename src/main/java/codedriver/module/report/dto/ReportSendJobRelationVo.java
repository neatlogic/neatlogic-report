package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;
import com.alibaba.fastjson.annotation.JSONField;

public class ReportSendJobRelationVo {
	@EntityField(name = "报表发送计划ID", type = ApiParamType.LONG)
	private Long reportSendJobId;
	@EntityField(name = "报表ID", type = ApiParamType.LONG)
	private Long reportId;
	@EntityField(name = "条件", type = ApiParamType.STRING)
	@JSONField(serialize = false)
	private String condition;
	@EntityField(name = "配置", type = ApiParamType.STRING)
	@JSONField(serialize = false)
	private String config;
	@EntityField(name = "排序", type = ApiParamType.INTEGER)
	@JSONField(serialize = false)
	private Integer sort;

	public Long getReportSendJobId() {
		return reportSendJobId;
	}

	public void setReportSendJobId(Long reportSendJobId) {
		this.reportSendJobId = reportSendJobId;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
}
