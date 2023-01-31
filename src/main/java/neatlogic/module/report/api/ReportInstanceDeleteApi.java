/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dto.ReportInstanceVo;
import neatlogic.framework.report.exception.ReportNotFoundException;
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
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())
                && !Objects.equals(UserContext.get().getUserUuid(), instance.getFcu())) {
            throw new PermissionDeniedException(REPORT_MODIFY.class);
        }
        reportInstanceMapper.deleteReportInstanceTableColumn(id);
        reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(id);
        reportInstanceMapper.deleteReportInstanceById(id);
        return null;
    }
}
