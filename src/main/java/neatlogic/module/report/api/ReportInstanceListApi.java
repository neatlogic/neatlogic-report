/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dto.ReportAuthVo;
import neatlogic.module.report.dto.ReportInstanceAuthVo;
import neatlogic.module.report.dto.ReportInstanceVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ReportInstanceListApi extends PrivateApiComponentBase {

    @Resource
    private ReportInstanceMapper reportInstanceMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "reportinstance/list";
    }

    @Override
    public String getName() {
        return "获取报表列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "Return", desc = "报表列表", explode = ReportInstanceVo[].class)})
    @Description(desc = "获取报表列表(用于左侧报表实例菜单)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // 不分页查询当前用户有权限且激活的所有报表实例，只保留id、name字段
        ReportInstanceVo reportInstanceVo = new ReportInstanceVo();
        reportInstanceVo.setIsActive(1);
        List<ReportInstanceVo> instanceList = new ArrayList<>();
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())) {
            // 查询当前用户有权看到的实例
            String userUuid = UserContext.get().getUserUuid(true);
            List<ReportInstanceAuthVo> reportAuthList = new ArrayList<>();
            reportAuthList.add(new ReportInstanceAuthVo(GroupSearch.COMMON.getValue(), UserType.ALL.getValue()));
            AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
            reportAuthList.add(new ReportInstanceAuthVo(ReportAuthVo.AUTHTYPE_USER, userUuid));
            for (String roleUuid : authenticationInfoVo.getRoleUuidList()) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportAuthVo.AUTHTYPE_ROLE, roleUuid));
            }
            for (String teamUuid : authenticationInfoVo.getTeamUuidList()) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportAuthVo.AUTHTYPE_TEAM, teamUuid));
            }
            reportInstanceVo.setReportInstanceAuthList(reportAuthList);
            List<ReportInstanceVo> authInstanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
            if (CollectionUtils.isNotEmpty(authInstanceList)) {
                instanceList.addAll(authInstanceList);
            }

            // 查询当前用户创建的实例
            reportInstanceVo.setReportInstanceAuthList(null);
            reportInstanceVo.setSearchByFcu(1);
            List<ReportInstanceVo> ownerInstanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
            if (CollectionUtils.isNotEmpty(ownerInstanceList)) {
                instanceList.addAll(ownerInstanceList);
            }
            if (CollectionUtils.isNotEmpty(instanceList)) {
                instanceList = instanceList.stream().distinct()
                        .sorted(Comparator.comparing(ReportInstanceVo::getId).reversed()).collect(Collectors.toList());
            }
        } else {
            // REPORT_MODIFY权限可查询所有实例
            instanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
        }

        return instanceList;
    }
}
