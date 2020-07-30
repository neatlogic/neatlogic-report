package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ReportInstanceAuthVo {
	public final static String AUTHTYPE_USER = "user";
	public final static String AUTHTYPE_TEAM = "team";
	public final static String AUTHTYPE_ROLE = "role";

	@EntityField(name = "报表id", type = ApiParamType.LONG)
	private Long reportInstanceId;
	@EntityField(name = "授权对象类型", type = ApiParamType.STRING)
	private String authType;
	@EntityField(name = "授权对象uuid", type = ApiParamType.STRING)
	private String authUuid;

	public ReportInstanceAuthVo() {

	}

	public ReportInstanceAuthVo(Long _reportInstanceId, String _authType, String _authUuid) {
		this.reportInstanceId = _reportInstanceId;
		this.authType = _authType;
		this.authUuid = _authUuid;
	}

	public ReportInstanceAuthVo(String _authType, String _authUuid) {
		this.authType = _authType;
		this.authUuid = _authUuid;
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

	public Long getReportInstanceId() {
		return reportInstanceId;
	}

	public void setReportInstanceId(Long reportInstanceId) {
		this.reportInstanceId = reportInstanceId;
	}

}
