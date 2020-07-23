package codedriver.module.report.dto;

import org.apache.commons.lang3.StringUtils;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ReportTypeVo {
	@EntityField(name = "分类名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "报表数量", type = ApiParamType.INTEGER)
	private int reportCount;

	public String getName() {
		if (StringUtils.isBlank(name)) {
			name = "未分类";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReportCount() {
		return reportCount;
	}

	public void setReportCount(int reportCount) {
		this.reportCount = reportCount;
	}
}
