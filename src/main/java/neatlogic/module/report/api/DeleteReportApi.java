/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.framework.report.exception.ReportHasBeenQuotedByJobException;
import neatlogic.framework.report.exception.ReportHasInstanceException;
import neatlogic.framework.report.exception.ReportNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.dto.ReportVo;
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
