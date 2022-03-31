/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.datasource;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.report.dto.ReportDataSourceAuditVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import codedriver.module.report.auth.label.REPORT_DATASOURCE_MODIFY;
import codedriver.module.report.dao.mapper.ReportDataSourceAuditMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_DATASOURCE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchReportDataSourceAuditApi extends PrivateApiComponentBase {

    @Resource
    private ReportDataSourceAuditMapper reportDataSourceAuditMapper;

    @Override
    public String getToken() {
        return "report/datasource/audit/search";
    }

    @Override
    public String getName() {
        return "搜索数据源同步审计信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "datasourceId", type = ApiParamType.LONG, desc = "数据源id", isRequired = true)})
    @Description(desc = "搜索数据源同步审计信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportDataSourceAuditVo reportDataSourceAuditVo = JSONObject.toJavaObject(jsonObj, ReportDataSourceAuditVo.class);
        List<ReportDataSourceAuditVo> auditList = reportDataSourceAuditMapper.searchReportDataSourceAudit(reportDataSourceAuditVo);
        if (CollectionUtils.isNotEmpty(auditList)) {
            int rowNum = reportDataSourceAuditMapper.searchReportDataSourceAuditCount(reportDataSourceAuditVo);
            reportDataSourceAuditVo.setRowNum(rowNum);
        }
        return TableResultUtil.getResult(auditList, reportDataSourceAuditVo);
    }

}
