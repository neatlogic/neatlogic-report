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
