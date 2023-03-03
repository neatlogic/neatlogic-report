/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.api;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.report.exception.ReportNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.report.auth.label.REPORT_BASE;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.ReportVo;
import neatlogic.module.report.service.ReportService;
import neatlogic.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class GetReportTableApi extends PrivateBinaryStreamApiComponentBase {
    /**
     * 匹配表格
     */
    private final Pattern pattern = Pattern.compile("\\$\\{drawTable\\(.*\\)\\}");

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/table/get";
    }

    @Override
    public String getName() {
        return "获取报表表格分页数据";
    }

    @Input({
            @Param(name = "id", desc = "报表id", isRequired = true),
            @Param(name = "reportInstanceId", desc = "报表实例id"),
            @Param(name = "tableId", desc = "表格ID"),
            @Param(name = "currentPage", desc = "当前页数"),
            @Param(name = "pageSize", desc = "每页条数"),
    })
    @Description(desc = "展示报表接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        filter.remove("tableId");
        filter.remove("currentPage");
        filter.remove("pageSize");
        Long reportId = paramObj.getLong("id");
        ReportVo reportVo = reportMapper.getReportById(reportId);
        if (reportVo == null) {
            throw new ReportNotFoundException(reportId);
        }
        if (StringUtils.isBlank(reportVo.getSql())) {
            return null;
        }
        String content = reportVo.getContent();
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String tableContent = null;
        String tableId = paramObj.getString("tableId");
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            String e = matcher.group();
            String data = getFieldValue(e, "data");
            if (Objects.equals(tableId, data)) {
                tableContent = e;
                break;
            }
        }
        if (StringUtils.isBlank(tableContent)) {
            return null;
        }
        Long reportInstanceId = paramObj.getLong("reportInstanceId");

        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        PrintWriter out = response.getWriter();
        try {
            boolean isFirst = request.getHeader("referer") == null || !request.getHeader("referer").contains("report-show/" + reportId);
            Map<String, Object> returnMap = reportService.getQuerySqlResultById(tableId, reportVo, paramObj, showColumnsMap);
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            ReportFreemarkerUtil.getFreemarkerContent(tmpMap, returnMap, pageMap, filter, tableContent, out);
        } catch (Exception ex) {
            out.write("<div class=\"ivu-alert ivu-alert-error ivu-alert-with-icon ivu-alert-with-desc\">" + "<span class=\"ivu-alert-icon\"><i class=\"ivu-icon ivu-icon-ios-close-circle-outline\"></i></span>" + "<span class=\"ivu-alert-message\">异常：</span> <span class=\"ivu-alert-desc\"><span>" + ex.getMessage() + "</span></span></div>");
        }
        //out.write("</body></html>");
        out.flush();
        out.close();
        return null;
    }

    @Override
    public String getConfig() {
        return null;
    }

    private String getFieldValue(String str, String field) {
        int beginIndex = str.indexOf(field);
        if (beginIndex != -1) {
            beginIndex += field.length();
            int index1 = str.indexOf(",", beginIndex);
            int index2 = str.indexOf("}", beginIndex);
            int endIndex = -1;
            if (index1 == -1) {
                endIndex = index2;
            } else if (index2 == -1) {
                endIndex = index1;
            } else {
                endIndex = Math.min(index1, index2);
            }
            if (endIndex == -1) {
                return null;
            }
            String value = str.substring(beginIndex, endIndex);
            value = value.trim();
            if (!value.startsWith(":")) {
                return null;
            }
            value = value.substring(1);
            value = value.trim();
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }
            value = value.trim();
            return value;
        }
        return null;
    }
}
