//package codedriver.module.report.schedule.plugin;
//
//import codedriver.framework.asynchronization.threadlocal.TenantContext;
//import codedriver.framework.dao.mapper.MailServerMapper;
//import codedriver.framework.dao.mapper.UserMapper;
//import codedriver.framework.dto.MailServerVo;
//import codedriver.framework.dto.UserVo;
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
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.activation.DataHandler;
//import javax.activation.DataSource;
//import javax.mail.*;
//import javax.mail.internet.*;
//import javax.mail.util.ByteArrayDataSource;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
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
//    @Autowired
//    private UserMapper userMapper;
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
//	public void executeInternal(JobExecutionContext context, JobObject jobObject) throws JobExecutionException {
//		Long id = (Long) jobObject.getData("sendJobId");
//		/** 根据id获取报表*/
////		ReportSendJobVo sendJob = reportSendJobMapper.getJobBaseInfoById(id);
//        ReportSendJobVo sendJob = reportSendJobMapper.getJobById(id);
//        List<ReportSendJobRelationVo> relatedReportList = sendJob.getReportList();
////        List<ReportSendJobRelationVo> relatedReportList = reportSendJobMapper.getRelatedReportById(id);
////		List<InputStream> reportIsList = null;
//		Map<String,InputStream> reportMap = new HashMap<>();
//		if(CollectionUtils.isNotEmpty(relatedReportList)){
////			reportIsList = new ArrayList<>();
//			for(ReportSendJobRelationVo vo : relatedReportList){
//				ReportVo report = reportMapper.getReportById(vo.getReportId());
//				if(report != null){
//					JSONObject paramObj = JSONObject.parseObject(vo.getCondition());
//					ByteArrayOutputStream os = new ByteArrayOutputStream();
//					Map<String, Long> timeMap = new HashMap<>();
//                    Map<String, Object> returnMap = null;
//                    try {
//                        returnMap = reportService.getQueryResult(report.getId(), paramObj, timeMap, false);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    Map<String, Object> tmpMap = new HashMap<>();
//					Map<String, Object> commonMap = new HashMap<>();
//					tmpMap.put("report", returnMap);
//					tmpMap.put("param", paramObj);
//					tmpMap.put("common", commonMap);
//                    String content = null;
//                    try {
//                        content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, report.getContent());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        ExportUtil.getWordFileByHtml(content, true, os);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    InputStream is = new ByteArrayInputStream(os.toByteArray());
////					reportIsList.add(is);
//					reportMap.put(report.getName(),is);
//				}
//			}
//		}
//
//		/** 获取收件人与抄送人 */
//        List<String> scList = sendJob.getScList();
//        List<String> ccList = sendJob.getCcList();
//        List<String> scEmailList = new ArrayList<>();
//        List<String> ccEmailList = new ArrayList<>();
//        for(String s : scList){
//            if(!s.contains("@")){
//                UserVo user = userMapper.getUserBaseInfoByUuid(s);
//                scEmailList.add(user.getEmail());
//            }else{
//                scEmailList.add(s);
//            }
//        }
//        for(String s : ccList){
//            if(!s.contains("@")){
//                UserVo user = userMapper.getUserBaseInfoByUuid(s);
//                ccEmailList.add(user.getEmail());
//            }else{
//                ccEmailList.add(s);
//            }
//        }
//
//        String sc = String.join(",",scEmailList);
//        String cc = String.join(",",ccEmailList);
//
//        try {
//			MailServerVo mailServerVo = mailServerMapper.getActiveMailServer();
//			if (mailServerVo != null && StringUtils.isNotBlank(mailServerVo.getHost()) && mailServerVo.getPort() != null) {
//                Properties props = new Properties();
//                props.setProperty("mail.smtp.host", mailServerVo.getHost());
//                props.setProperty("mail.smtp.port", mailServerVo.getPort().toString());
//                props.put("mail.smtp.auth", "true");
//                Session session = Session.getInstance(props, new Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(mailServerVo.getUserName(), mailServerVo.getPassword());
//                    }
//                });
//
//                MimeMessage msg = new MimeMessage(session);
//                msg.setFrom(new InternetAddress(mailServerVo.getFromAddress(), mailServerVo.getName()));
//                /** 设置收件人 */
//                msg.setRecipients(Message.RecipientType.TO,
//                        InternetAddress.parse(sc, false));
//                /** 设置抄送人 */
//                msg.setRecipients(Message.RecipientType.CC,
//                        InternetAddress.parse(cc, false));
//                /** 设置邮件标题 */
//                msg.setSubject(sendJob.getEmailTitle());
//                msg.setSentDate(new Date());
//
//                MimeMultipart multipart = new MimeMultipart("mixed");
//
//                MimeBodyPart text = new MimeBodyPart();
//                text.setContent(sendJob.getEmailContent(),"text/plain;charset=UTF-8");
//                multipart.addBodyPart(text);
//
//                MimeBodyPart messageBodyPart = new MimeBodyPart();
//
//                for(Map.Entry<String,InputStream> entry : reportMap.entrySet()){
//                    DataSource dataSource = new ByteArrayDataSource(entry.getValue(), "application/pdf");
//                    DataHandler dataHandler = new DataHandler(dataSource);
//                    messageBodyPart.setDataHandler(dataHandler);
//                    messageBodyPart.setFileName(MimeUtility.encodeText(entry.getKey() + ".pdf"));
//                    multipart.addBodyPart(messageBodyPart);
//                }
//                msg.setContent(multipart);
//                msg.saveChanges();
//
//                Transport.send(msg);
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
