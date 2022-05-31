/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.report.exception.ReportNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.sqlrunner.SqlInfo;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
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
        return "展示报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", desc = "报表id", isRequired = true),
            @Param(name = "reportInstanceId", desc = "报表实例id"),
    })
    @Description(desc = "展示报表接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        Long reportId = paramObj.getLong("id");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        reportMapper.updateReportInstanceVisitCount(reportInstanceId);

        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        // Map<String, Object> paramMap = ReportToolkit.getParamMap(request);
        PrintWriter out = response.getWriter();
        try {
            ReportVo reportVo = reportService.getReportDetailById(reportId);
            if (reportVo == null) {
                throw new ReportNotFoundException(reportId);
            }
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
            boolean isFirst = request.getHeader("referer") == null || !request.getHeader("referer").contains("report-show/" + reportId);
//            Map<String, Object> returnMap = reportService.getQueryResult(reportId, paramObj, timeMap, isFirst, showColumnsMap);
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, isFirst, showColumnsMap, tableList);
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
//            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            ReportFreemarkerUtil.getFreemarkerContent(tmpMap, returnMap, pageMap, filter, reportVo.getContent(), out);
        } catch (Exception ex) {
            ex.printStackTrace();
            out.write("<div class=\"ivu-alert ivu-alert-error ivu-alert-with-icon ivu-alert-with-desc\">" + "<span class=\"ivu-alert-icon\"><i class=\"ivu-icon ivu-icon-ios-close-circle-outline\"></i></span>" + "<span class=\"ivu-alert-message\">异常：</span> <span class=\"ivu-alert-desc\"><span>" + ex.getMessage() + "</span></span></div>");
        }
        //out.write("</body></html>");
        out.flush();
        out.close();
        return null;
    }

    private List<SqlInfo> getTableList(String content) {
        List<SqlInfo> sqlInfoList = new ArrayList<>();
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
            if ("true".equals(needPage)) {
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
