package neatlogic.module.report.auth.label;

import neatlogic.framework.auth.core.AuthBase;

import java.util.Collections;
import java.util.List;

public class REPORT_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.report.reportmodify.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.report.reportmodify.introduction";
	}

	@Override
	public String getAuthGroup() {
		return "report";
	}

	@Override
	public Integer getSort() {
		return 2;
	}

	@Override
	public List<Class<? extends AuthBase>> getIncludeAuths(){
		return Collections.singletonList(REPORT_BASE.class);
	}
}
