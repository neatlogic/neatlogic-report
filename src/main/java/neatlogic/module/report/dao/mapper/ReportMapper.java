package neatlogic.module.report.dao.mapper;

import java.util.List;

import neatlogic.module.report.dto.ReportAuthVo;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportTypeVo;
import neatlogic.module.report.dto.ReportVo;
import org.apache.ibatis.annotations.Param;

public interface ReportMapper {

    List<ReportParamVo> getReportParamByReportId(Long reportId);

    int searchReportCount(ReportVo reportVo);

    List<ReportTypeVo> getAllReportType();

    List<ReportVo> searchReport(ReportVo reportVo);

    List<ReportAuthVo> getReportAuthByReportId(Long reportId);

    ReportVo getReportById(Long reportId);

    ReportVo getReportBaseInfo(Long reportId);

    int getReportParamCountByMatrixUuid(String matrixUuid);

    List<ReportParamVo> getReportParamByMatrixUuid(@Param("matrixUuid") String matrixUuid, @Param("startNum") int startNum, @Param("pageSize") int pageSize);

    ReportVo getReportByIdForUpudate(Long id);

    ReportVo getReportByName(String name);

    int checkReportNameIsExists(ReportVo reportVo);

    List<Long> checkReportIdListExists(List<Long> idList);

    int updateReportActive(ReportVo reportVo);

    int updateReport(ReportVo reportVo);

    int updateReportVisitCount(Long reportId);

    int updateReportInstanceVisitCount(Long reportInstanceId);

    int insertReport(ReportVo reportVo);

    int insertReportParam(ReportParamVo reportParamVo);

    int insertReportAuth(ReportAuthVo reportAuthVo);

    int batchInsertReportParam(List<ReportParamVo> list);

    int batchInsertReportAuth(List<ReportAuthVo> list);

    int deleteReportAuthByReportId(Long reportId);

    int deleteReportById(Long reportId);

    int deleteReportParamByReportId(Long reportId);

}
