package codedriver.module.report.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;

@Service
public class ReportInstanceServiceImpl implements ReportInstanceService {
	Logger logger = LoggerFactory.getLogger(ReportInstanceServiceImpl.class);

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Override
	public ReportInstanceVo getReportInstanceDetailById(Long reportInstanceId) {
		ReportInstanceVo reportInstanceVo = reportInstanceMapper.getReportInstanceById(reportInstanceId);
		reportInstanceVo.setReportInstanceAuthList(reportInstanceMapper.getReportInstanceAuthByReportInstanceId(reportInstanceId));
		reportInstanceVo.setTableColumnList(reportInstanceMapper.getReportInstanceTableColumnList(reportInstanceId));
		return reportInstanceVo;
	}

}
