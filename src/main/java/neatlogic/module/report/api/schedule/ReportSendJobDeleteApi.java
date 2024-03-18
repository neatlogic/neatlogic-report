/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.report.api.schedule;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportSendJobNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportSendJobVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ReportSendJobDeleteApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private SchedulerMapper schedulerMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "report/sendjob/delete";
    }

    @Override
    public String getName() {
        return "删除报表发送计划";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表发送计划ID")})
    @Output({})
    @Description(desc = "删除报表发送计划")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        ReportSendJobVo job = reportSendJobMapper.getJobBaseInfoById(id);
        if (job == null) {
            throw new ReportSendJobNotFoundException(id);
        }
        /* 清除定时任务 */
        IJob handler = SchedulerManager.getHandler("neatlogic.module.report.schedule.plugin.ReportSendJob");
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobObject newJobObject = new JobObject.Builder(job.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(job.getCron()).addData("sendJobId", job.getId()).build();
        schedulerManager.unloadJob(newJobObject);
        reportSendJobMapper.deleteReportReceiver(id);
        reportSendJobMapper.deleteReportRelation(id);
        schedulerMapper.deleteJobAuditByJobUuid(id.toString());
        reportSendJobMapper.deleteJobById(id);
        return null;
    }
}
