package codedriver.module.report.service;

import codedriver.module.report.dto.ReportInstanceVo;

public interface ReportInstanceService {
	public ReportInstanceVo getReportInstanceDetailById(Long reportInstanceId);
}
