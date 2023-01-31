/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportAuthVo;
import neatlogic.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ReportListApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "report/list";
    }

    @Override
    public String getName() {
        return "获取报表定义列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "用于回显的报表ID列表"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "报表定义列表", explode = ReportVo[].class)})
    @Description(desc = "获取报表定义列表(用于报表实例编辑面板的模版选择下拉框)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        reportVo.setIsActive(1);
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())) {
            String userUuid = UserContext.get().getUserUuid();
            List<ReportAuthVo> reportAuthList = new ArrayList<>();
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
            reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_USER, userUuid));
            for (String roleUuid : authenticationInfoVo.getRoleUuidList()) {
                reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_ROLE, roleUuid));
            }
            for (String teamUuid : authenticationInfoVo.getTeamUuidList()) {
                reportAuthList.add(new ReportAuthVo(ReportAuthVo.AUTHTYPE_TEAM, teamUuid));
            }
            reportVo.setReportAuthList(reportAuthList);
        }
        List<ReportVo> reportList = reportMapper.searchReport(reportVo);
        JSONObject returnObj = new JSONObject();
        returnObj.put("tbodyList", reportList);
        if (reportList.size() > 0 && reportVo.getNeedPage()) {
            int rowNum = reportMapper.searchReportCount(reportVo);
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageSize", reportVo.getPageSize());
            returnObj.put("currentPage", reportVo.getCurrentPage());
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, reportVo.getPageSize()));
        }

        return returnObj;
    }
}
