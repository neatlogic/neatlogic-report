/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.api;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.report.exception.ReportNotFoundException;
import codedriver.framework.report.exception.TableNotFoundInReportException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.DocType;
import codedriver.framework.util.ExportUtil;
import codedriver.framework.util.excel.ExcelBuilder;
import codedriver.framework.util.excel.SheetBuilder;
import codedriver.module.report.auth.label.REPORT_BASE;
import codedriver.module.report.constvalue.ActionType;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@AuthAction(action = REPORT_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
@Service
public class ExportReportDetailApi extends PrivateBinaryStreamApiComponentBase {
    private static final Log logger = LogFactory.getLog(ExportReportDetailApi.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/detail/export/{id}/{type}";
    }

    @Override
    public String getName() {
        return "导出报表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", desc = "报表id", type = ApiParamType.LONG, isRequired = true),
            @Param(name = "reportInstanceId", desc = "报表实例id", type = ApiParamType.LONG),
            @Param(name = "type", desc = "文件类型", type = ApiParamType.ENUM, rule = "pdf,word,excel", isRequired = true)})
    @Description(desc = "导出报表接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JSONObject filter = new JSONObject();
        filter.putAll(paramObj);
        filter.remove("type");
        Long reportId = paramObj.getLong("id");
        String type = paramObj.getString("type");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        /* 获取表格显示列配置 */
        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        OutputStream os = null;
        try {
            ReportVo reportVo = reportService.getReportDetailById(reportId);
            if (reportVo == null) {
                throw new ReportNotFoundException(reportId);
            }
            Map<String, Object> returnMap = reportService.getQuerySqlResult(reportVo, paramObj, showColumnsMap);
            Map<String, Map<String, Object>> pageMap = (Map<String, Map<String, Object>>) returnMap.remove("page");
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, returnMap, pageMap, filter, reportVo.getContent(), ActionType.EXPORT.getValue());
            if (DocType.PDF.getValue().equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        " attachment; filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".pdf\"");
                ExportUtil.getPdfFileByHtml(content, os, true, true);
            } else if (DocType.WORD.getValue().equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/x-download");
                response.setHeader("Content-Disposition",
                        " attachment; filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".docx\"");
                ExportUtil.getWordFileByHtml(content, os, true, true);
            } else if (DocType.EXCEL.getValue().equals(type)) {
                Map<String, List<Map<String, Object>>> tableMap = getTableListByHtml(content);
                if (MapUtils.isEmpty(tableMap)) {
                    throw new TableNotFoundInReportException();
                }
                ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class);
                for (Map.Entry<String, List<Map<String, Object>>> entry : tableMap.entrySet()) {
                    String tableName = entry.getKey();
                    List<Map<String, Object>> tableBody = entry.getValue();
                    Map<String, Object> map = tableBody.get(0);
                    List<String> headerList = new ArrayList<>();
                    List<String> columnList = new ArrayList<>();
                    for (String key : map.keySet()) {
                        headerList.add(key);
                        columnList.add(key);
                    }
                    SheetBuilder sheetBuilder = builder.withBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT)
                            .withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE)
                            .withHeadBgColor(HSSFColor.HSSFColorPredefined.DARK_BLUE)
                            .withColumnWidth(30)
                            .addSheet(tableName)
                            .withHeaderList(headerList)
                            .withColumnList(columnList);
                    sheetBuilder.addDataList(tableBody);
                }
                Workbook workbook = builder.build();
                String fileNameEncode = reportVo.getName() + ".xlsx";
                Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
                if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                    fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
                } else {
                    fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                }
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
                os = response.getOutputStream();
                workbook.write(os);
            }
        } catch (ApiRuntimeException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
        return null;
    }

    /**
     * 遵循以下格式的HTML，才会被识别表格
     * 其中：
     * 1、class="ivu-card ivu-card-dis-hover ivu-card-shadow"的<div>标签必须有id
     * 2、必须存在class="ivu-card-head"的div标签
     * 3、<table>标签的class必须为table-main tstable-body
     * 4、<tbody>标签的class必须为tbody-main
     * 5、<thead>标签必须存在，且符合标准DOM结构(thead > tr > th)，<tr>标签的class必须为th-left
     * <div id="tableDataMonth" class="ivu-card ivu-card-dis-hover ivu-card-shadow">
     *     <div class="ivu-card-head">按月统计</div>
     *     <div class="ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large">
     *         <div class="tstable-main bg-op">
     *             <table class="table-main tstable-body">
     *                 <thead>
     *                     <tr class="th-left">
     *                         <th>月</th>
     *                         <th>工单数量</th>
     *                     </tr>
     *                 </thead>
     *                 <tbody class="tbody-main">
     *                     <tr>
     *                         <td>2022-06</td>
     *                         <td>22</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2022-05</td>
     *                         <td>26</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2022-04</td>
     *                         <td>3</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2022-03</td>
     *                         <td>121</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2022-02</td>
     *                         <td>98</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2022-01</td>
     *                         <td>189</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2021-12</td>
     *                         <td>141</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2021-11</td>
     *                         <td>72</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2021-10</td>
     *                         <td>29</td>
     *                     </tr>
     *                     <tr>
     *                         <td>2021-09</td>
     *                         <td>10</td>
     *                     </tr>
     *                 </tbody>
     *             </table>
     *         </div>
     *     </div>
     * </div>
     *
     * @param content
     * @return
     */
    private Map<String, List<Map<String, Object>>> getTableListByHtml(String content) {
        Map<String, List<Map<String, Object>>> tableMap = new LinkedHashMap();
        if (StringUtils.isNotBlank(content)) {
            Document doc = Jsoup.parse(content);
            /** 抽取所有class="ivu-card ivu-card-dis-hover ivu-card-shadow"的元素 */
            Elements elements = doc.getElementsByClass("ivu-card ivu-card-dis-hover ivu-card-shadow");
            if (CollectionUtils.isNotEmpty(elements)) {
                for (Element element : elements) {
                    if (element.hasAttr("id")) {
                        Elements tableNameEls = element.getElementsByClass("ivu-card-head");
                        String tableName = tableNameEls.text();
                        if (StringUtils.isNotBlank(tableName)) {
                            Elements tableBodyEls = element.getElementsByClass("table-main tstable-body");
                            if (CollectionUtils.isNotEmpty(tableBodyEls)) {
                                Element tableBody = tableBodyEls.get(0);
                                Elements ths = tableBody.select(".th-left>th");
                                Elements tbodys = tableBody.getElementsByClass("tbody-main");
                                if (CollectionUtils.isNotEmpty(ths) && CollectionUtils.isNotEmpty(tbodys)) {
                                    Iterator<Element> thIterator = ths.iterator();
                                    List<String> thValueList = new ArrayList<>();
                                    /** 抽取表头数据 */
                                    while (thIterator.hasNext()) {
                                        String text = thIterator.next().ownText();
                                        thValueList.add(text);
                                    }
                                    Element tbody = tbodys.first();
                                    Elements trs = tbody.children();
                                    if (CollectionUtils.isNotEmpty(trs) && CollectionUtils.isNotEmpty(thValueList)) {
                                        Iterator<Element> trIterator = trs.iterator();
                                        List<Map<String, Object>> valueList = new ArrayList<>();
                                        /** 抽取表格内容数据，与表头key-value化存储 */
                                        while (trIterator.hasNext()) {
                                            Element tds = trIterator.next();
                                            Elements tdEls = tds.children();
                                            List<Element> tdList = tdEls.subList(0, tdEls.size());
                                            Map<String, Object> map = new LinkedHashMap<>();
                                            for (int i = 0; i < tdList.size(); i++) {
                                                map.put(thValueList.get(i), tdList.get(i).text()); // text()返回剥离HTML标签的内容
                                            }
                                            valueList.add(map);
                                        }
                                        tableMap.put(tableName, valueList);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return tableMap;
    }

}
