package codedriver.module.report.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.auth.core.AuthFactory;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.exception.ReportInstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;

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
