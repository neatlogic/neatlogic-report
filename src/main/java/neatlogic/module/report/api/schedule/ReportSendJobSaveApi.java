/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.api.schedule;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.report.exception.ReportSendJobNameRepeatException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.scheduler.exception.ScheduleIllegalParameterException;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportReceiverVo;
import neatlogic.module.report.dto.ReportSendJobRelationVo;
import neatlogic.module.report.dto.ReportSendJobVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * reportList结构形如：
 * [{"id":167237352693760,"condition":{"beforeDay":"30","keyword":"a"},"formValue":[{"beforeDay":"90&=&过去90天"}]},{"id":158660705656832,"condition":{"beforeDay":"7"}}]
 */
@Service
@AuthAction(action = REPORT_MODIFY.class)
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ReportSendJobSaveApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "report/sendjob/save";
    }

    @Override
    public String getName() {
        return "保存报表发送计划";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "报表发送计划ID"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, xss = true, desc = "报表发送计划名称"),
            @Param(name = "emailTitle", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "邮件标题"),
            @Param(name = "emailContent", type = ApiParamType.STRING, desc = "邮件正文"),
            @Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
            @Param(name = "toList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "收件人列表，可填用户UUID或邮箱"),
            @Param(name = "ccList", type = ApiParamType.JSONARRAY, desc = "抄送人列表，可填用户UUID或邮箱"),
            @Param(name = "reportList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "报表列表")
    })
    @Output({@Param(name = "id", type = ApiParamType.LONG, desc = "计划ID")})
    @Description(desc = "保存报表发送计划")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportSendJobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>() {
        });
        if (!CronExpression.isValidExpression(jobVo.getCron())) {
            throw new ScheduleIllegalParameterException(jobVo.getCron());
        }
        if (reportSendJobMapper.checkNameIsRepeat(jobVo) > 0) {
            throw new ReportSendJobNameRepeatException(jobVo.getName());
        }

        JSONArray toArray = jsonObj.getJSONArray("toList");
        JSONArray ccArray = jsonObj.getJSONArray("ccList");
        JSONArray reportList = jsonObj.getJSONArray("reportList");
        /** 读取收件人与抄送人 */
        List<ReportReceiverVo> toVoList = new ArrayList<>();
        List<ReportReceiverVo> ccVoList = new ArrayList<>();
        getToAndCcList(jobVo, toArray, ccArray, toVoList, ccVoList);
        /** 读取关联的报表 */
        List<ReportSendJobRelationVo> relationList = getRelations(jobVo, reportList);

        ReportSendJobVo oldJobVo = reportSendJobMapper.getJobBaseInfoById(jobVo.getId());
        jobVo.setLcu(UserContext.get().getUserUuid());
        if (oldJobVo == null) {
            jobVo.setFcu(UserContext.get().getUserUuid());
            reportSendJobMapper.insertJob(jobVo);
        } else {
            reportSendJobMapper.updateJob(jobVo);
            reportSendJobMapper.deleteReportReceiver(jobVo.getId());
            reportSendJobMapper.deleteReportRelation(jobVo.getId());
        }
        /**保存邮件接收人*/
        if (CollectionUtils.isNotEmpty(toVoList)) {
            reportSendJobMapper.batchInsertReportReceiver(toVoList);
        }
        if (CollectionUtils.isNotEmpty(ccVoList)) {
            reportSendJobMapper.batchInsertReportReceiver(ccVoList);
        }
        /** 保存关联的报表 */
        if (CollectionUtils.isNotEmpty(relationList)) {
            reportSendJobMapper.batchInsertReportRelation(relationList);
        }
        /** 根据isActive启动或清除定时任务 */
        IJob handler = SchedulerManager.getHandler("neatlogic.module.report.schedule.plugin.ReportSendJob");
        String tenantUuid = TenantContext.get().getTenantUuid();
        JobObject newJobObject = new JobObject.Builder(jobVo.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(jobVo.getCron()).addData("sendJobId", jobVo.getId()).build();
        if (jobVo.getIsActive().intValue() == 1) {
            schedulerManager.loadJob(newJobObject);
        } else {
            schedulerManager.unloadJob(newJobObject);
        }

        JSONObject result = new JSONObject();
        result.put("id", jobVo.getId());

        return result;
    }

    public IValid name() {
        return value -> {
            ReportSendJobVo jobVo = JSON.toJavaObject(value, ReportSendJobVo.class);
            if (reportSendJobMapper.checkNameIsRepeat(jobVo) > 0) {
                return new FieldValidResultVo(new ReportSendJobNameRepeatException(jobVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

    /**
     * 读取关联的报表
     */
    private List<ReportSendJobRelationVo> getRelations(ReportSendJobVo jobVo, JSONArray reportList) {
        List<ReportSendJobRelationVo> relationList = null;
        if (CollectionUtils.isNotEmpty(reportList)) {
            relationList = new ArrayList<>();
            for (int i = 0; i < reportList.size(); i++) {
                JSONObject report = JSONObject.parseObject(reportList.get(i).toString());
                ReportSendJobRelationVo vo = new ReportSendJobRelationVo();
                vo.setReportSendJobId(jobVo.getId());
                vo.setReportId(report.getLong("id"));
                vo.setCondition(report.getString("condition"));
                vo.setConfig(report.getString("formValue"));
                vo.setSort(i);
                relationList.add(vo);
            }
        }
        return relationList;
    }

    /**
     * 读取收件人与抄送人
     */
    private void getToAndCcList(ReportSendJobVo jobVo, JSONArray toArray, JSONArray ccArray, List<ReportReceiverVo> toVoList, List<ReportReceiverVo> ccVoList) {
        if (CollectionUtils.isNotEmpty(toArray)) {
            List<String> toList = toArray.toJavaList(String.class);
            for (String s : toList) {
                ReportReceiverVo vo = new ReportReceiverVo();
                vo.setReportSendJobId(jobVo.getId());
                vo.setReceiver(s);
                vo.setType("to");
                toVoList.add(vo);
            }
        }
        if (CollectionUtils.isNotEmpty(ccArray)) {
            List<String> ccList = ccArray.toJavaList(String.class);
            for (String s : ccList) {
                ReportReceiverVo vo = new ReportReceiverVo();
                vo.setReportSendJobId(jobVo.getId());
                vo.setReceiver(s);
                vo.setType("cc");
                ccVoList.add(vo);
            }
        }
    }

}
