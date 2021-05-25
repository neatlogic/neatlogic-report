package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportHasInstanceException extends ApiRuntimeException {

	private static final long serialVersionUID = -2372416746502767188L;

	public ReportHasInstanceException(String reportName) {
		super("报表：" + reportName + "已存在实例，不可删除");
	}
}
