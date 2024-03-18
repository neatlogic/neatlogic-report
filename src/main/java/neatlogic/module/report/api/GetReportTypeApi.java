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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.module.report.auth.label.REPORT_TEMPLATE_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportTypeVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_TEMPLATE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportTypeApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

    @Override
    public String getToken() {
        return "report/type/get";
    }

    @Override
    public String getName() {
        return "nmra.getreporttypeapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "nmra.getreporttypeapi.getname")
    @Output(@Param(explode = ReportTypeVo.class))
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray returnList = new JSONArray();
        List<ReportTypeVo> reportTypeList = reportMapper.getAllReportType();
        int reportCount = 0;
        JSONObject all = new JSONObject();
        all.put("label", $.t("common.all"));
        all.put("name", "all");
        all.put("id", "all");
        returnList.add(all);
        for (ReportTypeVo reportTypeVo : reportTypeList) {
            JSONObject obj = new JSONObject();
            obj.put("label", reportTypeVo.getLabel());
            obj.put("name", reportTypeVo.getName());
            obj.put("id", reportTypeVo.getName());
            obj.put("count", reportTypeVo.getReportCount());
            returnList.add(obj);
            reportCount += reportTypeVo.getReportCount();
        }
        all.put("count", reportCount);
        return returnList;
    }
}
