/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
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
        return "nmra.searchreportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", desc = "common.isactive"),
            @Param(name = "type", type = ApiParamType.STRING, desc = "common.type"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword", xss = true),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
    })
    @Output({@Param(explode = BasePageVo.class), @Param(name = "tbodyList", desc = "common.tbodylist", explode = ReportVo[].class)})
    @Description(desc = "nmra.searchreportapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportVo reportVo = JSONObject.toJavaObject(jsonObj, ReportVo.class);
        if (Objects.equals(reportVo.getType(), "all")) {
            reportVo.setType(null);
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
