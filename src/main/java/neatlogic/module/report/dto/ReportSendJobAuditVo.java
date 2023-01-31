package neatlogic.module.report.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.scheduler.dto.JobAuditVo;

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
