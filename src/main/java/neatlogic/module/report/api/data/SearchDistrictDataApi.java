/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.report.api.data;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.dto.data.DistrictVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportDataMapper;
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
