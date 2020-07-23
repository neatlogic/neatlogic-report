package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = 88898742732604752L;

	public ReportNotFoundException(Long reportId) {
		super("报表：" + reportId + "不存在");
	}
}
