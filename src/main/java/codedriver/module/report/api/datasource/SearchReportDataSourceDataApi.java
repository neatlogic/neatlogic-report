/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api.datasource;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.report.dto.ReportDataSourceDataVo;
import codedriver.framework.report.dto.ReportDataSourceFieldVo;
import codedriver.framework.report.dto.ReportDataSourceVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.report.dao.mapper.ReportDataSourceDataMapper;
import codedriver.module.report.dao.mapper.ReportDataSourceMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchReportDataSourceDataApi extends PrivateApiComponentBase {

    @Resource
    private ReportDataSourceMapper reportDataSourceMapper;
    @Resource
    private ReportDataSourceDataMapper reportDataSourceDataMapper;


    @Override
    public String getToken() {
        return "report/datasource/data/search";
    }

    @Override
    public String getName() {
        return "查询数据源数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "dataSourceId", type = ApiParamType.LONG, desc = "数据源id", isRequired = true)})
    @Description(desc = "查询数据源数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ReportDataSourceDataVo reportDataSourceDataVo = JSONObject.toJavaObject(jsonObj, ReportDataSourceDataVo.class);
        ReportDataSourceVo reportDataSourceVo = reportDataSourceMapper.getReportDataSourceById(reportDataSourceDataVo.getDataSourceId());
        JSONObject returnObj = new JSONObject();
        if (CollectionUtils.isNotEmpty(reportDataSourceVo.getFieldList())) {
            reportDataSourceDataVo.setFieldList(reportDataSourceVo.getFieldList());
            int rowNum = reportDataSourceDataMapper.searchDataSourceDataCount(reportDataSourceDataVo);
            reportDataSourceDataVo.setRowNum(rowNum);
            List<HashMap<String, Object>> resultList = reportDataSourceDataMapper.searchDataSourceData(reportDataSourceDataVo);

            JSONArray headerList = new JSONArray();
            JSONObject idHeadObj = new JSONObject();
            idHeadObj.put("key", "id");
            idHeadObj.put("title", "#");
            headerList.add(idHeadObj);
            JSONObject insertTimeHeadObj = new JSONObject();
            insertTimeHeadObj.put("key", "insertTime");
            insertTimeHeadObj.put("title", "添加日期");
            headerList.add(insertTimeHeadObj);
            for (ReportDataSourceFieldVo fieldVo : reportDataSourceVo.getFieldList()) {
                JSONObject headObj = new JSONObject();
                headObj.put("key", "field_" + fieldVo.getId());
                headObj.put("title", fieldVo.getLabel());
                headerList.add(headObj);
            }
            returnObj.put("currentPage", reportDataSourceDataVo.getCurrentPage());
            returnObj.put("pageSize", reportDataSourceDataVo.getPageSize());
            returnObj.put("pageCount", reportDataSourceDataVo.getPageCount());
            returnObj.put("rowNum", reportDataSourceDataVo.getRowNum());
            returnObj.put("theadList", headerList);
            returnObj.put("tbodyList", resultList);
        }
        return returnObj;
    }

}
