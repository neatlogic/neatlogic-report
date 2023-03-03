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

package neatlogic.module.report.api.config;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.report.dto.ReportBlackWhiteListVo;
import neatlogic.framework.report.exception.ReportBlackWhiteListIsExistsException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_ADMIN;
import neatlogic.module.report.dao.mapper.ReportConfigMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Service
public class SaveReportBlackWhiteListApi extends PrivateApiComponentBase {

    @Resource
    private ReportConfigMapper reportConfigMapper;


    @Override
    public String getToken() {
        return "/report/config/blackwhitelist/save";
    }

    @Override
    public String getName() {
        return "保存报表可用对象";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，不提供代表添加"),
            @Param(name = "itemName", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "对象名称"),
            @Param(name = "type", type = ApiParamType.ENUM, isRequired = true, rule = "blacklist,whitelist", desc = "类型"),
            @Param(name = "itemType", type = ApiParamType.ENUM, isRequired = true, rule = "table,column", desc = "对象类型"),
            @Param(name = "description", type = ApiParamType.STRING, xss = true, desc = "说明"),
    })
    @Output({@Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ReportBlackWhiteListVo.class)})
    @Description(desc = "保存报表可用对象接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportBlackWhiteListVo reportBlackWhiteListVo = JSONObject.toJavaObject(jsonObj, ReportBlackWhiteListVo.class);
        Long id = jsonObj.getLong("id");
        if (reportConfigMapper.checkItemIsExists(reportBlackWhiteListVo) > 0) {
            throw new ReportBlackWhiteListIsExistsException(reportBlackWhiteListVo);
        }
        if (id == null) {
            reportConfigMapper.insertBlackWhiteList(reportBlackWhiteListVo);
        } else {
            reportConfigMapper.updateBlackWhiteList(reportBlackWhiteListVo);
        }
        return null;
    }
}
