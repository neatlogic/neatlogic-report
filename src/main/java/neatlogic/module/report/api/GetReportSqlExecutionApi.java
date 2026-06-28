/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.sqlrunner.SqlInfo;
import neatlogic.framework.sqlrunner.SqlRunner;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetReportSqlExecutionApi extends PrivateApiComponentBase {

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/sql/execution/get";
    }

    @Override
    public String getName() {
        return "获取报表执行SQL";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)})
    @Output({@Param(name = "sqlList", type = ApiParamType.JSONARRAY, desc = "SQL列表"),
            @Param(name = "param", type = ApiParamType.JSONOBJECT, desc = "执行参数")})
    @Description(desc = "获取报表执行SQL")
    @Override
    public Object myDoService(JSONObject paramObj) {
        Long reportId = paramObj.getLong("id");
        ReportVo reportVo = reportService.getReportDetailById(reportId);
        if (reportVo == null) {
            throw new ReportNotFoundException(reportId);
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("reportId", reportId);
        resultObj.put("param", paramObj);
        JSONArray sqlArray = new JSONArray();
        resultObj.put("sqlList", sqlArray);
        if (StringUtils.isBlank(reportVo.getSql())) {
            return resultObj;
        }
        SqlRunner sqlRunner = new SqlRunner(reportVo.getSql(), "reportId_" + reportId);
        List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(paramObj);
        List<SqlInfo> tableList = reportService.getTableList(reportVo.getContent());
        if (CollectionUtils.isNotEmpty(tableList)) {
            for (SqlInfo sqlInfo : sqlInfoList) {
                for (SqlInfo tableInfo : tableList) {
                    if (Objects.equals(sqlInfo.getId(), tableInfo.getId())) {
                        sqlInfo.setNeedPage(tableInfo.getNeedPage());
                        sqlInfo.setPageSize(tableInfo.getPageSize());
                        break;
                    }
                }
            }
        }
        for (SqlInfo sqlInfo : sqlInfoList) {
            JSONObject sqlObj = new JSONObject();
            sqlObj.put("id", sqlInfo.getId());
            sqlObj.put("sql", sqlInfo.getSql());
            sqlObj.put("executableSql", sqlInfo.getExecutableSql());
            sqlObj.put("needPage", sqlInfo.getNeedPage());
            sqlObj.put("pageSize", sqlInfo.getPageSize());
            sqlObj.put("currentPage", paramObj.getInteger("currentPage") == null ? 1 : paramObj.getInteger("currentPage"));
            sqlObj.put("parameterList", sqlInfo.getParameterList());
            sqlObj.put("parameterValueList", getParameterValueList(sqlInfo.getParameterList(), paramObj));
            sqlArray.add(sqlObj);
        }
        return resultObj;
    }

    private JSONArray getParameterValueList(List<String> parameterList, JSONObject paramObj) {
        JSONArray parameterValueArray = new JSONArray();
        if (CollectionUtils.isEmpty(parameterList)) {
            return parameterValueArray;
        }
        for (String parameter : parameterList) {
            JSONObject parameterObj = new JSONObject();
            parameterObj.put("name", parameter);
            parameterObj.put("value", paramObj.get(parameter));
            parameterValueArray.add(parameterObj);
        }
        return parameterValueArray;
    }
}
