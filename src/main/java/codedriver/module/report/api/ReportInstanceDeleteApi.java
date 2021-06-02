package codedriver.module.report.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;
import codedriver.module.report.exception.ReportNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@AuthAction(action = REPORT_BASE.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class ReportInstanceDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private ReportInstanceMapper reportInstanceMapper;

    @Override
    public String getToken() {
        return "reportinstance/delete";
    }

    @Override
    public String getName() {
        return "删除报表实例";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表实例id", isRequired = true)})
    @Output({})
    @Description(desc = "删除报表实例")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        ReportInstanceVo instance = reportInstanceMapper.getReportInstanceById(id);
        if (instance == null) {
            throw new ReportNotFoundException(id);
        }
        // 如果没有REPORT_MODIFY权限且不是创建者，那么无权删除
        if (!AuthActionChecker.checkByUserUuid(REPORT_MODIFY.class.getSimpleName())
                && !Objects.equals(UserContext.get().getUserUuid(), instance.getFcu())) {
            throw new PermissionDeniedException();
        }
        reportInstanceMapper.deleteReportInstanceTableColumn(id);
        reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(id);
        reportInstanceMapper.deleteReportInstanceById(id);
        return null;
    }
}
