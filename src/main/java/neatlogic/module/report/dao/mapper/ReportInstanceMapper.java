package neatlogic.module.report.dao.mapper;

import java.util.List;

import neatlogic.module.report.dto.ReportInstanceAuthVo;
import neatlogic.module.report.dto.ReportInstanceTableColumnVo;
import neatlogic.module.report.dto.ReportInstanceVo;

public interface ReportInstanceMapper {

	int checkReportInstanceExists(Long instanceId);

	ReportInstanceVo getReportInstanceById(Long reportInstanceId);

	List<ReportInstanceAuthVo> getReportInstanceAuthByReportInstanceId(Long reportInstanceId);

	List<ReportInstanceVo> searchReportInstance(ReportInstanceVo reportInstanceVo);

	int searchReportInstanceCount(ReportInstanceVo reportInstanceVo);

	List<ReportInstanceVo> getReportInstanceList(ReportInstanceVo reportInstanceVo);

	List<ReportInstanceTableColumnVo> getReportInstanceTableColumnList(Long reportInstanceId);

	/**
	 * @Description: 检查id为reportId的报表是否被报表实例引用
	 * @Author: laiwt
	 * @Date: 2021/1/18 11:52
	 * @Params: [reportId]
	 * @Returns: int
	**/
	int checkReportInstanceExistsByReportId(Long reportId);

	int insertReportInstance(ReportInstanceVo reportInstanceVo);

	int insertReportInstanceAuth(ReportInstanceAuthVo reportInstanceAuthVo);

	int batchInsertReportInstanceTableColumn(List<ReportInstanceTableColumnVo> list);

	int updateReportInstance(ReportInstanceVo reportInstanceVo);

	int updateReportInstanceActive(ReportInstanceVo reportInstanceVo);

	int deleteReportInstanceAuthByReportInstanceId(Long reportInstanceId);

	int deleteReportInstanceTableColumn(Long reportInstanceId);

	int deleteReportInstanceById(Long reportInstanceId);

}
