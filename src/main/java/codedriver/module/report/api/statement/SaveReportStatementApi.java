/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.statement;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.report.dto.ReportStatementVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_STATEMENT_MODIFY;
import codedriver.module.report.dao.mapper.ReportStatementMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_STATEMENT_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveReportStatementApi extends PrivateApiComponentBase {
    @Resource
    private ReportStatementMapper reportStatementMapper;


    @Override
    public String getToken() {
        return "report/statement/save";
    }

    @Override
    public String getName() {
        return "保存报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，不存在代表添加"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "名称", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "description", type = ApiParamType.STRING, desc = "说明", xss = true, maxLength = 500),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", defaultValue = "0"),
            @Param(name = "width", type = ApiParamType.INTEGER, desc = "画布宽度"),
            @Param(name = "height", type = ApiParamType.INTEGER, desc = "画布高度"),
            @Param(name = "widgetList", type = ApiParamType.JSONARRAY, desc = "组件列表", isRequired = true)})
    @Description(desc = "保存报表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportStatementVo reportStatementVo = JSONObject.toJavaObject(jsonObj, ReportStatementVo.class);
        if (jsonObj.getLong("id") == null) {
            reportStatementVo.setFcu(UserContext.get().getUserUuid(true));
            reportStatementMapper.insertReportStatement(reportStatementVo);
        } else {
            reportStatementVo.setLcu(UserContext.get().getUserUuid(true));
            reportStatementMapper.updateReportStatement(reportStatementVo);
        }
        return reportStatementVo.getId();
    }

}
