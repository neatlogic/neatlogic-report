package neatlogic.module.report.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.service.ReportSqldefineService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class SearchReportSqlTableApi extends PrivateApiComponentBase {

    @Resource
    private ReportSqldefineService reportSqldefineService;

    @Override
    public String getToken() {
        return "report/sqldefine/table/search";
    }

    @Override
    public String getName() {
        return "查询报表SQL表定义";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "moduleId", type = ApiParamType.STRING, desc = "模块id"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "表定义列表", type = ApiParamType.JSONARRAY)})
    @Description(desc = "查询报表SQL表定义")
    @Override
    public Object myDoService(JSONObject jsonObj) {
        BasePageVo pageVo = JSONObject.toJavaObject(jsonObj, BasePageVo.class);
        JSONArray allList = reportSqldefineService.searchTable(jsonObj.getString("moduleId"), jsonObj.getString("keyword"));
        JSONObject resultObj = new JSONObject();
        if (pageVo.getNeedPage()) {
            int rowNum = allList.size();
            int fromIndex = Math.min(pageVo.getStartNum(), rowNum);
            int toIndex = Math.min(fromIndex + pageVo.getPageSize(), rowNum);
            JSONArray pageList = new JSONArray();
            for (int i = fromIndex; i < toIndex; i++) {
                pageList.add(allList.get(i));
            }
            resultObj.put("tbodyList", pageList);
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageSize", pageVo.getPageSize());
            resultObj.put("currentPage", pageVo.getCurrentPage());
            resultObj.put("pageCount", PageUtil.getPageCount(rowNum, pageVo.getPageSize()));
        } else {
            resultObj.put("tbodyList", allList);
        }
        return resultObj;
    }
}
