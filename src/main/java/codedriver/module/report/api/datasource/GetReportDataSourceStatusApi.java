/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.datasource;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.report.dto.ReportDataSourceVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportDataSourceMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportDataSourceStatusApi extends PrivateApiComponentBase {

    @Resource
    private ReportDataSourceMapper reportDataSourceMapper;

    @Override
    public String getToken() {
        return "report/datasource/status/get";
    }

    @Override
    public String getName() {
        return "获取报表数据源状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "idl列表", isRequired = true)})
    @Output({@Param(explode = ReportDataSourceVo[].class)})
    @Description(desc = "获取报表数据源状态接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < jsonObj.getJSONArray("idList").size(); i++) {
            idList.add(jsonObj.getJSONArray("idList").getLong(i));
        }
        if (CollectionUtils.isNotEmpty(idList)) {
            return reportDataSourceMapper.getReportDataSourceByIdList(idList);
        }
        return null;
    }

}
