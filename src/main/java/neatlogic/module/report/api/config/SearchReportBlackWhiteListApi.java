/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api.config;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.report.dto.ReportBlackWhiteListVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.report.auth.label.REPORT_ADMIN;
import neatlogic.module.report.dao.mapper.ReportConfigMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class SearchReportBlackWhiteListApi extends PrivateApiComponentBase {

    @Resource
    private ReportConfigMapper reportConfigMapper;


    @Override
    public String getToken() {
        return "/report/config/blackwhitelist/search";
    }

    @Override
    public String getName() {
        return "报表可用对象查询";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "black,white", desc = "类型"),
            @Param(name = "itemType", type = ApiParamType.ENUM, rule = "table,column", desc = "对象类型")})
    @Output({@Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ReportBlackWhiteListVo.class)})
    @Description(desc = "报表可用对象查询接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportBlackWhiteListVo reportBlackWhiteListVo = JSONObject.toJavaObject(jsonObj, ReportBlackWhiteListVo.class);
        List<ReportBlackWhiteListVo> resultList = reportConfigMapper.searchBlackWhiteList(reportBlackWhiteListVo);
        if (CollectionUtils.isNotEmpty(resultList)) {
            int rowNum = reportConfigMapper.searchBlackWhiteListCount(reportBlackWhiteListVo);
            reportBlackWhiteListVo.setRowNum(rowNum);
        }
        return TableResultUtil.getResult(resultList, reportBlackWhiteListVo);
    }
}
