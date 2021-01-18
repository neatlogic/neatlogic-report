package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportInstanceNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = 4761165006438771515L;

	public ReportInstanceNotFoundException(Long reportinstanceId) {
		super("报表实例：" + reportinstanceId + "不存在");
	}
}
