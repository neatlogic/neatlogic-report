package neatlogic.module.report.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.plugin.PageRowBounds;
import neatlogic.framework.dto.RestVo;
import neatlogic.framework.report.exception.ReportParamNameRepeatsException;
import neatlogic.framework.report.exception.TableNotFoundInReportException;
import neatlogic.framework.sqlrunner.SqlInfo;
import neatlogic.framework.sqlrunner.SqlRunner;
import neatlogic.framework.util.RestUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import neatlogic.module.report.config.ReportConfig;
import neatlogic.module.report.dao.mapper.ReportInstanceMapper;
import neatlogic.module.report.dao.mapper.ReportMapper;
import neatlogic.module.report.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    /**
     * 匹配表格
     */
    private final Pattern pattern = Pattern.compile("\\$\\{drawTable\\(.*\\)\\}");

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportInstanceMapper reportInstanceMapper;

    @Resource
    private DataSource dataSource;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public ReportVo getReportDetailById(Long reportId) {
        ReportVo reportVo = reportMapper.getReportById(reportId);
        if (reportVo != null) {
            reportVo.setParamList(reportMapper.getReportParamByReportId(reportId));
            List<ReportAuthVo> reportAuthList = reportMapper.getReportAuthByReportId(reportVo.getId());
            reportVo.setReportAuthList(reportAuthList);
        }
        return reportVo;
    }

    private Object getRemoteResult(RestVo restVo) {
        String result = RestUtil.sendPostRequest(restVo);
        try {
            return JSON.parse(result);
        } catch (Exception ex) {
            return result;
        }
    }


    /**
     * 查询报表实例的表格显示列配置
     *
     * @param reportInstanceId 报表实例id
     * @return 结果集
     */
    @Override
    public Map<String, List<String>> getShowColumnsMap(Long reportInstanceId) {
        Map<String, List<String>> showColumnsMap = new HashMap<>();
        /* 查询表格显示列配置 */
        List<ReportInstanceTableColumnVo> columnList = reportInstanceMapper.getReportInstanceTableColumnList(reportInstanceId);
        if (CollectionUtils.isNotEmpty(columnList)) {
            /* 根据tableId分组 */
            Map<String, List<ReportInstanceTableColumnVo>> columnMap = columnList.stream().collect(Collectors.groupingBy(ReportInstanceTableColumnVo::getTableId));
            /* 根据sort排序并取出字段名，组装成tableId与字段列表的map */
            for (Map.Entry<String, List<ReportInstanceTableColumnVo>> entry : columnMap.entrySet()) {
                List<String> columns = entry.getValue().stream().sorted(Comparator.comparing(ReportInstanceTableColumnVo::getSort)).map(ReportInstanceTableColumnVo::getColumn).collect(Collectors.toList());
                showColumnsMap.put(entry.getKey(), columns);
            }
        }
        return showColumnsMap;
    }

    private List<Map<String, Object>> selectTableColumns(Map<String, List<String>> showColumnsMap, SelectVo select, List<Map<String, Object>> tmpList) {
        List<String> showColumnList = showColumnsMap.get(select.getId());
        /* 筛选表格显示列 */
        for (Map<String, Object> map : tmpList) {
            map.entrySet().removeIf(stringObjectEntry -> !showColumnList.contains(stringObjectEntry.getKey()));
        }
        /* 排序 */
        List<Map<String, Object>> sqList = new ArrayList<>();
        for (Map<String, Object> map : tmpList) {
            Map<String, Object> _map = new LinkedHashMap<>();
            for (String s : showColumnList) {
                _map.put(s, map.get(s));
            }
            sqList.add(_map);
        }
        return sqList;
    }

    private List<Map<String, Object>> selectTableColumns(List<String> showColumnList, List<Map<String, Object>> tmpList) {
        /* 筛选表格显示列 */
//        for (Map<String, Object> map : tmpList) {
//            map.entrySet().removeIf(stringObjectEntry -> !showColumnList.contains(stringObjectEntry.getKey()));
//        }
        /* 排序 */
        List<Map<String, Object>> sqList = new ArrayList<>();
        for (Map<String, Object> map : tmpList) {
            Map<String, Object> _map = new LinkedHashMap<>();
            for (String s : showColumnList) {
                Object value = map.get(s);
                if (value == null) {
                    value = "null";
                }
                _map.put(s, value);
            }
            sqList.add(_map);
        }
        return sqList;
    }

    private Map<String, List> wrapResultMapToMap(ResultMapVo resultMapVo, Map<String, Object> result, Map<String, List> returnMap) {
        StringBuilder key = new StringBuilder();
        List<Map<String, Object>> resultList = null;
        if (resultMapVo.getGroupByList() != null && resultMapVo.getGroupByList().size() > 0) {
            for (int i = 0; i < resultMapVo.getGroupByList().size(); i++) {
                key.append(result.get(resultMapVo.getGroupByList().get(i)));
                if (i < resultMapVo.getGroupByList().size() - 1) {
                    key.append("-");
                }
            }
        } else {
            return null;
        }
        Map<String, Object> newResult = new HashMap<>();
        if (!key.toString().equals("") && returnMap.containsKey(key.toString())) {
            resultList = returnMap.get(key.toString());
        } else {
            resultList = new ArrayList<>();
            returnMap.put(key.toString(), resultList);
        }

        // Iterator<Map.Entry<String, Object>> iter =
        // result.entrySet().iterator();
        for (String str : resultMapVo.getPropertyList()) {
            newResult.put(str, result.get(str));
        }
        resultList.add(newResult);
        return returnMap;
    }

    /**
     * 判断这一条数据是否已经存在
     *
     * @param resultMapVo 结果数据
     * @param resultList  结果数据
     * @param result      结果数据
     * @param checkMap    中途结果
     * @return 结果
     */
    private Boolean isExists(ResultMapVo resultMapVo, List<Map<String, Object>> resultList, Map<String, Object> result, Map<String, Map<String, Object>> checkMap) {
        boolean isExists = false;
        StringBuilder key = new StringBuilder();
        if (resultList == null) {
            resultList = new ArrayList<Map<String, Object>>();
        }
        if (resultMapVo.getGroupByList() != null && resultMapVo.getGroupByList().size() > 0) {
            for (int i = 0; i < resultMapVo.getGroupByList().size(); i++) {
                key.append(result.get(resultMapVo.getGroupByList().get(i)));
                if (i < resultMapVo.getGroupByList().size() - 1) {
                    key.append("-");
                }
            }
        } else if (resultMapVo.getPropertyList() != null && resultMapVo.getPropertyList().size() > 0) {
            for (int i = 0; i < resultMapVo.getPropertyList().size(); i++) {
                key.append(result.get(resultMapVo.getPropertyList().get(i)));
                if (i < resultMapVo.getPropertyList().size() - 1) {
                    key.append("-");
                }
            }
        }

        //System.out.println(key);
        if (!key.toString().equals("") && checkMap.containsKey(key.toString())) {
            isExists = true;
        }
        resultMapVo.setKey(key.toString());
        return isExists;
    }

    private List<Map<String, Object>> resultMapRecursion(ResultMapVo resultMapVo, List<Map<String, Object>> resultList,
                                                         Map<String, Object> result, Map<String, Map<String, Object>> checkMap) {
        boolean isExists = isExists(resultMapVo, resultList, result, checkMap);
        String key = resultMapVo.getKey();
        Map<String, Object> newResult = null;
        if (!isExists) {
            newResult = new HashMap<>();
            newResult.put("UUID", key);
            checkMap.put(key, newResult);
            boolean isAllColumnEmpty = true;
            for (String str : resultMapVo.getPropertyList()) {
                boolean needReadFile = false, needEncode = false;
                String tmp = str;
                if (str.contains("CONTENT_PATH:")) {// 读取文件内容
                    str = str.replace("CONTENT_PATH:", "");
                    needReadFile = true;
                }
                if (str.contains("ENCODE_HTML:")) {// 转义
                    str = str.replace("ENCODE_HTML:", "");
                    needEncode = true;
                }
                // FIXME 读取文件内容的字段需要补充实现，建议改成策略模式，不要用if else
                if (!needReadFile) {
                    if (needEncode) {
                        // newResult.put(str, encodeHtml(HtmlUtil.clearStringHTML((result.get(tmp) ==
                        // null ? "" : result.get(tmp).toString()))));
                    } else {
                        newResult.put(str, result.get(tmp));
                    }
                } else {
                    if (needEncode) {
                        // newResult.put(str,
                        // encodeHtml(Toolkit.clearStringHTML((FileWorker.readContent(result.get(tmp) ==
                        // null ? "" : result.get(tmp).toString())))));
                    } else {
                        // newResult.put(str,
                        // Toolkit.clearStringHTML(FileWorker.readContent(result.get(tmp) == null ? "" :
                        // result.get(tmp).toString())));
                    }
                }
                if (result.get(tmp) != null) {
                    isAllColumnEmpty = false;
                }
            }
            if (resultMapVo.getResultMap() != null) {
                for (Map.Entry<String, ResultMapVo> entry : resultMapVo.getResultMap().entrySet()) {
                    Map<String, Map<String, Object>> subCheckMap = new HashMap<>();
                    newResult.put("CHECKMAP-" + entry.getKey(), subCheckMap);
                    newResult.put(entry.getKey(), resultMapRecursion(entry.getValue(),
                            new ArrayList<>(), result, subCheckMap));
                }
            }
            if (!isAllColumnEmpty) {
                resultList.add(newResult);
            }
            resultMapVo.setIndex(resultMapVo.getIndex() + 1);
        } else {
            newResult = checkMap.get(key);
            if (resultMapVo.getResultMap() != null) {
                for (Map.Entry<String, ResultMapVo> entry : resultMapVo.getResultMap().entrySet()) {
                    resultMapRecursion(entry.getValue(), (List<Map<String, Object>>) newResult.get(entry.getKey()),
                            result, (Map<String, Map<String, Object>>) newResult.get("CHECKMAP-" + entry.getKey()));
                }
            }
        }
        return resultList;
    }

    public int deleteReportById(Long reportId) {
        reportMapper.deleteReportParamByReportId(reportId);
        reportMapper.deleteReportAuthByReportId(reportId);
        reportMapper.deleteReportById(reportId);
        return 1;
    }

    @Override
    public List<SqlInfo> getTableList(String content) {
        List<SqlInfo> sqlInfoList = new ArrayList<>();
        if (StringUtils.isBlank(content)) {
            return sqlInfoList;
        }
        Matcher matcher = pattern.matcher(content);
        while(matcher.find()) {
            String e = matcher.group();
            String tableId = getFieldValue(e, "data");
            if (StringUtils.isBlank(tableId)) {
                tableId = getFieldValue(e, "\"data\"");
                if (StringUtils.isBlank(tableId)) {
                    continue;
                }
            }
            SqlInfo sqlInfo = new SqlInfo();
            sqlInfo.setId(tableId);
            sqlInfo.setTableContent(e);
            sqlInfoList.add(sqlInfo);
            String needPage = getFieldValue(e, "needPage");
            if (StringUtils.isBlank(needPage)) {
                needPage = getFieldValue(e, "\"needPage\"");
            }
            if ("true".equalsIgnoreCase(needPage)) {
                sqlInfo.setNeedPage(true);
            }
            String pageSize = getFieldValue(e, "pageSize");
            if (StringUtils.isBlank(pageSize)) {
                pageSize = getFieldValue(e, "\"pageSize\"");
            }
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

    @Override
    public Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap) {
        return getQuerySqlResult(reportVo, paramMap, showColumnsMap, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap, List<SqlInfo> tableList) {
        return getQuerySqlResult(reportVo, paramMap, showColumnsMap, new ArrayList<>(), tableList);
    }

    @Override
    public Map<String, Object> getQuerySqlResult(ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap, List<String> needExecuteSqlIdList, List<SqlInfo> tableList) {
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtils.isBlank(reportVo.getSql())) {
            return resultMap;
        }
        SqlRunner sqlRunner = new SqlRunner(reportVo.getSql(), "reportId_" + reportVo.getId());
        List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(paramMap);
        for (SqlInfo sqlInfo : sqlInfoList) {
            for (SqlInfo needPageTable : tableList) {
                if (Objects.equals(sqlInfo.getId(), needPageTable.getId())) {
                    sqlInfo.setNeedPage(needPageTable.getNeedPage());
                    sqlInfo.setPageSize(needPageTable.getPageSize());
                }
            }
        }
        Map<String, Object> pageMap = new HashMap<>();
        Map<String, Long> timeMap = new HashMap<>();
        for (SqlInfo sqlInfo : sqlInfoList) {
            // 如果SQL设置了延迟加载，第一次访问时不主动获取数据
//            if (isFirst) {
//                continue;
//            }
            if (CollectionUtils.isEmpty(needExecuteSqlIdList) || needExecuteSqlIdList.contains(sqlInfo.getId())) {
                long sqlTimeStart = System.currentTimeMillis();
                if (sqlInfo.getNeedPage()) {
                    BasePageVo basePageVo = new BasePageVo();
                    Integer currentPage = paramMap.getInteger("currentPage");
                    if (currentPage != null) {
                        basePageVo.setCurrentPage(currentPage);
                    }
                    basePageVo.setPageSize(sqlInfo.getPageSize());
                    PageRowBounds rowBounds = new PageRowBounds(basePageVo.getStartNum(), basePageVo.getPageSize());
//                    List list = sqlRunner.runSqlById(sqlInfo, paramMap, rowBounds);
                    List list = runSql(sqlRunner, sqlInfo, paramMap, rowBounds, showColumnsMap);
                    if (CollectionUtils.isNotEmpty(list)) {
                        resultMap.put(sqlInfo.getId(), list);
                        timeMap.put("SQL_" + sqlInfo.getId() + "_SIZE", (long) list.size());
                    }
                    basePageVo.setRowNum(rowBounds.getRowNum());
                    JSONObject pageObj = new JSONObject();
                    pageObj.put("rowNum", basePageVo.getRowNum());
                    pageObj.put("currentPage", basePageVo.getCurrentPage());
                    pageObj.put("pageSize", basePageVo.getPageSize());
                    pageObj.put("pageCount", basePageVo.getPageCount());
                    pageObj.put("tableId", sqlInfo.getId());
                    pageMap.put(sqlInfo.getId(), pageObj);
                } else {
//                    List list = sqlRunner.runSqlById(sqlInfo, paramMap);
                    List list = runSql(sqlRunner, sqlInfo, paramMap, null, showColumnsMap);
                    if (CollectionUtils.isNotEmpty(list)) {
                        resultMap.put(sqlInfo.getId(), list);
                        timeMap.put("SQL_" + sqlInfo.getId() + "_SIZE", (long) list.size());
                    }
                }
                timeMap.put("SQL_" + sqlInfo.getId(), System.currentTimeMillis() - sqlTimeStart);
            }
        }
        resultMap.put(ReportConfig.REPORT_PAGE_MAP_KEY, pageMap);
        resultMap.put(ReportConfig.REPORT_TIME_MAP_KEY, timeMap);
        return resultMap;
    }

    private List<Map<String, Object>> runSql(SqlRunner sqlRunner, SqlInfo sqlInfo, JSONObject paramMap, PageRowBounds rowBounds, Map<String, List<String>> showColumnsMap) {
        List list = null;
        if (rowBounds != null) {
            list = sqlRunner.runSqlById(sqlInfo, paramMap, rowBounds);
        } else {
            list = sqlRunner.runSqlById(sqlInfo, paramMap);
        }
        List<String> propertyList = sqlInfo.getPropertyList();
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map<?, ?> map) {
                Map<String, Object> hashMap = new LinkedHashMap<>();
                if (CollectionUtils.isNotEmpty(propertyList)) {
                    for (String property : propertyList) {
                        hashMap.put(property, map.get(property));
                    }
                } else {
                    for (Map.Entry<?, ?> entity : map.entrySet()) {
                        hashMap.put((String) entity.getKey(), entity.getValue());
                    }
                }
                resultList.add(hashMap);
            }
        }
        /* 如果存在表格且存在表格显示列的配置，则筛选显示列并排序
           showColumnMap:key->表格ID;value->配置的表格显示列
        */
        if (MapUtils.isNotEmpty(showColumnsMap)) {
            List<String> showColumnList = showColumnsMap.get(sqlInfo.getId());
            if (showColumnList != null) {
                List<Map<String, Object>> sqList = selectTableColumns(showColumnList, resultList);
                resultList = sqList;
            }
        }
        return resultList;
    }

    @Override
    public Map<String, Object> getQuerySqlResultById(String id, ReportVo reportVo, JSONObject paramMap, Map<String, List<String>> showColumnsMap) {
        Map<String, Object> resultMap = new HashMap<>();
        SqlRunner sqlRunner = new SqlRunner(reportVo.getSql(), "reportId_" + reportVo.getId());
        List<SqlInfo> sqlInfoList = sqlRunner.getAllSqlInfoList(paramMap);
        Map<String, Object> pageMap = new HashMap<>();
        Map<String, Long> timeMap = new HashMap<>();
        for (SqlInfo sqlInfo : sqlInfoList) {
            if (Objects.equals(sqlInfo.getId(), id)) {
                long sqlTimeStart = System.currentTimeMillis();
                BasePageVo basePageVo = new BasePageVo();
                Integer currentPage = paramMap.getInteger("currentPage");
                if (currentPage != null) {
                    basePageVo.setCurrentPage(currentPage);
                }
                Integer pageSize = paramMap.getInteger("pageSize");
                if (pageSize != null) {
                    basePageVo.setPageSize(pageSize);
                }
                PageRowBounds rowBounds = new PageRowBounds(basePageVo.getStartNum(), basePageVo.getPageSize());
                List list = sqlRunner.runSqlById(sqlInfo, paramMap, rowBounds);
                if (CollectionUtils.isNotEmpty(list)) {
                    resultMap.put(sqlInfo.getId(), list);
                    timeMap.put("SQL_" + sqlInfo.getId() + "_SIZE", (long) list.size());
                }
                basePageVo.setRowNum(rowBounds.getRowNum());
                JSONObject pageObj = new JSONObject();
                pageObj.put("rowNum", basePageVo.getRowNum());
                pageObj.put("currentPage", basePageVo.getCurrentPage());
                pageObj.put("pageSize", basePageVo.getPageSize());
                pageObj.put("pageCount", basePageVo.getPageCount());
                pageObj.put("tableId", sqlInfo.getId());
                pageMap.put(sqlInfo.getId(), pageObj);
                timeMap.put("SQL_" + sqlInfo.getId(), System.currentTimeMillis() - sqlTimeStart);
            }
        }
        resultMap.put(ReportConfig.REPORT_PAGE_MAP_KEY, pageMap);
        Object object = resultMap.get(id);
        if (object == null) {
            return resultMap;
        }
        List<String> propertyList = new ArrayList<>();
        for (SqlInfo sqlInfo : sqlInfoList) {
            if (Objects.equals(sqlInfo.getId(), id)) {
                propertyList = sqlInfo.getPropertyList();
                break;
            }
        }
        if (object instanceof List) {
            List<Map<String, Object>> resultList = new ArrayList<>();
            List list = (List) object;
            for (Object obj : list) {
                if (obj instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) obj;
                    Map<String, Object> hashMap = new LinkedHashMap<>();
                    if (CollectionUtils.isNotEmpty(propertyList)) {
                        for (String property : propertyList) {
                            Object value = map.get(property);
                            if (value == null) {
                                value = "null";
                            }
                            hashMap.put(property, value);
                        }
                    } else {
                        for (Map.Entry<?, ?> entity : map.entrySet()) {
                            hashMap.put((String) entity.getKey(), entity.getValue());
                        }
                    }
                    resultList.add(hashMap);
                }
            }
                /* 如果存在表格且存在表格显示列的配置，则筛选显示列并排序
                   showColumnMap:key->表格ID;value->配置的表格显示列
                */
            if (MapUtils.isNotEmpty(showColumnsMap)) {
                List<String> showColumnList = showColumnsMap.get(id);
                if (showColumnList != null) {
                    List<Map<String, Object>> sqList = selectTableColumns(showColumnList, resultList);
                    resultList = sqList;
                }
            }
            resultMap.put(id, resultList);
            timeMap.put("SQL_" + id + "_SIZE", (long) resultList.size());
        }
        resultMap.put(ReportConfig.REPORT_TIME_MAP_KEY, timeMap);
        return resultMap;
    }

    @Override
    public void validateReportParamList(List<ReportParamVo> paramList) {
        if (CollectionUtils.isNotEmpty(paramList)) {
            Set<String> keySet = new HashSet<>();
            for (int i = 0; i < paramList.size(); i++) {
                ReportParamVo paramVo = paramList.get(i);
                String key = paramVo.getName();
                if (StringUtils.isBlank(key)) {
                    throw new ReportParamNameRepeatsException(i);
                }
                if (keySet.contains(key)) {
                    throw new ReportParamNameRepeatsException(i);
                } else {
                    keySet.add(key);
                }
            }
        }
    }

    @Override
    public Workbook getReportWorkbook(String content) {
        Map<String, List<Map<String, Object>>> tableMap = getTableListByHtml(content);
        if (MapUtils.isNotEmpty(tableMap)) {
            ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class);
            for (Map.Entry<String, List<Map<String, Object>>> entry : tableMap.entrySet()) {
                String tableName = entry.getKey().trim();
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
            return builder.build();
        } else {
            // 考虑报表内容配置有自定义表格
            return getReportWorkbookByTemplateTable(content);
        }
    }

    /**
     * 带有tableName属性的table标签才会被识别为表格
     * table标签遵守严格的DOM规范
     * e.g:
     * <table tableName="按月统计">
     *     <thead>
     *         <tr>
     *             <th>月</th>
     *             <th>工单数量</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr>
     *             <td>2022-06</td>
     *             <td>22</td>
     *         </tr>
     *         <tr>
     *             <td>2022-05</td>
     *             <td>26</td>
     *         </tr>
     *         <tr>
     *             <td>2022-04</td>
     *             <td>3</td>
     *         </tr>
     *     </tbody>
     * </table>
     *
     * @param content
     * @return
     */
    private Map<String, List<Map<String, Object>>> getTableListByHtml(String content) {
        Map<String, List<Map<String, Object>>> tableMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(content)) {
            Document doc = Jsoup.parse(content);
            /** 抽取所有带有tableName属性的元素 */
            Elements elements = doc.getElementsByAttribute("tableName");
            if (CollectionUtils.isNotEmpty(elements)) {
                for (Element element : elements) {
                    String tableName = element.attr("tableName");
                    if (StringUtils.isNotBlank(tableName)) {
                        Elements ths = element.select("[tableName]>thead>tr>th");
                        Elements tbodys = element.select("[tableName]>tbody");
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
                                if (tableMap.containsKey(tableName)) {
                                    // 存在同名表格，增加空格区分存进Map
                                    do {
                                        tableName += " ";
                                    } while (tableMap.containsKey(tableName));
                                }
                                tableMap.put(tableName, valueList);
                            }
                        }
                    }
                }
            }
        }
        return tableMap;
    }

    /**
     * 解析内容配置里的不规范表格
     * 循环填充方式生成excel，兼容存在rowspan、colspan的表格
     *
     * @param content
     * @return
     */
    private Workbook getReportWorkbookByTemplateTable(String content) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        if (StringUtils.isNotBlank(content)) {
            Document doc = Jsoup.parse(content);
            Elements tableElements = doc.getElementsByTag("table");
            if (CollectionUtils.isEmpty(tableElements)) {
                // 没有table标签
                throw new TableNotFoundInReportException();
            }

            // 表头样式
            CellStyle headStyle = getDefualtHeadCellStyle(workbook);
            // 普通样式
            CellStyle style = getDefualtCellStyle(workbook);
            // 标志表头
            Map<Integer, Set<Integer>> thMap = null;
            // 表内所有单元格数据
            Map<Integer, Map<Integer, String>> rowList = null;
            // 单行单元格数据
            Map<Integer, String> columnList = null;
            // 合并单元格数组
            JSONArray mergeJsonArray = null;
            // 合并单元格对象
            JSONObject mergeJsonObj = null;
            // 行数据列表
            Elements trList = null;
            // 列数据列表
            Elements tdList = null;

            for (Element t : tableElements) {
                thMap = new HashMap<>();
                rowList = new HashMap<>();
                mergeJsonArray = new JSONArray();

                // 遍历表格内所有行
                trList = t.select("tr");
                for (int j = 0; j < trList.size(); j++) {
                    Element r = trList.get(j);
                    // 获取当前起始单元格行下标row, col
                    int rowIndex = getBottomRowIndex(rowList, j);
                    columnList = rowList.computeIfAbsent(rowIndex, k -> new HashMap<>());

                    // 遍历此行内所有列
                    tdList = r.select("th,td");
                    for (int k = 0; k < tdList.size(); k++) {
                        Element element = tdList.get(k);
                        boolean isTh = "th".equals(element.tagName());
                        // 获取当前起始单元格列下标
                        int colIndex = getRightColumnIndex(columnList, 0);

                        // 当前单元格内容
                        String trimStr = null;
                        // Excel 最大的 cell size 为 32767
                        if (element.text().length() >= 32000) {
                            trimStr = element.text().toString().substring(0, 32000);
                        } else {
                            Elements childrenEle = element.children();
                            // <br>标签替换为\n换行符
                            for (Element o : childrenEle) {
                                if ("br".equals(o.tag().normalName())) {
                                    if (trimStr == null) {
                                        trimStr = element.html().trim();
                                    }
                                    trimStr = trimStr.replaceAll("\\s*" + o + "+\\s*", "\n");
                                }
                            }
                        }
                        if (trimStr == null) {
                            trimStr = element.text().trim();
                        }

                        // 合并单元格 填充空值
                        int colspan = 1;
                        int rowspan = 1;
                        if (StringUtils.isNotBlank(element.attr("colspan"))) {
                            colspan = Integer.parseInt(element.attr("colspan").trim());
                        }
                        if (StringUtils.isNotBlank(element.attr("rowspan"))) {
                            rowspan = Integer.parseInt(element.attr("rowspan").trim());
                        }

                        if (colspan > 1 || rowspan > 1) {
                            for (int p = rowIndex; p < rowIndex + rowspan; p++) {
                                Map<Integer, String> nowColumnList = rowList.computeIfAbsent(p, k1 -> new HashMap<>());
                                for (int m = colIndex; m < colIndex + colspan; m++) {
                                    if (p == rowIndex && m == colIndex) {
                                        // 填充当前单元格内容
                                        nowColumnList.put(m, trimStr);
                                    } else {
                                        // 填充空字符串占位
                                        nowColumnList.put(m, "");
                                    }
                                    if (isTh) {
                                        thMap.computeIfAbsent(p, k2 -> new HashSet<>()).add(m);
                                    }
                                }
                            }
                            mergeJsonObj = new JSONObject();
                            mergeJsonObj.put("firstRow", rowIndex);
                            mergeJsonObj.put("lastRow", rowIndex + rowspan - 1);
                            mergeJsonObj.put("firstCol", colIndex);
                            mergeJsonObj.put("lastCol", colIndex + colspan - 1);
                            mergeJsonArray.add(mergeJsonObj);
                        } else {
                            columnList.put(colIndex, trimStr);
                            if (isTh) {
                                thMap.computeIfAbsent(rowIndex, k2 -> new HashSet<>()).add(colIndex);
                            }
                        }
                    }
                }

                // 生成sheet及填充数据
                SXSSFSheet sheet = workbook.createSheet();
                for (int i = 0; i < rowList.size(); i++) {
                    Map<Integer, String> tr = rowList.get(i);
                    SXSSFRow row = sheet.createRow(i);
                    for (int j = 0; j < tr.size(); j++) {
                        if (i == 0) {
                            sheet.setColumnWidth(j, 30 * 256);
                        }
                        String cellValue = tr.get(j);
                        SXSSFCell cell = row.createCell((short) j);
                        if (thMap.containsKey(i) && thMap.get(i).contains(j)) {
                            cell.setCellStyle(headStyle);
                        } else {
                            cell.setCellStyle(style);
                        }
                        cell.setCellValue(cellValue);
                    }
                }
                for (int p = 0; p < mergeJsonArray.size(); p++) {
                    mergeJsonObj = mergeJsonArray.getJSONObject(p);
                    int firstRow = mergeJsonObj.getInteger("firstRow");
                    int lastRow = mergeJsonObj.getInteger("lastRow");
                    int firstCol = mergeJsonObj.getInteger("firstCol");
                    int lastCol = mergeJsonObj.getInteger("lastCol");
                    sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
                }

            }
        }
        return workbook;
    }

    private CellStyle getDefualtCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setTopBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle getDefualtHeadCellStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.DARK_BLUE.getIndex());

        Font font = wb.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFont(font);
        style.setBottomBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setTopBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setLeftBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        style.setRightBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private int getRightColumnIndex(Map<Integer, String> columnList, int k) {
        int columnNum;
        if (!columnList.containsKey(k)) {
            columnNum = k;
        } else {
            columnNum = getRightColumnIndex(columnList, ++k);
        }
        return columnNum;
    }

    private int getBottomRowIndex(Map<Integer, Map<Integer, String>> rowList, int j) {
        if (j == 0) {
            return 0;
        }
        int length = rowList.get(j - 1).size();
        int row = j;
        int col = 0;
        while (rowList.containsKey(row) && rowList.get(row).get(col) != null) {
            if (col == length - 1) {
                row++;
                col = 0;
            } else {
                col++;
            }
        }
        return row;
    }
}
