package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.service.ReportSqldefineService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetReportSqlTableApi extends PrivateApiComponentBase {

    @Resource
    private ReportSqldefineService reportSqldefineService;

    @Override
    public String getToken() {
        return "report/sqldefine/table/get";
    }

    @Override
    public String getName() {
        return "获取报表SQL表定义";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id", isRequired = true),
            @Param(name = "name", type = ApiParamType.STRING, desc = "表名", isRequired = true)
    })
    @Output({@Param(type = ApiParamType.JSONOBJECT, desc = "表定义")})
    @Description(desc = "获取报表SQL表定义")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        JSONObject table = reportSqldefineService.getTable(jsonObj.getString("moduleId"), jsonObj.getString("name"));
        return table == null ? new JSONObject() : table;
    }
}
