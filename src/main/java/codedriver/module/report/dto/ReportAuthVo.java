package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ReportAuthVo {
	@EntityField(name = "报表id", type = ApiParamType.LONG)
	private Long reportId;
	@EntityField(name = "授权对象类型", type = ApiParamType.STRING)
	private String authType;
	@EntityField(name = "授权对象uuid", type = ApiParamType.STRING)
	private String authUuid;

	public ReportAuthVo() {

	}

	public ReportAuthVo(Long _reportId, String _authType, String _authUuid) {
		this.reportId = _reportId;
		this.authType = _authType;
		this.authUuid = _authUuid;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthUuid() {
		return authUuid;
	}

	public void setAuthUuid(String authUuid) {
		this.authUuid = authUuid;
	}

}
