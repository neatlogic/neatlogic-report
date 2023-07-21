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
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.report.exception.ReportNotFoundEditTargetException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.sqlrunner.SqlInfo;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ShowReportDetailApi extends PrivateBinaryStreamApiComponentBase {
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
        return "report/show/{id}";
    }

    @Override
    public String getName() {
        return "nmra.showreportdetailapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true),
            @Param(name = "reportInstanceId", type = ApiParamType.LONG, desc = "term.report.reportinstanceid"),
    })
    @Description(desc = "nmra.showreportdetailapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        Long reportId = paramObj.getLong("id");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        ReportVo reportVo = reportService.getReportDetailById(reportId);
        if (reportVo == null) {
            throw new ReportNotFoundEditTargetException(reportId);
        }
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        reportMapper.updateReportInstanceVisitCount(reportInstanceId);

        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        // Map<String, Object> paramMap = ReportToolkit.getParamMap(request);
        PrintWriter out = response.getWriter();
        try {
            List<SqlInfo> tableList = getTableList(reportVo.getContent());
            //out.write("<!DOCTYPE HTML>");
            //out.write("<html lang=\"en\">");
            //out.write("<head>");
            //out.write("<title>" + reportVo.getName() + "</title>");
            //out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            //out.write("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            //out.write("</head>");
            //out.write("<body>");
            Map<String, Long> timeMap = new HashMap<>();
//            boolean isFirst = request.getHeader("referer") == null || !request.getHeader("referer").contains("report-show/" + reportId);
//            Map<String, Object> returnMap = reportService.getQueryResult(reportId, paramObj, timeMap, isFirst, showColumnsMap);
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, showColumnsMap, tableList);
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            ReportFreemarkerUtil.getFreemarkerContent(tmpMap, returnMap, pageMap, filter, reportVo.getContent(), out);
        } catch (Exception ex) {
            out.write("<div class=\"ivu-alert ivu-alert-error ivu-alert-with-icon ivu-alert-with-desc\">" + "<span class=\"ivu-alert-icon\"><i class=\"ivu-icon ivu-icon-ios-close-circle-outline\"></i></span>" + "<span class=\"ivu-alert-message\">异常：</span> <span class=\"ivu-alert-desc\"><span>" + ex.getMessage() + "</span></span></div>");
        }
        //out.write("</body></html>");
        out.flush();
        out.close();
        return null;
    }

    private List<SqlInfo> getTableList(String content) {
        List<SqlInfo> sqlInfoList = new ArrayList<>();
        if (StringUtils.isBlank(content)) {
            return sqlInfoList;
        }
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            String e = matcher.group();
            String tableId = getFieldValue(e, "data");
            if (StringUtils.isBlank(tableId)) {
                continue;
            }
            SqlInfo sqlInfo = new SqlInfo();
            sqlInfo.setId(tableId);
            sqlInfoList.add(sqlInfo);
            String needPage = getFieldValue(e, "needPage");
            if ("true".equalsIgnoreCase(needPage)) {
                sqlInfo.setNeedPage(true);
            }
            String pageSize = getFieldValue(e, "pageSize");
            if (StringUtils.isNotBlank(pageSize)) {
                sqlInfo.setPageSize(Integer.parseInt(pageSize));
            }
        }
        return sqlInfoList;
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
