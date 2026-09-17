package neatlogic.module.report.auth.label;

import neatlogic.framework.auth.core.AuthBase;
import java.util.Collections;
import java.util.List;

/** 权限名称与说明使用国际化键，权限标识及校验规则保持不变。 */
public class REPORT_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.report_modify.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.report_modify.description";
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
