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

package neatlogic.module.report.api.schedule;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.report.exception.ReportSendJobNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportSendJobAuditVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobAuditSearchApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "report/sendjob/audit/search";
    }

    @Override
    public String getName() {
        return "获取报表发送记录列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "jobUuid",
                    type = ApiParamType.STRING,
                    desc = "发送计划ID",
                    isRequired = true),
            @Param(name = "currentPage",
                    type = ApiParamType.INTEGER,
                    desc = "当前页"),
            @Param(name = "pageSize",
                    type = ApiParamType.INTEGER,
                    desc = "每页数据条目"),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否需要分页，默认true")
    })
    @Output({@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, explode = ReportSendJobAuditVo[].class, desc = "发送记录列表")})
    @Description(desc = "获取报表发送记录列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JobAuditVo vo = JSONObject.toJavaObject(jsonObj, JobAuditVo.class);
        if (reportSendJobMapper.checkJobExists(Long.valueOf(vo.getJobUuid())) == 0) {
            throw new ReportSendJobNotFoundException(Long.valueOf(vo.getJobUuid()));
        }
        int rowNum = schedulerMapper.searchJobAuditCount(vo);
        int pageCount = PageUtil.getPageCount(rowNum, vo.getPageSize());
        vo.setPageCount(pageCount);
        vo.setRowNum(rowNum);
        List<ReportSendJobAuditVo> jobAuditList = reportSendJobMapper.searchReportSendJobAudit(vo);

        JSONObject resultObj = new JSONObject();
        resultObj.put("tbodyList", jobAuditList);
        resultObj.put("currentPage", vo.getCurrentPage());
        resultObj.put("pageSize", vo.getPageSize());
        resultObj.put("pageCount", vo.getPageCount());
        resultObj.put("rowNum", vo.getRowNum());
        return resultObj;
    }
}
