package codedriver.module.report.api.schedule;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleIllegalParameterException;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportReceiverVo;
import codedriver.module.report.dto.ReportSendJobRelationVo;
import codedriver.module.report.dto.ReportSendJobVo;
import codedriver.module.report.exception.ReportSendJobNameRepeatException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * reportList结构形如：
 * [{"id":1555,"condition":{"beforeDay":{"value":"7&=&过去7天"},"taskStatus":{"value":"aborted&=&已取消"},"keyword":{"value":"a"}}},{"id":23333,"condition":{"beforeDay":{"value":"30&=&过去30天"},"taskStatus":{"value":"hang&=&已挂起"},"change":{"value":["操作系统&=&操作系统","中间件&=&中间件"]}}}]
 */
@Service
@AuthAction(name = "REPORT_MODIFY")
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ReportSendJobSaveApi extends PrivateApiComponentBase {
	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Autowired
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
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, xss = true, desc = "报表发送计划名称"),
			@Param(name = "emailTitle", type = ApiParamType.STRING, isRequired = true, xss = true,desc = "邮件标题"),
			@Param(name = "emailContent", type = ApiParamType.STRING, desc = "邮件正文"),
			@Param(name = "cron", type = ApiParamType.STRING, isRequired = true, desc = "corn表达式"),
			@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, rule = "0,1", desc = "是否激活(0:禁用，1：激活)"),
			@Param(name = "scList", type = ApiParamType.JSONARRAY,isRequired = true, desc = "收件人列表，可填用户UUID或邮箱"),
			@Param(name = "ccList", type = ApiParamType.JSONARRAY,desc = "抄送人列表，可填用户UUID或邮箱"),
			@Param(name = "reportList", type = ApiParamType.JSONARRAY,isRequired = true,desc = "报表列表")
	})
	@Output({ @Param(name = "id", type = ApiParamType.LONG, desc = "计划ID") })
	@Description(desc = "保存报表发送计划")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ReportSendJobVo jobVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>(){});
		if (!CronExpression.isValidExpression(jobVo.getCron())) {
			throw new ScheduleIllegalParameterException(jobVo.getCron());
		}
		if (reportSendJobMapper.checkNameIsRepeat(jobVo) > 0) {
			throw new ReportSendJobNameRepeatException(jobVo.getName());
		}

		JSONArray scArray = jsonObj.getJSONArray("scList");
		JSONArray ccArray = jsonObj.getJSONArray("ccList");
		JSONArray reportList = jsonObj.getJSONArray("reportList");
		/** 读取收件人与抄送人 */
		List<ReportReceiverVo> scVoList = new ArrayList<>();
		List<ReportReceiverVo> ccVoList = new ArrayList<>();
		getScAndCcList(jobVo,scArray,ccArray,scVoList,ccVoList);
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
		if(CollectionUtils.isNotEmpty(scVoList)){
			reportSendJobMapper.batchInsertReportReceiver(scVoList);
		}
		if(CollectionUtils.isNotEmpty(ccVoList)){
			reportSendJobMapper.batchInsertReportReceiver(ccVoList);
		}
		/** 保存关联的报表 */
		if(CollectionUtils.isNotEmpty(relationList)){
			reportSendJobMapper.batchInsertReportRelation(relationList);
		}
		/** 根据isActive启动或清除定时任务 */
		IJob handler = SchedulerManager.getHandler("codedriver.module.report.schedule.plugin.ReportSendJob");
		String tenantUuid = TenantContext.get().getTenantUuid();
		JobObject newJobObject = new JobObject.Builder(jobVo.getId().toString(), handler.getGroupName(), handler.getClassName(), tenantUuid).withCron(jobVo.getCron()).addData("sendJobId",jobVo.getId()).build();
		if(jobVo.getIsActive().intValue() == 1){
			schedulerManager.loadJob(newJobObject);
		}else{
			schedulerManager.unloadJob(newJobObject);
		}

		JSONObject result = new JSONObject();
		result.put("id",jobVo.getId());

		return result;
	}

	/** 读取关联的报表 */
	private List<ReportSendJobRelationVo> getRelations(ReportSendJobVo jobVo, JSONArray reportList) {
		List<ReportSendJobRelationVo> relationList = null;
		if(CollectionUtils.isNotEmpty(reportList)){
			relationList = new ArrayList<>();
			for(Object o : reportList){
				JSONObject report = JSONObject.parseObject(o.toString());
				ReportSendJobRelationVo vo = new ReportSendJobRelationVo();
				vo.setReportSendJobId(jobVo.getId());
				vo.setReportId(report.getLong("id"));
				vo.setCondition(report.getString("condition"));
				relationList.add(vo);
			}
		}
		return relationList;
	}
	/** 读取收件人与抄送人 */
	private void getScAndCcList(ReportSendJobVo jobVo,JSONArray scArray,JSONArray ccArray,List<ReportReceiverVo> scVoList,List<ReportReceiverVo> ccVoList){
		if(CollectionUtils.isNotEmpty(scArray)){
			List<String> scList = scArray.toJavaList(String.class);
			for(String s : scList){
				ReportReceiverVo vo = new ReportReceiverVo();
				vo.setReportSendJobId(jobVo.getId());
				vo.setReceiver(s);
				vo.setType("s");
				scVoList.add(vo);
			}
		}
		if(CollectionUtils.isNotEmpty(ccArray)){
			List<String> ccList = ccArray.toJavaList(String.class);
			for(String s : ccList){
				ReportReceiverVo vo = new ReportReceiverVo();
				vo.setReportSendJobId(jobVo.getId());
				vo.setReceiver(s);
				vo.setType("c");
				ccVoList.add(vo);
			}
		}
	}

}
