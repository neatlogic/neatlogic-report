package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface ReportSqldefineService {

    JSONArray searchTable(String moduleId, String keyword);

    JSONObject getTable(String moduleId, String name);
}
