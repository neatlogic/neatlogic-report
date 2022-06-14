/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.schedule;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.report.exception.ReportSendJobNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.scheduler.dao.mapper.SchedulerMapper;
import codedriver.framework.scheduler.dto.JobAuditVo;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportSendJobMapper;
import codedriver.module.report.dto.ReportSendJobAuditVo;
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
