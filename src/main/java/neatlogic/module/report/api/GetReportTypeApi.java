/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
