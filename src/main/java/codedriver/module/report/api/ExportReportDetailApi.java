package codedriver.module.report.api;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.exception.ReportNotFoundException;
import codedriver.module.report.service.ReportService;
import codedriver.module.report.util.ExportUtil;
import codedriver.module.report.util.ReportFreemarkerUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ExportReportDetailApi extends PrivateBinaryStreamApiComponentBase {
    private static final Log logger = LogFactory.getLog(ExportReportDetailApi.class);

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private ReportService reportService;

    @Override
    public String getToken() {
        return "report/export/{id}/{type}";
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
        Long reportId = paramObj.getLong("id");
        String type = paramObj.getString("type");
        Long reportInstanceId = paramObj.getLong("reportInstanceId");
        // 统计使用次数
        reportMapper.updateReportVisitCount(reportId);
        /** 获取表格显示列配置 */
        Map<String, List<String>> showColumnsMap = reportService.getShowColumnsMap(reportInstanceId);

        OutputStream os = null;
        try {
            ReportVo reportVo = reportService.getReportDetailById(reportId);
            if (reportVo == null) {
                throw new ReportNotFoundException(reportId);
            }
            Map<String, Long> timeMap = new HashMap<>();
            Map<String, Object> returnMap = reportService.getQueryResult(reportId, paramObj, timeMap, false,showColumnsMap);
            Map<String, Object> tmpMap = new HashMap<>();
            Map<String, Object> commonMap = new HashMap<>();
            tmpMap.put("report", returnMap);
            tmpMap.put("param", paramObj);
            tmpMap.put("common", commonMap);

            String content = ReportFreemarkerUtil.getFreemarkerExportContent(tmpMap, reportVo.getContent());
            if ("pdf".equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                    "attachment;filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".pdf\"");
                ExportUtil.getPdfFileByHtml(content, true, os);
            } else if ("word".equals(type)) {
                os = response.getOutputStream();
                response.setContentType("application/x-download");
                response.setHeader("Content-Disposition",
                    "attachment;filename=\"" + URLEncoder.encode(reportVo.getName(), "utf-8") + ".docx\"");
                ExportUtil.getWordFileByHtml(content, true, os);
            }else if("excel".equals(type)){
                List<List<Map<String, Object>>> tableList = getTableListByHtml(content);
                if(CollectionUtils.isNotEmpty(tableList)){
                    HSSFWorkbook workbook = new HSSFWorkbook();
                    for(int i = 0;i < tableList.size();i++){
                        List<Map<String, Object>> list = tableList.get(i);
                        Map<String, Object> map = list.get(i);
                        List<String> headerList = new ArrayList<>();
                        List<String> columnList = new ArrayList<>();
                        for(String key : map.keySet()){
                            headerList.add(key);
                            columnList.add(key);
                        }
                        ExcelUtil.exportData(workbook,headerList,columnList,list,30,i);
                    }
                    String fileNameEncode = reportVo.getName() + ".xls";
                    Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
                    if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                        fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
                    } else {
                        fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
                    }
                    response.setContentType("application/vnd.ms-excel;charset=utf-8");
                    response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
                    os = response.getOutputStream();
                    workbook.write(os);
                }
            }

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
     * 从HTML中抽取表格元素数据
     * 一个报表中可能存在多个表格，这些表格的table上都有class="tstable-body"的属性，
     * 所以可以根据class拿到所有表格
     * @param content
     * @return
     */
    private List<List<Map<String, Object>>> getTableListByHtml(String content) {
        List<List<Map<String,Object>>> tableList = null;
        if(StringUtils.isNotBlank(content)){
            Document doc = Jsoup.parse(content);
            Elements tableElements = null;
            /** 抽取所有带class="tstable-body"的表格元素 */
            if(doc != null && CollectionUtils.isNotEmpty((tableElements = doc.getElementsByClass("tstable-body")))){
                tableList = new ArrayList<>();
                Iterator<Element> tableIterator = tableElements.iterator();
                while(tableIterator.hasNext()){
                    Element next = tableIterator.next();
                    Elements ths = next.getElementsByTag("th");
                    Elements tbodys = next.getElementsByTag("tbody");
                    if(CollectionUtils.isNotEmpty(ths) && CollectionUtils.isNotEmpty(tbodys)){
                        Iterator<Element> thIterator = ths.iterator();
                        List<String> thValueList = new ArrayList<>();
                        /** 抽取表头数据 */
                        while (thIterator.hasNext()){
                            String text = thIterator.next().ownText();
                            thValueList.add(text);
                        }
                        Element tbody = tbodys.first();
                        Elements trs = tbody.getElementsByTag("tr");
                        if(CollectionUtils.isNotEmpty(trs) && CollectionUtils.isNotEmpty(thValueList)){
                            Iterator<Element> trIterator = trs.iterator();
                            List<Map<String,Object>> valueList = new ArrayList<>();
                            /** 抽取表格内容数据，与表头key-value化存储 */
                            while (trIterator.hasNext()){
                                Element tds = trIterator.next();
                                Elements tdEls = tds.getElementsByTag("td");
                                List<Element> tdList = tdEls.subList(0, tdEls.size());
                                Map<String,Object> map = new HashMap<>();
                                for(int i = 0;i < tdList.size();i++){
                                    map.put(thValueList.get(i),tdList.get(i).ownText());
                                }
                                valueList.add(map);
                            }
                            tableList.add(valueList);
                        }
                    }
                }
            }
        }
        return tableList;
    }

}
