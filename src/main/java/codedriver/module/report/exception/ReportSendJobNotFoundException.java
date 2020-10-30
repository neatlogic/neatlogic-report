package codedriver.module.report.exception;

import codedriver.framework.exception.core.ApiRuntimeException;

public class ReportSendJobNotFoundException extends ApiRuntimeException {

	private static final long serialVersionUID = -867701399844094885L;

	public ReportSendJobNotFoundException(Long id) {
		super("报表发送计划：" + id + "不存在");
	}
}
