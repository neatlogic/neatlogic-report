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

	/**
	 * @Description: 检查id为reportId的报表是否被报表实例引用
	 * @Author: laiwt
	 * @Date: 2021/1/18 11:52
	 * @Params: [reportId]
	 * @Returns: int
	**/
	public int checkReportInstanceExistsByReportId(Long reportId);

	public int insertReportInstance(ReportInstanceVo reportInstanceVo);

	public int insertReportInstanceAuth(ReportInstanceAuthVo reportInstanceAuthVo);

	public int batchInsertReportInstanceTableColumn(List<ReportInstanceTableColumnVo> list);

	public int updateReportInstance(ReportInstanceVo reportInstanceVo);

	public int updateReportInstanceActive(ReportInstanceVo reportInstanceVo);

	public int deleteReportInstanceAuthByReportInstanceId(Long reportInstanceId);

	public int deleteReportInstanceTableColumn(Long reportInstanceId);

	public int deleteReportInstanceById(Long reportInstanceId);

}
