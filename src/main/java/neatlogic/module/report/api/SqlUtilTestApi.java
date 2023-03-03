/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.sqlrunner.SqlInfo;
import neatlogic.framework.sqlrunner.SqlRunner;
import neatlogic.module.report.auth.label.REPORT_ADMIN;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
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
