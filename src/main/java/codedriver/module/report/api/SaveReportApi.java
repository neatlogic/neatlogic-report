/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.report.exception.ReportRepeatException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Service
@Transactional
public class SaveReportApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

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
        if (CollectionUtils.isNotEmpty(reportVo.getParamList())) {
            int i = 0;
            for (ReportParamVo paramVo : reportVo.getParamList()) {
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
