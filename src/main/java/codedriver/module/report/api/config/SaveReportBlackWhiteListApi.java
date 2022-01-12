/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.config;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.report.dto.ReportBlackWhiteListVo;
import codedriver.framework.report.exception.ReportBlackWhiteListIsExistsException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_ADMIN;
import codedriver.module.report.dao.mapper.ReportConfigMapper;
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
