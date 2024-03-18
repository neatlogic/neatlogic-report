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

package neatlogic.module.report.api.config;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.dto.ReportBlackWhiteListVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_ADMIN;
import neatlogic.module.report.dao.mapper.ReportConfigMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetReportBlackWhiteListApi extends PrivateApiComponentBase {

    @Resource
    private ReportConfigMapper reportConfigMapper;


    @Override
    public String getToken() {
        return "/report/config/blackwhitelist/get";
    }

    @Override
    public String getName() {
        return "获取报表可用对象";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "id")})
    @Output({@Param(explode = ReportBlackWhiteListVo.class)})
    @Description(desc = "获取报表可用对象接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        return reportConfigMapper.getBlackWhiteById(id);
    }
}
