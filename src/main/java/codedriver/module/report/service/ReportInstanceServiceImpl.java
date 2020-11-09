package codedriver.module.report.service;

import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportInstanceServiceImpl implements ReportInstanceService {
	Logger logger = LoggerFactory.getLogger(ReportInstanceServiceImpl.class);

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Autowired
	private ReportService reportService;

	@Override
	public ReportInstanceVo getReportInstanceDetailById(Long reportInstanceId) {
		ReportInstanceVo reportInstanceVo = reportInstanceMapper.getReportInstanceById(reportInstanceId);
		reportInstanceVo.setReportInstanceAuthList(reportInstanceMapper.getReportInstanceAuthByReportInstanceId(reportInstanceId));
		reportInstanceVo.setTableColumnsMap(reportService.getShowColumnsMap(reportInstanceId));
		return reportInstanceVo;
	}

}
