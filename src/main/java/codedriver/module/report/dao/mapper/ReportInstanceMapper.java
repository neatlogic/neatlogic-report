package codedriver.module.report.dao.mapper;

import java.util.List;

import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceTableColumnVo;
import codedriver.module.report.dto.ReportInstanceVo;

public interface ReportInstanceMapper {
	public ReportInstanceVo getReportInstanceById(Long reportInstanceId);

	public List<ReportInstanceAuthVo> getReportInstanceAuthByReportInstanceId(Long reportInstanceId);

	public List<ReportInstanceVo> searchReportInstance(ReportInstanceVo reportInstanceVo);

	public int searchReportInstanceCount(ReportInstanceVo reportInstanceVo);

	public List<ReportInstanceTableColumnVo> getReportInstanceTableColumnList(Long reportInstanceId);

	public int insertReportInstance(ReportInstanceVo reportInstanceVo);

	public int insertReportInstanceAuth(ReportInstanceAuthVo reportInstanceAuthVo);

	public int batchInsertReportInstanceTableColumn(List<ReportInstanceTableColumnVo> list);

	public int updateReportInstance(ReportInstanceVo reportInstanceVo);

	public int updateReportInstanceActive(ReportInstanceVo reportInstanceVo);

	public int deleteReportInstanceAuthByReportInstanceId(Long reportInstanceId);

	public int deleteReportInstanceTableColumn(Long reportInstanceId);

}
