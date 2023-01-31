package neatlogic.module.report.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;

import java.io.Serializable;

public class ReportAuthVo implements Serializable {
    private static final long serialVersionUID = 1695049249233643108L;

    public final static String AUTHTYPE_USER = "user";
    public final static String AUTHTYPE_TEAM = "team";
    public final static String AUTHTYPE_ROLE = "role";

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

    public ReportAuthVo(String _authType, String _authUuid) {
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
