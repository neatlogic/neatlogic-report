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
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ReportFreemarkerUtil;
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
            int beginIndex = e.indexOf("report.");
            beginIndex = e.indexOf(".", beginIndex);
            int endIndex = e.indexOf(",", beginIndex);
            String tableId2 = e.substring(beginIndex + 1, endIndex);
            if (Objects.equals(tableId, tableId2)) {
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
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.get("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            ReportFreemarkerUtil.getFreemarkerContent(tmpMap, pageMap, filter, tableContent, out);
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

}
