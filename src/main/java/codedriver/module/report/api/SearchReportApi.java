/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class SearchReportApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

    @Override
    public String getToken() {
        return "report/search";
    }

    @Override
    public String getName() {
        return "查询报表定义";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "是否激活"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "类型"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "报表定义列表", explode = ReportVo[].class)})
    @Description(desc = "查询报表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
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
