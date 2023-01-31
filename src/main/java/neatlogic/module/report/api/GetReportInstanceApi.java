/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.exception.type.PermissionDeniedException;
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
import neatlogic.framework.report.exception.ReportInstanceNotFoundException;
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

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "reportinstance/get";
    }

    @Override
    public String getName() {
        return "获取报表详细信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "报表id", isRequired = true)})
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "获取报表详细信息接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        boolean hasAuth = AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName());
        Long reportInstanceId = jsonObj.getLong("id");
        if (reportInstanceMapper.checkReportInstanceExists(reportInstanceId) == 0) {
            throw new ReportInstanceNotFoundException(reportInstanceId);
        }
        ReportInstanceVo reportInstanceVo = reportInstanceService.getReportInstanceDetailById(reportInstanceId);
        // 如果不是创建人也没有REPORT_MODIFY权限，看是否在授权列表中，不在则无权查看
        String userUuid = UserContext.get().getUserUuid(true);
        if (!hasAuth && !Objects.equals(userUuid, reportInstanceVo.getFcu())) {
            List<String> userRoleList = UserContext.get().getRoleUuidList();
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);
            if (reportInstanceVo.getReportInstanceAuthList() != null) {
                for (ReportInstanceAuthVo auth : reportInstanceVo.getReportInstanceAuthList()) {
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
