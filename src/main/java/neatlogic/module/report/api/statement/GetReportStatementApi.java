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

package neatlogic.module.report.api.statement;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.dto.ReportStatementVo;
import neatlogic.framework.report.exception.ReportStatementNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportStatementMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportStatementApi extends PrivateApiComponentBase {
    @Resource
    private ReportStatementMapper reportStatementMapper;

    @Override
    public String getToken() {
        return "report/statement/get";
    }

    @Override
    public String getName() {
        return "nmras.getreportstatementapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "common.id")})
    @Output({@Param(explode = ReportStatementVo.class)})
    @Description(desc = "nmras.getreportstatementapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        ReportStatementVo reportStatementVo = reportStatementMapper.getReportStatementById(id);
        if (reportStatementVo == null) {
            throw new ReportStatementNotFoundEditTargetException(id);
        }
        return reportStatementVo;
    }

}
