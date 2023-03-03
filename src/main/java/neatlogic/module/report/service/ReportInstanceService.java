package neatlogic.module.report.service;

import neatlogic.module.report.dto.ReportInstanceVo;

public interface ReportInstanceService {
    ReportInstanceVo getReportInstanceDetailById(Long reportInstanceId);
}
