package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportHasBeenQuotedByReportInstanceException extends ApiRuntimeException {

	private static final long serialVersionUID = -2372416746502767188L;

	public ReportHasBeenQuotedByReportInstanceException(String reportName) {
		super("报表：" + reportName + "已被报表实例引用");
	}
}
