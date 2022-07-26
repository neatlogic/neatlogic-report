/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.sqlrunner.SqlInfo;
import codedriver.framework.sqlrunner.SqlRunner;
import codedriver.module.report.auth.label.REPORT_ADMIN;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
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
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        String sql = reportVo.getSql();
        SqlRunner sqlRunner = new SqlRunner(sql, "reportId_" + reportId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", reportId);
        List<String> needPageSelectIdList = new ArrayList<>();
//        Map<String, List> map = sqlRunner.runAllSql(paramMap);
        Map<String, Object> resultMap = new HashMap<>();
        List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(paramMap);
        for (SqlInfo sqlInfo : sqlInfoList) {
            List<String> parameterList = sqlInfo.getParameterList();
            if (parameterList.contains("startNum") && parameterList.contains("pageSize")) {
                needPageSelectIdList.add(sqlInfo.getId());
                continue;
            }
            List list = sqlRunner.runSqlById(sqlInfo, paramMap);
            resultMap.put(sqlInfo.getId(), list);
        }
        if (CollectionUtils.isNotEmpty(needPageSelectIdList)) {
            BasePageVo basePageVo = new BasePageVo();
            Integer currentPage = paramObj.getInteger("currentPage");
            if (currentPage != null) {
                basePageVo.setCurrentPage(currentPage);
            }
            Integer pageSize = paramObj.getInteger("pageSize");
            if (pageSize != null) {
                basePageVo.setPageSize(10);
            }
            Map<String, Object> pageMap = new HashMap<>();
            for (SqlInfo sqlInfo : sqlInfoList) {
                if (needPageSelectIdList.contains(sqlInfo.getId())) {
                    List list = (List) resultMap.remove(sqlInfo.getId() + "RowNum");
                    if (CollectionUtils.isNotEmpty(list)) {
                        Integer rowNum = (Integer) list.get(0);
                        if (rowNum != null) {
                            basePageVo.setRowNum(rowNum);
                            JSONObject pageObj = new JSONObject();
                            pageObj.put("rowNum", basePageVo.getRowNum());
                            pageObj.put("currentPage", basePageVo.getCurrentPage());
                            pageObj.put("pageSize", basePageVo.getPageSize());
                            pageObj.put("pageCount", basePageVo.getPageCount());
                            pageObj.put("needPage", true);
                            pageMap.put(sqlInfo.getId(), pageObj);
                            if (rowNum > 0) {
                                paramMap.put("startNum", basePageVo.getStartNum());
                                paramMap.put("pageSize", basePageVo.getPageSize());
                                list= sqlRunner.runSqlById(sqlInfo, paramMap);
                                if (CollectionUtils.isNotEmpty(list)) {
                                    resultMap.put(sqlInfo.getId(), list);
                                }
                            }
                        }
                    }
                }
            }
            resultMap.put("page", pageMap);
        }
        return null;
    }
}
