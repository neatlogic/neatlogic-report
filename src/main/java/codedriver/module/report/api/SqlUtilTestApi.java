/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.sqlrunner.SqlInfo;
import codedriver.framework.sqlrunner.SqlRunner;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
public class SqlUtilTestApi extends PrivateBinaryStreamApiComponentBase {

    @Override
    public String getToken() {
        return "sqlutil/test";
    }

    @Override
    public String getName() {
        return "测试SqlUtil";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Resource
    private ReportMapper reportMapper;

    @Input({
            @Param(name = "reportId", type = ApiParamType.LONG, isRequired = true, desc = "报表id")
    })
    @Description(desc = "测试SqlUtil")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long reportId = paramObj.getLong("reportId");
        ReportVo reportVo = reportMapper.getReportById(reportId);
        if (reportVo == null) {
            return null;
        }
        String sql = reportVo.getSql();
        SqlRunner sqlRunner = new SqlRunner(sql, "reportId_" + reportId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportId", reportId);
        List<Long> reportIdList = new ArrayList<>();
        reportIdList.add(reportId);
        paramMap.put("reportIdList", reportIdList);
        Map<String, List> map = sqlRunner.runAllSql(paramMap);
        Map<String, Object> returnResultMap = new HashMap<>();
        List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(paramMap);
        for (SqlInfo sqlInfo : sqlInfoList) {
            List list = sqlRunner.runSqlById(sqlInfo.getId(), paramMap);
            returnResultMap.put(sqlInfo.getId(), list);
            System.out.println(JSONObject.toJSONString(sqlInfo));
        }
        return null;
    }
}
