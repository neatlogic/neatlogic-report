/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.statement;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.report.dto.ReportStatementVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import codedriver.module.report.auth.label.REPORT_ADMIN;
import codedriver.module.report.dao.mapper.ReportStatementMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = REPORT_ADMIN.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchReportStatementApi extends PrivateApiComponentBase {
    @Resource
    private ReportStatementMapper reportStatementMapper;

    @Override
    public String getToken() {
        return "report/statement/search";
    }

    @Override
    public String getName() {
        return "搜索报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字")})
    @Output({@Param(name = "tbodyList", explode = ReportStatementVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "搜索报表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportStatementVo reportStatementVo = JSONObject.toJavaObject(jsonObj, ReportStatementVo.class);
        List<ReportStatementVo> reportStatementList = reportStatementMapper.searchReportStatement(reportStatementVo);
        if (CollectionUtils.isNotEmpty(reportStatementList)) {
            reportStatementVo.setRowNum(reportStatementMapper.searchReportStatementCount(reportStatementVo));
        }
        return TableResultUtil.getResult(reportStatementList, reportStatementVo);
    }

}
