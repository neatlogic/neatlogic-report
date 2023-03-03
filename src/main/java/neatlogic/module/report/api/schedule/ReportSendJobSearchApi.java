/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.api.schedule;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.dao.mapper.SchedulerMapper;
import neatlogic.framework.scheduler.dto.JobAuditVo;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportSendJobVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobSearchApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private SchedulerMapper schedulerMapper;

    @Override
    public String getToken() {
        return "report/sendjob/search";
    }

    @Override
    public String getName() {
        return "查询报表发送计划";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword",
                    type = ApiParamType.STRING,
                    desc = "关键词",
                    xss = true),
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
    @Output({
            @Param(name = "jobList",
                    type = ApiParamType.JSONARRAY,
                    explode = ReportSendJobVo[].class,
                    desc = "发送计划列表"),
            @Param(explode = BaseEditorVo.class)
    })
    @Description(desc = "查询报表发送计划")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportSendJobVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ReportSendJobVo>() {
        });
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = reportSendJobMapper.searchJobCount(vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<ReportSendJobVo> jobList = reportSendJobMapper.searchJob(vo);
        /* 查询发送次数与收件人 */
        if (CollectionUtils.isNotEmpty(jobList)) {
            List<ReportSendJobVo> toList = reportSendJobMapper.getReportToList(jobList.stream().map(ReportSendJobVo::getId).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(toList)) {
                for (ReportSendJobVo job : jobList) {
                    for (ReportSendJobVo to : toList) {
                        JobAuditVo jobAuditVo = new JobAuditVo();
                        jobAuditVo.setJobUuid(job.getId().toString());
                        job.setExecCount(schedulerMapper.searchJobAuditCount(jobAuditVo));
                        if (Objects.equals(job.getId(), to.getId())) {
                            job.setToNameList(to.getToNameList());
                            break;
                        }
                    }
                }
            }
        }
        returnObj.put("jobList", jobList);
        return returnObj;
    }
}
