/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.schedule.plugin;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.scheduler.core.JobBase;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.util.EmailUtil;
import codedriver.module.report.constvalue.ActionType;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportReceiverVo;
import codedriver.module.report.dto.ReportSendJobRelationVo;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;
import codedriver.framework.util.ExportUtil;
import codedriver.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 报表发送计划定时器
 */
@Component
public class ReportSendJob extends JobBase {
	static Logger logger = LoggerFactory.getLogger(ReportSendJob.class);

	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
	private ReportMapper reportMapper;

    @Autowired
    private UserMapper userMapper;

	@Autowired
	private ReportService reportService;

	@Override
    public Boolean isHealthy(JobObject jobObject) {
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
		if (jobVo != null && Objects.equals(jobVo.getIsActive(),1)) {
			JobObject newJobObject = new JobObject.Builder(jobVo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(jobVo.getCron()).addData("sendJobId", jobVo.getId()).build();
			schedulerManager.loadJob(newJobObject);
		} else {
			schedulerManager.unloadJob(jobObject);
		}
	}

	@Override
	public void initJob(String tenantUuid) {
		/** 初始化所有已激活的发送计划 */
		List<ReportSendJobVo> jobList = reportSendJobMapper.getAllActiveJob();
		if(CollectionUtils.isNotEmpty(jobList)){
			for(ReportSendJobVo vo : jobList){
				JobObject newJobObject = new JobObject.Builder(vo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(vo.getCron()).addData("sendJobId",vo.getId()).build();
				schedulerManager.loadJob(newJobObject);
			}
		}
	}

	@Override
	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
		Long id = (Long) jobObject.getData("sendJobId");
		/** 获取报表发送计划*/
        ReportSendJobVo sendJob = reportSendJobMapper.getJobById(id);
        Map<String, InputStream> reportMap = null;
        String to = null;
        String cc = null;
        boolean canExec = false;
        if(sendJob != null && Objects.equals(sendJob.getIsActive(),1)) {
            List<ReportSendJobRelationVo> relatedReportList = sendJob.getReportRelationList();
            /** 获取报表 */
            if(CollectionUtils.isNotEmpty(relatedReportList)) {
                reportMap = getReport(relatedReportList);
            }
            /** 获取收件人与抄送人 */
            List<ReportReceiverVo> receiverList = sendJob.getReceiverList();
            List<String> toEmailList = new ArrayList<>();
            List<String> ccEmailList = new ArrayList<>();
            getReceiverList(receiverList, toEmailList, ccEmailList);
            if(CollectionUtils.isNotEmpty(toEmailList)) {
                to = String.join(",", toEmailList);
            }
            if(CollectionUtils.isNotEmpty(ccEmailList)){
                cc = String.join(",", ccEmailList);
            }
            if(MapUtils.isNotEmpty(reportMap) && StringUtils.isNotBlank(to)){
                canExec = true;
            }
        }
        if(canExec){
            /** 发送邮件 */
            try{
                EmailUtil.sendEmail(sendJob.getEmailTitle(),sendJob.getEmailContent(), reportMap, to, cc);
            }catch (Exception e){
                throw new JobExecutionException(e.getMessage());
            }

        }else{
            schedulerManager.unloadJob(jobObject);
        }
	}

    /**
     * 获取收件人与抄送人
     * receiverList包含收件人与抄送人的UUID或email
     * 此方法根据UUID找到用户的email
     * @param receiverList
     * @param toEmailList
     * @param ccEmailList
     */
    private void getReceiverList(List<ReportReceiverVo> receiverList, List<String> toEmailList, List<String> ccEmailList) {
        if(CollectionUtils.isNotEmpty(receiverList)){
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
     * @param relatedReportList
     * @return
     */
    private Map<String, InputStream> getReport(List<ReportSendJobRelationVo> relatedReportList) {
        Map<String, InputStream> reportMap = null;
        if (CollectionUtils.isNotEmpty(relatedReportList)) {
            reportMap = new HashMap<>();
            for (ReportSendJobRelationVo vo : relatedReportList) {
                ByteArrayOutputStream os = null;
                ReportVo report = reportMapper.getReportById(vo.getReportId());
                if (report != null) {
                    try{
                        JSONObject paramObj = JSONObject.parseObject(vo.getCondition());
                        if(MapUtils.isEmpty(paramObj)){
                            paramObj = new JSONObject();
                        }
                        os = new ByteArrayOutputStream();
                        Map<String, Long> timeMap = new HashMap<>();
                        Map<String, Object> returnMap = reportService.getQueryResult(report.getId(), paramObj, timeMap, false,null);
                        Map<String, Object> tmpMap = new HashMap<>();
                        Map<String, Object> commonMap = new HashMap<>();
                        tmpMap.put("report", returnMap);
                        tmpMap.put("param", paramObj);
                        tmpMap.put("common", commonMap);
                        String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, report.getContent(),ActionType.VIEW.getValue());
                        ExportUtil.getPdfFileByHtml(content, true, os);
                        InputStream is = new ByteArrayInputStream(os.toByteArray());
                        reportMap.put(report.getName(), is);
                        os.close();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if(os != null){
                            try {
                                os.flush();
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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
