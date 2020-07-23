package codedriver.module.report.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class REPORT_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "报表管理员权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "可以查看所有报表并对报表进行修改操作";
	}

	@Override
	public String getAuthGroup() {
		return "report";
	}
}
