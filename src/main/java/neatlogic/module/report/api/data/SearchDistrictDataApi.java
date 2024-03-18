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

package neatlogic.module.report.api.data;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.dto.data.DistrictVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportDataMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchDistrictDataApi extends PrivateApiComponentBase {

    @Resource
    private ReportDataMapper reportDataMapper;

    @Override
    public String getToken() {
        return "report/data/district/search";
    }

    @Override
    public String getName() {
        return "搜索地理数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "parent", type = ApiParamType.INTEGER, desc = "上级区域编码，例如中国编码：100000")})
    @Output({@Param(explode = DistrictVo[].class)})
    @Description(desc = "搜索地理数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        DistrictVo districtVo = JSONObject.toJavaObject(jsonObj, DistrictVo.class);
        if (districtVo.getParent() != null) {
            DistrictVo parentDistrictVo = reportDataMapper.getDistrictDataById(districtVo.getParent());
            districtVo.setLft(parentDistrictVo.getLft());
            districtVo.setRht(parentDistrictVo.getRht());
        }
        return reportDataMapper.searchDistrictData(districtVo);
    }

}
