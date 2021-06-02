/*
 * Copyright(c) 2021. TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceAuthVo;
import codedriver.module.report.dto.ReportInstanceVo;
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
    private TeamMapper teamMapper;

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
        // 不分页查询当前用户可看的、激活的所有报表实例，只保留id、name字段
        ReportInstanceVo reportInstanceVo = new ReportInstanceVo();
        reportInstanceVo.setIsActive(1);
        List<ReportInstanceVo> instanceList = new ArrayList<>();
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())) {
            // 查询当前用户有权看到的实例
            String userUuid = UserContext.get().getUserUuid(true);
            List<ReportInstanceAuthVo> reportAuthList = new ArrayList<>();
            reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_USER, userUuid));
            for (String roleUuid : UserContext.get().getRoleUuidList()) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_ROLE, roleUuid));
            }
            for (String teamUuid : teamMapper.getTeamUuidListByUserUuid(userUuid)) {
                reportAuthList.add(new ReportInstanceAuthVo(ReportInstanceAuthVo.AUTHTYPE_TEAM, teamUuid));
            }
            reportInstanceVo.setReportInstanceAuthList(reportAuthList);
            List<ReportInstanceVo> authInstanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
            if (CollectionUtils.isNotEmpty(authInstanceList)) {
                instanceList.addAll(authInstanceList);
            }

            // 查询当前用户创建的实例
            reportInstanceVo.setReportInstanceAuthList(null);
            reportInstanceVo.setFcu(UserContext.get().getUserUuid());
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
