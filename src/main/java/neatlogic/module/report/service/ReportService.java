package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.sqlrunner.SqlInfo;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ReportService {

    ReportVo getReportDetailById(Long reportId);

    Map<String, List<String>> getShowColumnsMap(Long reportInstanceId);

    @Transactional
    int deleteReportById(Long reportId);

    List<SqlInfo> getTableList(String content);

    Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap);

    Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap, List<SqlInfo> tableList);

    /**
     *
     * @param reportVo 报表配置信息
     * @param paramMap 参数数据
     * @param showColumnsMap 显示列信息
     * @param needExecuteSqlIdList 需要执行的sqlId列表，如果为空，代表全部执行
     * @param tableList 表格sql信息列表
     * @return
     */
    Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap, List<String> needExecuteSqlIdList, List<SqlInfo> tableList);

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
