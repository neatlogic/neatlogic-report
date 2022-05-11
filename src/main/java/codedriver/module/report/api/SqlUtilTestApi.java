/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.util.SqlUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
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
    private DataSource dataSource;

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
        SqlUtil sqlUtil = new SqlUtil.SqlUtilBuilder(dataSource).withNamespace("reportId_" + reportId).build(sql);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportId", reportId);
        Map<String, List> map = sqlUtil.executeAllSelectMappedStatement(paramMap);
        List<String> idList = sqlUtil.getAllSelectMappedStatementIdList();
        for (String id : idList) {
            System.out.println(id);
            List list = sqlUtil.executeAllSelectMappedStatementById(id, paramMap);
            for (Object obj : list) {
                System.out.println(JSONObject.toJSONString(obj));
            }
        }
        List<Map<String, String>> resultMappingList = sqlUtil.getAllResultMappingList();
        for (Map<String, String> resultMapping : resultMappingList) {
            System.out.println(JSONObject.toJSONString(resultMapping));
        }
        List<String> resultMapIdList = sqlUtil.getResultMapIdList();
        for (String resultMapId : resultMapIdList) {
            List<Map<String, String>> resultMappingList2 = sqlUtil.getResultMappingListByResultMapId(resultMapId);
        }
        return null;
    }
}
