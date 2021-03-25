package codedriver.module.report.service;

import codedriver.module.report.dto.ReportInstanceVo;

public interface ReportInstanceService {
    ReportInstanceVo getReportInstanceDetailById(Long reportInstanceId);
}
