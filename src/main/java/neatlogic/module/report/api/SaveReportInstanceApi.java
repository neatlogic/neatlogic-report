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
        if (CollectionUtils.isNotEmpty(reportInstanceVo.getReportInstanceAuthList())) {
            reportInstanceMapper.insertReportInstanceAuthList(reportInstanceVo.getReportInstanceAuthList());
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
