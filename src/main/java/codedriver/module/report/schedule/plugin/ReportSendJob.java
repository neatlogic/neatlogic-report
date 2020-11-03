//package codedriver.module.report.schedule.plugin;
//
//import codedriver.framework.asynchronization.threadlocal.TenantContext;
//import codedriver.framework.dao.mapper.MailServerMapper;
//import codedriver.framework.dto.MailServerVo;
//import codedriver.framework.notify.exception.EmailServerNotFoundException;
//import codedriver.framework.scheduler.core.JobBase;
//import codedriver.framework.scheduler.dto.JobObject;
//import codedriver.module.report.dao.mapper.ReportMapper;
//import codedriver.module.report.dao.mapper.ReportSendJobMapper;
//import codedriver.module.report.dto.ReportSendJobRelationVo;
//import codedriver.module.report.dto.ReportSendJobVo;
//import codedriver.module.report.dto.ReportVo;
//import codedriver.module.report.service.ReportService;
//import codedriver.module.report.util.ExportUtil;
//import codedriver.module.report.util.ReportFreemarkerUtil;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections4.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.commons.mail.HtmlEmail;
//import org.quartz.JobExecutionContext;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.activation.DataHandler;
//import javax.activation.DataSource;
//import javax.mail.internet.MimeBodyPart;
//import javax.mail.internet.MimeUtility;
//import javax.mail.util.ByteArrayDataSource;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 报表发送计划定时器
// */
//@Component
//public class ReportSendJob extends JobBase {
//	static Logger logger = LoggerFactory.getLogger(ReportSendJob.class);
//
//	@Autowired
//	private ReportSendJobMapper reportSendJobMapper;
//
//	@Autowired
//	private ReportMapper reportMapper;
//
//	@Autowired
//	private MailServerMapper mailServerMapper;
//
//	@Autowired
//	private ReportService reportService;
//
//
//	@Override
//	public Boolean checkCronIsExpired(JobObject jobObject) {
//		ReportSendJobVo jobVo = reportSendJobMapper.getJobBaseInfoById(Long.valueOf(jobObject.getJobName()));
//		if (jobVo != null) {
//			if (jobVo.getIsActive().equals(1) && jobVo.getCron().equals(jobObject.getCron())) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	@Override
//	public void reloadJob(JobObject jobObject) {
//		String tenantUuid = jobObject.getTenantUuid();
//		// 切换租户库
//		TenantContext.get().switchTenant(tenantUuid);
//		ReportSendJobVo jobVo = reportSendJobMapper.getJobBaseInfoById(Long.valueOf(jobObject.getJobName()));
//		if (jobVo != null && jobVo.getIsActive().equals(1)) {
//			JobObject newJobObject = new JobObject.Builder(jobVo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(jobVo.getCron()).addData("sendJobId", jobVo.getId()).build();
//			schedulerManager.loadJob(newJobObject);
//		} else {
//			schedulerManager.unloadJob(jobObject);
//		}
//	}
//
//	@Override
//	public void initJob(String tenantUuid) {
//		/** 初始化所有已激活的发送计划 */
//		List<ReportSendJobVo> jobList = reportSendJobMapper.getAllActiveJob();
//		if(CollectionUtils.isNotEmpty(jobList)){
//			for(ReportSendJobVo vo : jobList){
//				JobObject newJobObject = new JobObject.Builder(vo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(vo.getCron()).addData("sendJobId",vo.getId()).build();
//				schedulerManager.loadJob(newJobObject);
//			}
//		}
//	}
//
//	@Override
//	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws Exception {
//		Long id = (Long) jobObject.getData("sendJobId");
//		/** 根据id获取报表*/
//		ReportSendJobVo sendJob = reportSendJobMapper.getJobBaseInfoById(id);
//		List<ReportSendJobRelationVo> relatedReportList = reportSendJobMapper.getRelatedReportById(id);
//		List<InputStream> reportIsList = null;
//		if(CollectionUtils.isNotEmpty(relatedReportList)){
//			reportIsList = new ArrayList<>();
//			for(ReportSendJobRelationVo vo : relatedReportList){
//				ReportVo report = reportMapper.getReportById(vo.getReportId());
//				if(report != null){
//					JSONObject paramObj = JSONObject.parseObject(vo.getCondition());
//					ByteArrayOutputStream os = new ByteArrayOutputStream();
//					Map<String, Long> timeMap = new HashMap<>();
//					Map<String, Object> returnMap = reportService.getQueryResult(report.getId(), paramObj, timeMap, false);
//					Map<String, Object> tmpMap = new HashMap<>();
//					Map<String, Object> commonMap = new HashMap<>();
//					tmpMap.put("report", returnMap);
//					tmpMap.put("param", paramObj);
//					tmpMap.put("common", commonMap);
//					String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, report.getContent());
//					ExportUtil.getWordFileByHtml(content, true, os);
//					InputStream is = new ByteArrayInputStream(os.toByteArray());
//					reportIsList.add(is);
//				}
//			}
//		}
//
//		try {
//			MailServerVo mailServerVo = mailServerMapper.getActiveMailServer();
//			if (mailServerVo != null && StringUtils.isNotBlank(mailServerVo.getHost()) && mailServerVo.getPort() != null) {
//				HtmlEmail se = new HtmlEmail();
//				se.setHostName(mailServerVo.getHost());
//				se.setSmtpPort(mailServerVo.getPort());
//				if (StringUtils.isNotBlank(mailServerVo.getUserName()) && StringUtils.isNotBlank(mailServerVo.getPassword())) {
//					se.setAuthentication(mailServerVo.getUserName(), mailServerVo.getPassword());
//				}
//				if (StringUtils.isNotBlank(mailServerVo.getFromAddress())) {
//					se.setFrom(mailServerVo.getFromAddress(), mailServerVo.getName());
//				}
//
//				se.setSubject(sendJob.getEmailTitle());
//				se.setMsg(sendJob.getEmailContent());
//				InputStream is = new ByteArrayInputStream(new byte[]{});
//				MimeBodyPart messageBodyPart = new MimeBodyPart();
//				DataSource dataSource = new ByteArrayDataSource(is, "application/png");
//				DataHandler dataHandler = new DataHandler(dataSource);
//				messageBodyPart.setDataHandler(dataHandler);
//				messageBodyPart.setFileName(MimeUtility.encodeText("aa.doc"));
//			} else {
//				throw new EmailServerNotFoundException();
//			}
//		} catch (Exception ex) {
//			logger.error(ex.getMessage(), ex);
//		}
//
//	}
//
//	@Override
//	public String getGroupName() {
//		return TenantContext.get().getTenantUuid() + "-REPORT-SEND";
//	}
//
//}
