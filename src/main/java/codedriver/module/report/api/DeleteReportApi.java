/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.exception.ReportHasBeenQuotedByJobException;
import codedriver.module.report.exception.ReportHasInstanceException;
import codedriver.module.report.exception.ReportNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dto.ReportVo;
import org.springframework.transaction.annotation.Transactional;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
@Service
@Transactional
public class DeleteReportApi extends PrivateApiComponentBase {

	@Autowired
	private ReportMapper reportMapper;

	@Autowired
	private ReportInstanceMapper reportInstanceMapper;

	@Autowired
	private ReportSendJobMapper reportSendJobMapper;

	@Override
	public String getToken() {
		return "report/delete";
	}

	@Override
	public String getName() {
		return "删除报表";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "报表id") })
	@Output({})
	@Description(desc = "删除报表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long reportId = jsonObj.getLong("id");
		ReportVo report = reportMapper.getReportById(reportId);
		if(report == null){
			throw new ReportNotFoundException(reportId);
		}
		/* 检查是否被报表实例引用 **/
		if(reportInstanceMapper.checkReportInstanceExistsByReportId(reportId) > 0){
			throw new ReportHasInstanceException(report.getName());
		}
		/* 检查是否被报表发送计划引用 **/
		if(reportSendJobMapper.checkJobExistsByReportId(reportId) > 0){
			throw new ReportHasBeenQuotedByJobException(report.getName());
		}
		reportMapper.deleteReportAuthByReportId(reportId);
		reportMapper.deleteReportParamByReportId(reportId);
		reportMapper.deleteReportById(reportId);
		return null;
	}
}
