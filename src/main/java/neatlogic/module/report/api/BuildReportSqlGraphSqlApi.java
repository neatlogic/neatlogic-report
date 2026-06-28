package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.service.ReportSqlGraphService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class BuildReportSqlGraphSqlApi extends PrivateApiComponentBase {

    @Resource
    private ReportSqlGraphService reportSqlGraphService;

    @Override
    public String getToken() {
        return "report/sqlgraph/sql/build";
    }

    @Override
    public String getName() {
        return "生成报表绘图SQL";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "sqlGraphConfig", type = ApiParamType.JSONOBJECT, desc = "报表SQL绘图配置", isRequired = true)})
    @Output({@Param(name = "sql", type = ApiParamType.STRING, desc = "MyBatis XML"),
            @Param(name = "tableList", type = ApiParamType.JSONARRAY, desc = "数据集列表"),
            @Param(name = "errorList", type = ApiParamType.JSONARRAY, desc = "错误列表")})
    @Description(desc = "生成报表绘图SQL")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return reportSqlGraphService.buildSql(jsonObj.getJSONObject("sqlGraphConfig"));
    }
}
