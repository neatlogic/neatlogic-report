package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ReportInstanceTableColumnVo {
	@EntityField(name = "报表实例ID", type = ApiParamType.LONG)
	private Long reportInstanceId;
	@EntityField(name = "表格ID", type = ApiParamType.STRING)
	private String tableId;
	@EntityField(name = "字段名", type = ApiParamType.STRING)
	private String column;
	@EntityField(name = "排序", type = ApiParamType.INTEGER)
	private Integer sort;

	public Long getReportInstanceId() {
		return reportInstanceId;
	}

	public void setReportInstanceId(Long reportInstanceId) {
		this.reportInstanceId = reportInstanceId;
	}

	public String getTableId() {
		return tableId;
	}

	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
}
