package codedriver.module.report.service;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.dto.ReportVo;

public interface ReportService {
	public Map<String, Object> getQueryResult(Long reportId, JSONObject paramMap, Map<String, Long> timeMap, boolean isFirst) throws Exception;

	public ReportVo getReportDetailById(Long reportId);

	@Transactional
	public int deleteReportById(Long reportId);
}
