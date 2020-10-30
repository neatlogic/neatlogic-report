package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.dto.BaseEditorVo;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.util.SnowflakeUtil;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportSendJobVo extends BaseEditorVo {
	@EntityField(name = "id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "名称", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "邮件标题", type = ApiParamType.STRING)
	private String emailTitle;
	@EntityField(name = "邮件内容", type = ApiParamType.STRING)
	private String emailContent;
	@EntityField(name = "cron表达式", type = ApiParamType.STRING)
	private String cron;
	@EntityField(name = "是否激活", type = ApiParamType.INTEGER)
	private Integer isActive;

	@EntityField(name = "下次发送时间")
	private Date nextFireTime;
	@EntityField(name = "发送次数", type = ApiParamType.INTEGER)
	private Integer execCount;
	@EntityField(name = "邮件接收人列表", type = ApiParamType.JSONARRAY)
	@JSONField(serialize = false)
	private List<ReportReceiverVo> receiverList;
	@EntityField(name = "收件人列表", type = ApiParamType.JSONARRAY)
	private List<String> scList;
	@EntityField(name = "抄送人列表", type = ApiParamType.JSONARRAY)
	private List<String> ccList;
	@EntityField(name = "收件人用户名或邮箱列表", type = ApiParamType.JSONARRAY)
	private List<String> scNameList;
	@EntityField(name = "报表列表", type = ApiParamType.JSONARRAY)
	private List<ReportSendJobRelationVo> reportList;

	public Long getId() {
		if (id == null) {
			id = SnowflakeUtil.uniqueLong();
		}
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailTitle() {
		return emailTitle;
	}

	public void setEmailTitle(String emailTitle) {
		this.emailTitle = emailTitle;
	}

	public String getEmailContent() {
		return emailContent;
	}

	public void setEmailContent(String emailContent) {
		this.emailContent = emailContent;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Integer getExecCount() {
		return execCount;
	}

	public void setExecCount(Integer execCount) {
		this.execCount = execCount;
	}

	public List<ReportReceiverVo> getReceiverList() {
		return receiverList;
	}

	public void setReceiverList(List<ReportReceiverVo> receiverList) {
		this.receiverList = receiverList;
	}

	public List<String> getScList() {
		if(CollectionUtils.isEmpty(scList) && CollectionUtils.isNotEmpty(receiverList)){
			scList = new ArrayList<>();
			for(ReportReceiverVo vo : receiverList) {
				if("s".equals(vo.getType())){
					if(!vo.getReceiver().contains("@")){
						vo.setReceiver(GroupSearch.USER.getValuePlugin() + vo.getReceiver());
					}
					scList.add(vo.getReceiver());
				}
			}
		}
		return scList;
	}

	public void setScList(List<String> scList) {
		this.scList = scList;
	}

	public List<String> getCcList() {
		if(CollectionUtils.isEmpty(ccList) && CollectionUtils.isNotEmpty(receiverList)){
			ccList = new ArrayList<>();
			for(ReportReceiverVo vo : receiverList) {
				if("c".equals(vo.getType())){
					if(!vo.getReceiver().contains("@")){
						vo.setReceiver(GroupSearch.USER.getValuePlugin() + vo.getReceiver());
					}
					ccList.add(vo.getReceiver());
				}
			}
		}
		return ccList;
	}

	public void setCcList(List<String> ccList) {
		this.ccList = ccList;
	}

	public List<String> getScNameList() {
		return scNameList;
	}

	public void setScNameList(List<String> scNameList) {
		this.scNameList = scNameList;
	}

	public List<ReportSendJobRelationVo> getReportList() {
		return reportList;
	}

	public void setReportList(List<ReportSendJobRelationVo> reportList) {
		this.reportList = reportList;
	}
}
