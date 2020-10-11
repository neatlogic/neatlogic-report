package codedriver.module.report.api;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceVo;
import codedriver.module.report.dto.ReportParamVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportInstanceService;
import codedriver.module.report.service.ReportService;

@Service
public class GetReportInstanceApi extends PrivateApiComponentBase {

    @Autowired
    private ReportInstanceService reportInstanceService;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private ReportService reportService;

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
        // 权限判断：如果是管理员
        boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
        Long reportInstanceId = jsonObj.getLong("id");
        ReportInstanceVo reportInstanceVo = reportInstanceService.getReportInstanceDetailById(reportInstanceId);

        if (!hasAuth) {
            String userUuid = UserContext.get().getUserUuid(true);
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
        }
        if (!hasAuth) {
            throw new PermissionDeniedException();
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
