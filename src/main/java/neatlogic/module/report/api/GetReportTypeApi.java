/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_MODIFY;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportTypeVo;
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
        int reportCount = 0;
        JSONObject all = new JSONObject();
        all.put("label", "所有");
        all.put("name", "all");
        all.put("id", "all");
        returnList.add(all);
        for (ReportTypeVo reportTypeVo : reportTypeList) {
            JSONObject obj = new JSONObject();
            obj.put("label", reportTypeVo.getLabel());
            obj.put("name", reportTypeVo.getName());
            obj.put("id", reportTypeVo.getName());
            obj.put("count", reportTypeVo.getReportCount());
            returnList.add(obj);
            reportCount += reportTypeVo.getReportCount();
        }
        all.put("count", reportCount);
        return returnList;
    }
}
