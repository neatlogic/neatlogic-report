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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.report.exception.ReportRepeatException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportAuthVo;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.service.ReportSqlGraphService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Service
@Transactional
public class SaveReportApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Resource
    private ReportSqlGraphService reportSqlGraphService;

    @Override
    public String getToken() {
        return "report/save";
    }

    @Override
    public String getName() {
        return "nmra.savereportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "common.name"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "common.type", defaultValue = ""),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive"),
            @Param(name = "sql", type = ApiParamType.STRING, desc = "nmra.savereportapi.input.param.desc.sql"),
            @Param(name = "sqlEditMode", type = ApiParamType.STRING, desc = "SQL编辑模式"),
            @Param(name = "sqlGraphConfig", type = ApiParamType.STRING, desc = "SQL绘图配置"),
            @Param(name = "condition", type = ApiParamType.STRING, desc = "common.condition"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "common.content"),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "common.authlist")})
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "nmra.savereportapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        if (reportVo.getType() == null) {
            //type不能为null，兼容前端回选
            reportVo.setType("");
        }
        if (StringUtils.isBlank(reportVo.getSqlEditMode())) {
            // 老报表没有编辑模式字段时，统一按原 XML 配置方式处理。
            reportVo.setSqlEditMode("xml");
        }
        if (StringUtils.equals(reportVo.getSqlEditMode(), "graph") && StringUtils.isNotBlank(reportVo.getSqlGraphConfig())) {
            JSONObject buildResult = reportSqlGraphService.buildSql(JSONObject.parseObject(reportVo.getSqlGraphConfig()));
            if (CollectionUtils.isNotEmpty(buildResult.getJSONArray("errorList"))) {
                throw new IllegalArgumentException(StringUtils.join(buildResult.getJSONArray("errorList"), "；"));
            }
            reportVo.setSql(buildResult.getString("sql"));
        }
        if (reportMapper.checkReportNameIsExists(reportVo) > 0) {
            throw new ReportRepeatException(reportVo.getName());
        }
        reportVo.setLcu(UserContext.get().getUserUuid());
        if (jsonObj.getLong("id") != null) {
            reportMapper.deleteReportAuthByReportId(reportVo.getId());
            reportMapper.deleteReportParamByReportId(reportVo.getId());
            reportMapper.updateReport(reportVo);
        } else {
            reportVo.setFcu(UserContext.get().getUserUuid());
            reportMapper.insertReport(reportVo);
        }
        List<ReportParamVo> paramList = reportVo.getParamList();
        if (CollectionUtils.isNotEmpty(paramList)) {
            reportService.validateReportParamList(paramList);
            int i = 0;
            for (ReportParamVo paramVo : paramList) {
                paramVo.setReportId(reportVo.getId());
                paramVo.setSort(i);
                reportMapper.insertReportParam(paramVo);
                i += 1;
            }
        }
        if (CollectionUtils.isNotEmpty(reportVo.getAuthList())) {
            for (String auth : reportVo.getAuthList()) {
                ReportAuthVo reportAuthVo = new ReportAuthVo(reportVo.getId(), auth.split("#")[0], auth.split("#")[1]);
                reportMapper.insertReportAuth(reportAuthVo);
            }
        }
        return reportVo.getId();
    }

    public IValid name() {
        return value -> {
            ReportVo reportVo = JSON.toJavaObject(value, ReportVo.class);
            if (reportMapper.checkReportNameIsExists(reportVo) > 0) {
                return new FieldValidResultVo(new ReportRepeatException(reportVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
