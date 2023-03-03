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

package neatlogic.module.report.api;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportInstanceAuthVo;
import neatlogic.module.report.dto.ReportInstanceTableColumnVo;
import neatlogic.module.report.dto.ReportInstanceVo;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.framework.report.exception.ReportInstanceNotFoundException;
import neatlogic.framework.report.exception.ReportIsNotActiveException;
import neatlogic.framework.report.exception.ReportNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
@Service
@Transactional
public class SaveReportInstanceApi extends PrivateApiComponentBase {

    @Resource
    private ReportInstanceMapper reportInstanceMapper;

    @Resource
    private ReportMapper reportMapper;

    @Override
    public String getToken() {
        return "reportinstance/save";
    }

    @Override
    public String getName() {
        return "保存报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "报表名称", isRequired = true, xss = true),
            @Param(name = "reportId", type = ApiParamType.LONG, desc = "报表定义id", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "报表授权列表"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "报表参数配置"),
            @Param(name = "tableConfig", type = ApiParamType.JSONARRAY, desc = "表格字段配置")
    })
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "保存报表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        ReportInstanceVo reportInstanceVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);

        ReportVo report = reportMapper.getReportBaseInfo(reportInstanceVo.getReportId());
        if (report == null) {
            throw new ReportNotFoundException(reportInstanceVo.getReportId());
        }
        if (!Objects.equals(report.getIsActive(), 1)) {
            throw new ReportIsNotActiveException(report.getName());
        }

        /** 读取表格配置 */
        JSONArray tableConfig = jsonObj.getJSONArray("tableConfig");
        List<ReportInstanceTableColumnVo> tableColumnList = getTableColumnVoList(reportInstanceVo.getId(), tableConfig);

        reportInstanceVo.setLcu(UserContext.get().getUserUuid());
        if (jsonObj.getLong("id") != null) {
            ReportInstanceVo instance = reportInstanceMapper.getReportInstanceById(reportInstanceVo.getId());
            if (instance == null) {
                throw new ReportInstanceNotFoundException(reportInstanceVo.getId());
            }
            if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())
                    && !Objects.equals(UserContext.get().getUserUuid(), instance.getFcu())) {
                throw new PermissionDeniedException(REPORT_MODIFY.class);
            }
            reportInstanceMapper.deleteReportInstanceAuthByReportInstanceId(reportInstanceVo.getId());
            reportInstanceMapper.deleteReportInstanceTableColumn(reportInstanceVo.getId());
            reportInstanceMapper.updateReportInstance(reportInstanceVo);
        } else {
            reportInstanceVo.setFcu(UserContext.get().getUserUuid());
            reportInstanceMapper.insertReportInstance(reportInstanceVo);
        }
        if (CollectionUtils.isNotEmpty(tableColumnList)) {
            reportInstanceMapper.batchInsertReportInstanceTableColumn(tableColumnList);
        }
        if (CollectionUtils.isNotEmpty(reportInstanceVo.getAuthList())) {
            for (String auth : reportInstanceVo.getAuthList()) {
                ReportInstanceAuthVo reportInstanceAuthVo = new ReportInstanceAuthVo(reportInstanceVo.getId(), auth.split("#")[0], auth.split("#")[1]);
                reportInstanceMapper.insertReportInstanceAuth(reportInstanceAuthVo);
            }
        }
        return reportInstanceVo.getId();
    }

    private List<ReportInstanceTableColumnVo> getTableColumnVoList(Long reportInstanceId, JSONArray tableConfig) {
        List<ReportInstanceTableColumnVo> tableColumnList = null;
        if (CollectionUtils.isNotEmpty(tableConfig)) {
            tableColumnList = new ArrayList<>();
            for (Object o : tableConfig) {
                JSONObject obj = JSONObject.parseObject(o.toString());
                String id = obj.getString("tableId");
                JSONArray columnList = obj.getJSONArray("columnList");
                if (CollectionUtils.isNotEmpty(columnList)) {
                    for (int i = 0; i < columnList.size(); i++) {
                        ReportInstanceTableColumnVo vo = new ReportInstanceTableColumnVo();
                        vo.setReportInstanceId(reportInstanceId);
                        vo.setTableId(id);
                        vo.setColumn(columnList.get(i).toString());
                        vo.setSort(i);
                        tableColumnList.add(vo);
                    }
                }
            }
        }
        return tableColumnList;
    }
}
