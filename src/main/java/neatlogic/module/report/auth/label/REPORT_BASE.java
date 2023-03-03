package neatlogic.module.report.auth.label;

import neatlogic.framework.auth.core.AuthBase;

public class REPORT_BASE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.report.reportbase.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.report.reportbase.introduction";
	}

	@Override
	public String getAuthGroup() {
		return "report";
	}

	@Override
	public Integer getSort() {
		return 1;
	}
}
