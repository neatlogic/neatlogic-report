/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.framework.report.exception.ReportInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dto.ReportInstanceVo;

import java.util.Objects;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateReportInstanceActiveApi extends PrivateApiComponentBase {

    @Autowired
    private ReportInstanceMapper reportInstanceMapper;

    @Override
    public String getToken() {
        return "reportinstance/toggleactive";
    }

    @Override
    public String getName() {
        return "更改报表激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表id"), @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活")})
    @Description(desc = "更改报表激活状态")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportInstanceVo reportVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);
        ReportInstanceVo instance = reportInstanceMapper.getReportInstanceById(reportVo.getId());
        if (instance == null) {
            throw new ReportInstanceNotFoundException(reportVo.getId());
        }
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName()) && !Objects.equals(UserContext.get().getUserUuid(), instance.getFcu())) {
            throw new PermissionDeniedException(REPORT_MODIFY.class);
        }
        reportVo.setLcu(UserContext.get().getUserUuid());
        reportInstanceMapper.updateReportInstanceActive(reportVo);
        return null;
    }
}
