package codedriver.module.report.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class REPORT_BASE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "报表基础权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "查看报表";
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
