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

package neatlogic.module.report.schedule.plugin;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.util.EmailUtil;
import neatlogic.framework.util.ExportUtil;
import neatlogic.module.report.constvalue.ActionType;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportReceiverVo;
import neatlogic.module.report.dto.ReportSendJobRelationVo;
import neatlogic.module.report.dto.ReportSendJobVo;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 报表发送计划定时器
 */
@Component
@DisallowConcurrentExecution
public class ReportSendJob extends JobBase {
    static Logger logger = LoggerFactory.getLogger(ReportSendJob.class);

    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private UserMapper userMapper;


    @Resource
    private ReportService reportService;

    @Override
    public Boolean isMyHealthy(JobObject jobObject) {
        ReportSendJobVo jobVo = reportSendJobMapper.getJobBaseInfoById(Long.valueOf(jobObject.getJobName()));
        if (jobVo != null) {
            return jobVo.getIsActive().equals(1) && jobVo.getCron().equals(jobObject.getCron());
        }
        return false;
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        String tenantUuid = jobObject.getTenantUuid();
        TenantContext.get().switchTenant(tenantUuid);
        ReportSendJobVo jobVo = reportSendJobMapper.getJobBaseInfoById(Long.valueOf(jobObject.getJobName()));
        if (jobVo != null && Objects.equals(jobVo.getIsActive(), 1)) {
            JobObject newJobObject = new JobObject.Builder(jobVo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(jobVo.getCron()).addData("sendJobId", jobVo.getId()).build();
            schedulerManager.loadJob(newJobObject);
        } else {
            schedulerManager.unloadJob(jobObject);
        }
    }

    @Override
    public void initJob(String tenantUuid) {
        /* 初始化所有已激活的发送计划 */
        List<ReportSendJobVo> jobList = reportSendJobMapper.getAllActiveJob();
        if (CollectionUtils.isNotEmpty(jobList)) {
            for (ReportSendJobVo vo : jobList) {
                JobObject newJobObject = new JobObject.Builder(vo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(vo.getCron()).addData("sendJobId", vo.getId()).build();
                schedulerManager.loadJob(newJobObject);
            }
        }
    }

    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
        Long id = (Long) jobObject.getData("sendJobId");
        /* 获取报表发送计划*/
        ReportSendJobVo sendJob = reportSendJobMapper.getJobById(id);
        Map<String, InputStream> reportMap = null;
        String to = null;
        String cc = null;
        boolean canExec = false;
        if (sendJob != null && Objects.equals(sendJob.getIsActive(), 1)) {
            List<ReportSendJobRelationVo> relatedReportList = sendJob.getReportRelationList();
            /* 获取报表 */
            if (CollectionUtils.isNotEmpty(relatedReportList)) {
                reportMap = getReport(relatedReportList);
            }
            /* 获取收件人与抄送人 */
            List<ReportReceiverVo> receiverList = sendJob.getReceiverList();
            List<String> toEmailList = new ArrayList<>();
            List<String> ccEmailList = new ArrayList<>();
            getReceiverList(receiverList, toEmailList, ccEmailList);
            if (CollectionUtils.isNotEmpty(toEmailList)) {
                to = String.join(",", toEmailList);
            }
            if (CollectionUtils.isNotEmpty(ccEmailList)) {
                cc = String.join(",", ccEmailList);
            }
            if (MapUtils.isNotEmpty(reportMap) && StringUtils.isNotBlank(to)) {
                canExec = true;
            }
        }
        if (canExec) {
            /* 发送邮件 */
            try {
                EmailUtil.sendEmailWithFile(sendJob.getEmailTitle(), sendJob.getEmailContent(), to, cc, reportMap);
            } catch (Exception e) {
                throw new JobExecutionException(e.getMessage());
            }

        } else {
            schedulerManager.unloadJob(jobObject);
        }
    }

    /**
     * 获取收件人与抄送人
     * receiverList包含收件人与抄送人的UUID或email
     * 此方法根据UUID找到用户的email
     *
     * @param receiverList 所有接收者
     * @param toEmailList  接收者
     * @param ccEmailList  被抄送
     */
    private void getReceiverList(List<ReportReceiverVo> receiverList, List<String> toEmailList, List<String> ccEmailList) {
        if (CollectionUtils.isNotEmpty(receiverList)) {
            for (ReportReceiverVo vo : receiverList) {
                if ("to".equals(vo.getType())) { //收件人
                    if (!vo.getReceiver().contains("@")) {
                        UserVo user = userMapper.getUserBaseInfoByUuid(vo.getReceiver());
                        toEmailList.add(user.getEmail());
                    } else {
                        toEmailList.add(vo.getReceiver());
                    }
                } else if ("cc".equals(vo.getType())) { //抄送人
                    if (!vo.getReceiver().contains("@")) {
                        UserVo user = userMapper.getUserBaseInfoByUuid(vo.getReceiver());
                        ccEmailList.add(user.getEmail());
                    } else {
                        ccEmailList.add(vo.getReceiver());
                    }
                }
            }
        }
    }

    /**
     * 获取报表
     */
    private Map<String, InputStream> getReport(List<ReportSendJobRelationVo> relatedReportList) {
        Map<String, InputStream> reportMap = null;
        if (CollectionUtils.isNotEmpty(relatedReportList)) {
            reportMap = new HashMap<>();
            for (ReportSendJobRelationVo vo : relatedReportList) {
                ReportVo report = reportMapper.getReportById(vo.getReportId());
                if (report != null) {
                    try (ByteArrayOutputStream wordOs = new ByteArrayOutputStream(); ByteArrayOutputStream excelOs = new ByteArrayOutputStream()) {
                        JSONObject paramObj = JSONObject.parseObject(vo.getCondition());
                        if (MapUtils.isEmpty(paramObj)) {
                            paramObj = new JSONObject();
                        }
                        JSONObject filter = new JSONObject();
                        filter.putAll(paramObj);
                        Map<String, Object> returnMap = reportService.getQuerySqlResult(report, paramObj, null);
                        Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
                        Map<String, Object> tmpMap = new HashMap<>();
                        Map<String, Object> commonMap = new HashMap<>();
                        tmpMap.put("report", returnMap);
                        tmpMap.put("param", paramObj);
                        tmpMap.put("common", commonMap);
                        String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, returnMap, pageMap, filter, report.getContent(), ActionType.VIEW.getValue());
                        Workbook reportWorkbook = null;
                        try {
                            reportWorkbook = reportService.getReportWorkbook(content);
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                        if (reportWorkbook != null) {
                            reportWorkbook.write(excelOs);
                            InputStream excelIs = new ByteArrayInputStream(excelOs.toByteArray());
                            reportMap.put(report.getName() + ".xlsx", excelIs);
                        }
                        ExportUtil.getWordFileByHtml(content, wordOs, false, false);
                        InputStream wordIs = new ByteArrayInputStream(wordOs.toByteArray());
                        reportMap.put(report.getName() + ".docx", wordIs);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return reportMap;
    }

    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-REPORT-SEND";
    }

}
