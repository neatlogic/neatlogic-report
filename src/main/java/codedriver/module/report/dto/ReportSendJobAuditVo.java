package codedriver.module.report.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.framework.scheduler.dto.JobAuditVo;

import java.util.List;

public class ReportSendJobAuditVo extends JobAuditVo {

	@EntityField(name = "收件人列表", type = ApiParamType.JSONARRAY)
	private List<ReportReceiverVo> receiverList;

	public List<ReportReceiverVo> getReceiverList() {
		return receiverList;
	}

	public void setReceiverList(List<ReportReceiverVo> receiverList) {
		this.receiverList = receiverList;
	}
}
