package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONObject;

public interface ReportSqlGraphService {

    JSONObject buildSql(JSONObject sqlGraphConfig);

    JSONObject analyzeSql(String sql, JSONObject sqlGraphConfig);
}
