/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportHasBeenQuotedByJobException;
import neatlogic.framework.report.exception.ReportHasInstanceException;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
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
		return "nmra.deletereportapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id") })
	@Output({})
	@Description(desc = "nmra.deletereportapi.getname")
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
