/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.datasource;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.report.dto.ReportDataSourceConditionVo;
import codedriver.framework.report.dto.ReportDataSourceVo;
import codedriver.framework.report.exception.DataSourceConditionRequiredValueIsEmptyException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.module.report.auth.label.REPORT_ADMIN;
import codedriver.module.report.dao.mapper.ReportDataSourceMapper;
import codedriver.module.report.schedule.plugin.ReportDataSourceJob;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveReportDataSourceApi extends PrivateApiComponentBase {
    @Resource
    private SchedulerManager schedulerManager;

    @Resource
    private ReportDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "report/datasource/save";
    }

    @Override
    public String getName() {
        return "保存报表数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，不存在代表添加"),
            @Param(name = "label", type = ApiParamType.STRING, desc = "名称", maxLength = 50, isRequired = true, xss = true),
            @Param(name = "description", type = ApiParamType.STRING, desc = "说明", xss = true, maxLength = 500),
            @Param(name = "conditionList", type = ApiParamType.JSONARRAY, desc = "条件列表"),
            @Param(name = "cronExpression", type = ApiParamType.STRING, desc = "定时策略")})
    @Output({@Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ReportDataSourceVo[].class)})
    @Description(desc = "保存报表数据源接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportDataSourceVo reportDataSourceVo = JSONObject.toJavaObject(jsonObj, ReportDataSourceVo.class);
        reportDataSourceMapper.updateReportDataSource(reportDataSourceVo);
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getConditionList())) {
            for (ReportDataSourceConditionVo condition : reportDataSourceVo.getConditionList()) {
                if (condition.getIsRequired() == 1 && StringUtils.isBlank(condition.getValue())) {
                    throw new DataSourceConditionRequiredValueIsEmptyException(condition);
                }
                reportDataSourceMapper.updateReportDataSourceConditionValue(condition);
            }
        }

        String tenantUuid = TenantContext.get().getTenantUuid();
        IJob jobHandler = SchedulerManager.getHandler(ReportDataSourceJob.class.getName());
        JobObject jobObject = new JobObject.Builder(reportDataSourceVo.getId().toString(), jobHandler.getGroupName(), jobHandler.getClassName(), tenantUuid).build();
        if (StringUtils.isNotBlank(reportDataSourceVo.getCronExpression())) {
            schedulerManager.loadJob(jobObject);
        } else {
            schedulerManager.unloadJob(jobObject);
        }
        return null;
    }

}