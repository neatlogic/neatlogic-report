package codedriver.module.report.dao.mapper;

import java.util.List;

import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportTypeVo;
import codedriver.module.report.dto.ReportVo;

public interface ReportMapper {

	public List<ReportParamVo> getReportParamByReportId(Long reportId);

	public int searchReportCount(ReportVo reportVo);

	public List<ReportTypeVo> getAllReportType();

	public List<ReportVo> searchReport(ReportVo reportVo);

	public List<ReportAuthVo> getReportAuthByReportId(Long reportId);

	public ReportVo getReportById(Long reportId);

	public ReportVo getReportAndParamById(Long reportId);

	public int updateReportActive(ReportVo reportVo);

	public int updateReport(ReportVo reportVo);

	public int updateReportVisitCount(Long reportId);

	public int insertReport(ReportVo reportVo);

	public int insertReportParam(ReportParamVo reportParamVo);

	public int insertReportAuth(ReportAuthVo reportAuthVo);

	public int deleteReportAuthByReportId(Long reportId);

	public int deleteReportById(Long reportId);

	public int deleteReportParamByReportId(Long reportId);

}
