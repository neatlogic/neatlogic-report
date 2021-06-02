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
import java.util.List;

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
    @Description(desc = "获取报表列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // todo 左侧报表菜单：查询当前用户创建的或有权限的报表(id:name)
        // todo 如果有REPORT_MODIFY权限，则能看到所有实例
        ReportInstanceVo reportInstanceVo = new ReportInstanceVo();
        List<ReportInstanceVo> instanceList = new ArrayList<>();

        // todo 如果不是管理员，则只筛选激活的报表，除非是自己创建的
        if (!AuthActionChecker.check(REPORT_MODIFY.class.getSimpleName())) {
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
            reportInstanceVo.setIsActive(1);

            List<ReportInstanceVo> authInstanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);

            if (CollectionUtils.isNotEmpty(authInstanceList)) {
                authInstanceList.addAll(authInstanceList);
            }

            // todo 查询自己创建的
            reportInstanceVo.setReportInstanceAuthList(null);
            reportInstanceVo.setIsActive(null);
            reportInstanceVo.setFcu(UserContext.get().getUserUuid());

            List<ReportInstanceVo> ownerInstanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
            if (CollectionUtils.isNotEmpty(ownerInstanceList)) {
                instanceList.addAll(ownerInstanceList);
            }

        } else {
            instanceList = reportInstanceMapper.getReportInstanceList(reportInstanceVo);
        }

        return instanceList;
    }
}
