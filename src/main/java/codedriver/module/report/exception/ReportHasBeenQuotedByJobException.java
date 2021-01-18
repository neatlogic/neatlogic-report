package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportHasBeenQuotedByJobException extends ApiRuntimeException {

	private static final long serialVersionUID = 3652383679845478468L;

	public ReportHasBeenQuotedByJobException(String reportName) {
		super("报表：" + reportName + "已被报表发送计划引用");
	}
}
