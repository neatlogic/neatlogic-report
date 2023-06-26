/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.api;

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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
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

    @Override
    public String getToken() {
        return "report/save";
    }

    @Override
    public String getName() {
        return "保存报表定义";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表定义id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "报表定义名称"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "报表定义类型", defaultValue = ""),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活"),
            @Param(name = "sql", type = ApiParamType.STRING, desc = "报表定义数据源配置"),
            @Param(name = "condition", type = ApiParamType.STRING, desc = "报表定义条件配置"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "报表定义内容配置"),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "报表定义授权列表")})
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "保存报表定义")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        if (reportVo.getType() == null) {
            //type不能为null，兼容前端回选
            reportVo.setType("");
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
