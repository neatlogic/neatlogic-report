/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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

    @Override
    public String getToken() {
        return "report/list";
    }

    @Override
    public String getName() {
        return "nmra.reportlistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "common.defaultvalue"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "common.tbodylist", explode = ReportVo[].class)})
    @Description(desc = "nmra.reportlistapi.description.desc")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        reportVo.setIsActive(1);
        /*
        目前该接口只有前端报表管理中的添加编辑报表和搜索框左边选择模板的过滤条件用到，
        因为进入报表管理页就需要报表管理权限（REPORT_MODIFY），所以调用该接口的用户一定拥有报表管理权限（REPORT_MODIFY），
        下面的if语句将不会成立，在创建报表模板时设置的使用授权数据在这里没有用上，目前也没有发现其他地方用到。
         */
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())) {
            String userUuid = UserContext.get().getUserUuid();
            List<ReportAuthVo> reportAuthList = new ArrayList<>();
            reportAuthList.add(new ReportAuthVo(GroupSearch.COMMON.getValue(), UserType.ALL.getValue()));
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();;
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
