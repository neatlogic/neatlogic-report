package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.exception.ReportNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AuthAction(action = REPORT_MODIFY.class)
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
        if(reportInstanceMapper.getReportInstanceById(id) == null){
            throw new ReportNotFoundException(id);
        }
        reportInstanceMapper.deleteReportInstanceTableColumn(id);
        reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(id);
        reportInstanceMapper.deleteReportInstanceById(id);
        return null;
    }
}
