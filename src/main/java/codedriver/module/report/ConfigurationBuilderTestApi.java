/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.config.ConfigurationBuilder;
import codedriver.framework.dao.util.SqlUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.*;

@Service
public class ConfigurationBuilderTestApi extends PrivateBinaryStreamApiComponentBase {

    @Override
    public String getToken() {
        return "configurationBuilder/test";
    }
    @Override
    public String getName() {
        return "测试ConfigurationBuilder";
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
    @Description(desc = "测试ConfigurationBuilder")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long reportId = paramObj.getLong("reportId");
        ReportVo reportVo = reportMapper.getReportById(reportId);
        if (reportVo == null) {
            return null;
        }
        String sql = reportVo.getSql();
        Configuration configuration = new ConfigurationBuilder(dataSource).build(sql);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("reportId", reportId);
        Map<String, List> map = SqlUtil.executeSelectMappedStatement(configuration, paramMap);

        Collection<ResultMap> resultMapList = configuration.getResultMaps();
        for (ResultMap resultMap : resultMapList) {
            List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            for (ResultMapping resultMapping : resultMappingList) {
                System.out.println(JSONObject.toJSONString(resultMapping));
            }
            System.out.println(JSONObject.toJSONString(resultMap));
        }
        return null;
    }
}
