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

package neatlogic.module.report.api.config;

import com.alibaba.fastjson.JSONObject;
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
