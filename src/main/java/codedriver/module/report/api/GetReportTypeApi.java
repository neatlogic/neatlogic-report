/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_MODIFY;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportTypeVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportTypeApi extends PrivateApiComponentBase {

    @Resource
    private ReportMapper reportMapper;

    @Override
    public String getToken() {
        return "report/type/get";
    }

    @Override
    public String getName() {
        return "获取报表分类";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "获取报表分类")
    @Output(@Param(explode = ReportTypeVo.class))
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONArray returnList = new JSONArray();
        List<ReportTypeVo> reportTypeList = reportMapper.getAllReportType();
        for (ReportTypeVo reportTypeVo : reportTypeList) {
            JSONObject obj = new JSONObject();
            if (reportTypeVo.getReportCount() > 0) {
                obj.put("label", reportTypeVo.getLabel() + "(" + reportTypeVo.getReportCount() + ")");
            } else {
                obj.put("label", reportTypeVo.getLabel());
            }
            obj.put("name", reportTypeVo.getName());
            obj.put("id", reportTypeVo.getName());
            returnList.add(obj);
        }
        return returnList;
    }
}
