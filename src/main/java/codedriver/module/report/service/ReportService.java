package codedriver.module.report.service;

import codedriver.framework.sqlrunner.SqlInfo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Object> getQueryResult(Long reportId, JSONObject paramMap, Map<String, Long> timeMap, boolean isFirst, Map<String, List<String>> showColumnsMap) throws Exception;

    ReportVo getReportDetailById(Long reportId);

    Map<String, List<String>> getShowColumnsMap(Long reportInstanceId);

    @Transactional
    int deleteReportById(Long reportId);

    Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap);

    Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap, List<SqlInfo> tableList);

    Map<String, Object> getQuerySqlResultById(String id, ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap);

    void validateReportParamList(List<ReportParamVo> paramList);

    /**
     * 抽取{content}中的表格并生成Workbook
     *
     * @param content HTML
     * @return
     */
    Workbook getReportWorkbook(String content);
}
