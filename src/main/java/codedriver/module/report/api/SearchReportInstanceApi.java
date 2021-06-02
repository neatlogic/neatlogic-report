package codedriver.module.report.api;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportInstanceMapper;
import codedriver.module.report.dto.ReportInstanceVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class SearchReportInstanceApi extends PrivateApiComponentBase {

    @Resource
    private ReportInstanceMapper reportInstanceMapper;

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
        // 查询当前用户创建的实例，如果有REPORT_MODIFY权限，则查询所有
        ReportInstanceVo reportInstanceVo = JSONObject.toJavaObject(jsonObj, ReportInstanceVo.class);
        Boolean hasAuth = AuthActionChecker.checkByUserUuid(REPORT_MODIFY.class.getSimpleName());
        if (!hasAuth) {
            reportInstanceVo.setFcu(UserContext.get().getUserUuid());
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
