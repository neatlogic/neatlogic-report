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
public class AnalyzeReportSqlGraphXmlApi extends PrivateApiComponentBase {

    @Resource
    private ReportSqlGraphService reportSqlGraphService;

    @Override
    public String getToken() {
        return "report/sqlgraph/xml/analyze";
    }

    @Override
    public String getName() {
        return "分析报表SQL XML";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "sql", type = ApiParamType.STRING, desc = "MyBatis XML", isRequired = true),
            @Param(name = "sqlGraphConfig", type = ApiParamType.JSONOBJECT, desc = "当前SQL绘图配置")})
    @Output({@Param(name = "tableList", type = ApiParamType.JSONARRAY, desc = "数据集列表"),
            @Param(name = "sqlGraphConfig", type = ApiParamType.JSONOBJECT, desc = "同步后的SQL绘图配置"),
            @Param(name = "errorList", type = ApiParamType.JSONARRAY, desc = "错误列表")})
    @Description(desc = "分析报表SQL XML")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        return reportSqlGraphService.analyzeSql(jsonObj.getString("sql"), jsonObj.getJSONObject("sqlGraphConfig"));
    }
}
