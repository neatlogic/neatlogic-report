/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.datasource;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.report.dto.ReportDataSourceVo;
import codedriver.framework.report.exception.DataSourceIsNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.auth.label.REPORT_DATASOURCE_MODIFY;
import codedriver.module.report.dao.mapper.ReportDataSourceMapper;
import codedriver.module.report.service.ReportDataSourceService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@AuthAction(action = REPORT_DATASOURCE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
@Transactional
public class DeleteReportDataSourceApi extends PrivateApiComponentBase {

    @Resource
    private ReportDataSourceMapper reportDataSourceMapper;


    @Resource
    private ReportDataSourceService reportDataSourceService;


    @Override
    public String getToken() {
        return "report/datasource/delete";
    }

    @Override
    public String getName() {
        return "删除大屏数据源";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id", isRequired = true)})
    @Description(desc = "执行报表数据源数据同步接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        ReportDataSourceVo reportDataSourceVo = reportDataSourceMapper.getReportDataSourceById(id);
        if (reportDataSourceVo == null) {
            throw new DataSourceIsNotFoundException(id);
        }
        reportDataSourceService.deleteReportDataSource(reportDataSourceVo);
        return null;
    }

}
