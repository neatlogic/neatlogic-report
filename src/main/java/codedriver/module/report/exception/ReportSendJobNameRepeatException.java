package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiFieldValidRuntimeException;

public class ReportSendJobNameRepeatException extends ApiFieldValidRuntimeException {

	private static final long serialVersionUID = 3237715905744130211L;

	public ReportSendJobNameRepeatException(String name) {
		super("报表发送计划：'" + name + "'已存在");
	}
}
