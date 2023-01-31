/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

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
