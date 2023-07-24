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
import neatlogic.framework.report.exception.ReportSendJobNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dao.mapper.ReportSendJobMapper;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportSendJobRelationVo;
import neatlogic.module.report.dto.ReportSendJobVo;
import neatlogic.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ReportSendJobGetApi extends PrivateApiComponentBase {
    @Resource
    private ReportSendJobMapper reportSendJobMapper;

    @Resource
    private ReportMapper reportMapper;

    @Override
    public String getToken() {
        return "report/sendjob/get";
    }

    @Override
    public String getName() {
        return "nmras.reportsendjobgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")})
    @Output({@Param(name = "job", explode = ReportSendJobVo.class, desc = "term.report.sendplaninfo")})
    @Description(desc = "nmras.reportsendjobgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        if (reportSendJobMapper.checkJobExists(id) == 0) {
            throw new ReportSendJobNotFoundEditTargetException(id);
        }
        ReportSendJobVo job = reportSendJobMapper.getJobById(id);
        /** 获取报表条件控件回显值 */
        List<ReportSendJobRelationVo> reportRelationList = job.getReportRelationList();
        List<ReportVo> reportList = null;
        if (CollectionUtils.isNotEmpty(job.getReportList())) {
            job.getReportList().sort(Comparator.comparing(ReportVo::getSort));
            reportList = job.getReportList();
        }
        if (CollectionUtils.isNotEmpty(reportRelationList) && CollectionUtils.isNotEmpty(reportList)) {
            Map<Long, String> configMap = new HashMap<>();
            for (ReportSendJobRelationVo vo : reportRelationList) {
                configMap.put(vo.getReportId(), vo.getConfig());
            }
            for (ReportVo vo : reportList) {
                List<ReportParamVo> paramList = reportMapper.getReportParamByReportId(vo.getId());
                String config = configMap.get(vo.getId());
                if (CollectionUtils.isNotEmpty(paramList) && StringUtils.isNotBlank(config)) {
                    JSONObject paramObj = JSONObject.parseObject(config);
                    Iterator<ReportParamVo> it = paramList.iterator();
                    while (it.hasNext()) {
                        ReportParamVo param = it.next();
                        if (paramObj.containsKey(param.getName())) {
                            Object value = paramObj.get(param.getName());
                            if (param.getConfig() != null && value != null) {
                                param.getConfig().put("defaultValue", value);
                            }
                        }
                    }
                }
                vo.setParamList(paramList);
            }
        }

        JSONObject result = new JSONObject();
        result.put("job", job);
        return result;
    }
}
