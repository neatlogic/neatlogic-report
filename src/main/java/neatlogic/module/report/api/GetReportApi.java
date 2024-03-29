/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.sqlrunner.SqlInfo;
import neatlogic.framework.sqlrunner.SqlRunner;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetReportApi extends PrivateApiComponentBase {

    private final static Logger logger = LoggerFactory.getLogger(GetReportApi.class);
    /**
     * 匹配表格
     */
    private static final Pattern pattern = Pattern.compile("drawTable\\((\\{.*\\})\\)");

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/get";
    }

    @Override
    public String getName() {
        return "nmra.getreportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)})
    @Output({@Param(explode = ReportVo.class)})
    @Description(desc = "nmra.getreportapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        if (reportMapper.getReportById(id) == null) {
            throw new ReportNotFoundEditTargetException(id);
        }
        ReportVo reportVo = reportService.getReportDetailById(id);
        /* 查找表格 */
        try {
            getTableList(reportVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return reportVo;
    }

    private void getTableList(ReportVo reportVo) throws DocumentException {
        String content = reportVo.getContent();
        String sql = reportVo.getSql();
        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(sql)) {
            Map<String, String> tables = new HashMap<>();
            Matcher matcher = pattern.matcher(content);
            /* 寻找是表格的图表，生成[id->title]的map */
            while (matcher.find()) {
                String tableConfig = matcher.group(1);
                try {
                    JSONObject config = JSONObject.parseObject(tableConfig);
                    String data = config.getString("data");
                    String title = config.getString("title");
                    if (data != null && title != null) {
                        tables.put(data, title);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            /* tableColumnsMap中的key为表格ID与中文名组合而成的字符串,value为表格字段
              e.g:"tableData-工单上报列表"
             */
            List<Map<String, Object>> tableColumnsMapList = null;
            if (MapUtils.isNotEmpty(tables)) {
                tableColumnsMapList = new ArrayList<>();
                /* 从SQL中获取所有图表
                  从中寻找表格，记录下其id、title与column
                 */
                SqlRunner sqlRunner = new SqlRunner(reportVo.getSql(), "reportId_" + reportVo.getId());
                List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(null);
                for (Map.Entry<String, String> entry : tables.entrySet()) {
                    for (SqlInfo sqlInfo : sqlInfoList) {
                        if (Objects.equals(entry.getKey(), sqlInfo.getId())) {
                            Map<String, Object> tableColumnsMap = new HashMap<>();
                            tableColumnsMap.put("id", sqlInfo.getId());
                            tableColumnsMap.put("title", entry.getValue());
                            tableColumnsMap.put("columnList", sqlInfo.getPropertyList());
                            tableColumnsMapList.add(tableColumnsMap);
                            break;
                        }
                    }
                }
            }
            reportVo.setTableList(tableColumnsMapList);
        }
    }

//    private void getTableList(ReportVo reportVo) throws DocumentException {
//        String content = reportVo.getContent();
//        String sql = reportVo.getSql();
//        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(sql)) {
//            Map<String, String> tables = new HashMap<>();
//            Matcher matcher = pattern.matcher(content);
//            /* 寻找是表格的图表，生成[包含id的字符串->title]的map */
//            while (matcher.find()) {
//                String e = matcher.group();
//                Matcher m = namePattern.matcher(e);
//                /* 寻找表格title */
//                if (m.find()) {
//                    String name = m.group();
//                    if (name.contains(",")) {
//                        name = name.substring(name.indexOf("\"") + 1, name.indexOf(",") - 1);
//                    } else {
//                        name = name.substring(name.indexOf("\"") + 1, name.lastIndexOf("\""));
//                    }
//                    tables.put(e, name);
//                } else {
//                    tables.put(e, null);
//                }
//            }
//            /* tableColumnsMap中的key为表格ID与中文名组合而成的字符串,value为表格字段
//              e.g:"tableData-工单上报列表"
//             */
//            List<Map<String, Object>> tableColumnsMapList = null;
//            if (MapUtils.isNotEmpty(tables)) {
//                tableColumnsMapList = new ArrayList<>();
//                /* 从SQL中获取所有图表
//                  从中寻找表格，记录下其id、title与column
//                 */
//                Map<String, Object> map = ReportXmlUtil.analyseSql(sql);
//                List<SelectVo> selectList = (List<SelectVo>) map.get("select");
//                for (Map.Entry<String, String> entry : tables.entrySet()) {
//                    for (SelectVo vo : selectList) {
//                        if (entry.getKey().contains(vo.getId())) {
//                            Map<String, Object> tableColumnsMap = new HashMap<>();
//                            tableColumnsMap.put("id", vo.getId());
//                            tableColumnsMap.put("title", entry.getValue());
//                            tableColumnsMap.put("columnList", vo.getResultMap().getPropertyList());
//                            tableColumnsMapList.add(tableColumnsMap);
//                            break;
//                        }
//                    }
//                }
//            }
//            reportVo.setTableList(tableColumnsMapList);
//        }
//    }
}
