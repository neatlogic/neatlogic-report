/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
