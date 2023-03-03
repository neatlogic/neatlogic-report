package neatlogic.module.report.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;

public class ReportReceiverVo {
	@EntityField(name = "报表发送计划ID", type = ApiParamType.LONG)
	private Long reportSendJobId;
	@EntityField(name = "接收人UUID或邮箱地址", type = ApiParamType.STRING)
	private String receiver;
	@EntityField(name = "接收人类型(s:收件人；c:抄送人)", type = ApiParamType.STRING)
	private String type;

	public Long getReportSendJobId() {
		return reportSendJobId;
	}

	public void setReportSendJobId(Long reportSendJobId) {
		this.reportSendJobId = reportSendJobId;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
