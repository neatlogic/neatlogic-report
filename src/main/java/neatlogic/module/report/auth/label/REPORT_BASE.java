package neatlogic.module.report.auth.label;

import neatlogic.framework.auth.core.AuthBase;

/** 权限名称与说明使用国际化键，权限标识及校验规则保持不变。 */
public class REPORT_BASE extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.report_base.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.report_base.description";
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
