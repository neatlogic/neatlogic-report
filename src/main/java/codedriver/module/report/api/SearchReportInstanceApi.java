package codedriver.module.report.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceVo;

@Service
public class SearchReportInstanceApi extends ApiComponentBase {

    @Autowired
    private ReportInstanceMapper reportInstanceMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "reportinstance/search";
    }

    @Override
    public String getName() {
        return "查询报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),})
    @Output({@Param(explode = BasePageVo.class),
        @Param(name = "tbodyList", desc = "报表列表", explode = ReportInstanceVo[].class)})
    @Description(desc = "查询报表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportInstanceVo reportInstanceVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);
        String userUuid = UserContext.get().getUserUuid(true);
        // 权限判断：如果是管理员
        boolean hasAuth = AuthActionChecker.check("REPORT_MODIFY");
        if (!hasAuth) {
            // 如果不是管理员，则校验报表权限
            reportInstanceVo.setSearchMode("user");
            List<ReportInstanceAuthVo> reportAuthList = new ArrayList<>();
            reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_USER, userUuid));
            for (String roleUuid : UserContext.get().getRoleUuidList()) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_ROLE, roleUuid));
            }
            for (String teamUuid : teamMapper.getTeamUuidListByUserUuid(userUuid)) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_TEAM, teamUuid));
            }
            reportInstanceVo.setReportInstanceAuthList(reportAuthList);
        } else {
            reportInstanceVo.setSearchMode("admin");
        }

        List<ReportInstanceVo> reportInstanceList = reportInstanceMapper.searchReportInstance(reportInstanceVo);
        JSONObject returnObj = new JSONObject();
        returnObj.put("tbodyList", reportInstanceList);
        if (reportInstanceList.size() > 0 && reportInstanceVo.getNeedPage()) {
            int rowNum = reportInstanceMapper.searchReportInstanceCount(reportInstanceVo);
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageSize", reportInstanceVo.getPageSize());
            returnObj.put("currentPage", reportInstanceVo.getCurrentPage());
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, reportInstanceVo.getPageSize()));
        }

        return returnObj;
    }
}
