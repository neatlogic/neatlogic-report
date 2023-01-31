/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api.schedule;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportSendJobNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportSendJobVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class ReportSendJobStatusUpdateApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "report/sendjob/status/update";
    }

    @Override
    public String getName() {
        return "修改报表发送计划激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "报表发送计划ID"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
    })
    @Output({})
    @Description(desc = "修改报表发送计划激活状态")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportSendJobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>() {
        });
        ReportSendJobVo job = reportSendJobMapper.getJobBaseInfoById(jobVo.getId());
        jobVo.setLcu(UserContext.get().getUserUuid());
        if (job == null) {
            throw new ReportSendJobNotFoundException(jobVo.getId());
        } else {
            reportSendJobMapper.updateJobStatus(jobVo);
        }
        IJob handler = SchedulerManager.getHandler("neatlogic.module.report.schedule.plugin.ReportSendJob");
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("sendJobId", job.getId()).build();
        if (jobVo.getIsActive() == 1) {
            schedulerManager.loadJob(newJobObject);
        } else {
            schedulerManager.unloadJob(newJobObject);
        }

        return null;
    }
}
