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
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.report.exception.ReportInstanceNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dto.ReportInstanceAuthVo;
import neatlogic.module.report.dto.ReportInstanceVo;
import neatlogic.module.report.dto.ReportParamVo;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportInstanceService;
import neatlogic.module.report.service.ReportService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportInstanceApi extends PrivateApiComponentBase {

    @Resource
    private ReportInstanceService reportInstanceService;

    @Resource
    private ReportService reportService;

    @Resource
    private ReportInstanceMapper reportInstanceMapper;

    @Override
    public String getToken() {
        return "reportinstance/get";
    }

    @Override
    public String getName() {
        return "nmra.getreportinstanceapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)})
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "nmra.getreportinstanceapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());
        Long reportInstanceId = jsonObj.getLong("id");
        if (reportInstanceMapper.checkReportInstanceExists(reportInstanceId) == 0) {
            throw new ReportInstanceNotFoundEditTargetException(reportInstanceId);
        }
        ReportInstanceVo reportInstanceVo = reportInstanceService.getReportInstanceDetailById(reportInstanceId);
        // 如果不是创建人也没有REPORT_MODIFY权限，看是否在授权列表中，不在则无权查看
        String userUuid = UserContext.get().getUserUuid(true);
        if (!hasAuth && !Objects.equals(userUuid, reportInstanceVo.getFcu())) {
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            List<String> userRoleList = authenticationInfoVo.getRoleUuidList();
            List<String> teamUuidList = authenticationInfoVo.getTeamUuidList();
            if (reportInstanceVo.getReportInstanceAuthList() != null) {
                for (ReportInstanceAuthVo auth : reportInstanceVo.getReportInstanceAuthList()) {
                    if (auth.getAuthType().equals(GroupSearch.COMMON.getValue())) {
                        if (auth.getAuthUuid().equals(UserType.ALL.getValue())) {
                            hasAuth = true;
                            break;
                        }
                    }
                    if (auth.getAuthType().equals(ReportInstanceAuthVo.AUTHTYPE_USER)) {
                        if (auth.getAuthUuid().equals(userUuid)) {
                            hasAuth = true;
                            break;
                        }
                    } else if (auth.getAuthType().equals(ReportInstanceAuthVo.AUTHTYPE_ROLE)) {
                        if (userRoleList.contains(auth.getAuthUuid())) {
                            hasAuth = true;
                            break;
                        }
                    } else if (auth.getAuthType().equals(ReportInstanceAuthVo.AUTHTYPE_TEAM)) {
                        if (teamUuidList.contains(auth.getAuthUuid())) {
                            hasAuth = true;
                            break;
                        }
                    }
                }
            }
            if (!hasAuth) {
                throw new PermissionDeniedException();
            }
        }
        if (reportInstanceVo.getReportId() != null) {
            ReportVo reportVo = reportService.getReportDetailById(reportInstanceVo.getReportId());
            List<ReportParamVo> paramList = reportVo.getParamList();
            JSONObject paramObj = null;
            if (reportInstanceVo.getConfig() != null) {
                paramObj = reportInstanceVo.getConfig().getJSONObject("param");
            }
            if (CollectionUtils.isNotEmpty(paramList) && paramObj != null) {
                Iterator<ReportParamVo> it = paramList.iterator();
                while (it.hasNext()) {
                    ReportParamVo param = it.next();
                    if (paramObj.containsKey(param.getName())) {
                        JSONObject newObj = paramObj.getJSONObject(param.getName());
                        if (newObj != null) {
                            if (param.getConfig() != null) {
                                param.getConfig().put("defaultValue", newObj.getString("defaultValue"));
                            } else {
                                param.setConfig(newObj.toJSONString());
                            }
                        }
                    } else {
                        it.remove();
                    }
                }
            }
            reportInstanceVo.setParamList(paramList);
        }
        return reportInstanceVo;
    }
}
