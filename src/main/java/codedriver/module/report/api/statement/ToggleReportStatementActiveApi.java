/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.statement;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
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
public class ToggleReportStatementActiveApi extends PrivateApiComponentBase {
    @Resource
    private ReportStatementMapper reportStatementMapper;


    @Override
    public String getToken() {
        return "report/statement/toggleactive";
    }

    @Override
    public String getName() {
        return "激活/禁用报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "id"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活")})
    @Description(desc = "激活/禁用报表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        return null;
    }

}
