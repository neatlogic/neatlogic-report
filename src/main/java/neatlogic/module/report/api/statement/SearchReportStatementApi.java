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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.report.dto.ReportStatementVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.report.auth.label.REPORT_STATEMENT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportStatementMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_STATEMENT_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchReportStatementApi extends PrivateApiComponentBase {
    @Resource
    private ReportStatementMapper reportStatementMapper;

    @Override
    public String getToken() {
        return "report/statement/search";
    }

    @Override
    public String getName() {
        return "搜索报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")})
    @Output({@Param(name = "tbodyList", explode = ReportStatementVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "搜索报表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportStatementVo reportStatementVo = JSONObject.toJavaObject(jsonObj, ReportStatementVo.class);
        List<ReportStatementVo> reportStatementList = reportStatementMapper.searchReportStatement(reportStatementVo);
        if (CollectionUtils.isNotEmpty(reportStatementList)) {
            reportStatementVo.setRowNum(reportStatementMapper.searchReportStatementCount(reportStatementVo));
        }
        return TableResultUtil.getResult(reportStatementList, reportStatementVo);
    }

}
