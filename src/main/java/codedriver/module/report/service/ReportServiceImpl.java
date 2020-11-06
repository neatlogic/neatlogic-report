package codedriver.module.report.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.dao.mapper.ReportMapper;
import codedriver.module.report.dto.ReportAuthVo;
import codedriver.module.report.dto.ReportVo;
import codedriver.module.report.dto.RestVo;
import codedriver.module.report.dto.ResultMapVo;
import codedriver.module.report.dto.SelectVo;
import codedriver.module.report.util.ReportXmlUtil;
import codedriver.module.report.util.RestUtil;

@Service
public class ReportServiceImpl implements ReportService {
    Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
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
        String result = RestUtil.sendRequest(restVo);
        try {
            return JSON.parse(result);
        } catch (Exception ex) {
            return result;
        }
    }

    /**
     * 返回所有数据源结果
     */
    public Map<String, Object> getQueryResult(Long reportId, JSONObject paramMap, Map<String, Long> timeMap,
        boolean isFirst) throws Exception {
        ReportVo reportConfig = getReportDetailById(reportId);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, Object> returnResultMap = new HashMap<String, Object>();
        List<SelectVo> selectList = null;
        List<RestVo> restList = null;
        try {
            Map<String, Object> analyseMap = ReportXmlUtil.analyseSql(reportConfig.getSql(), paramMap);
            restList = (List<RestVo>)analyseMap.get("rest");
            selectList = (List<SelectVo>)analyseMap.get("select");
            for (RestVo rest : restList) {
                if (isFirst && rest.isLazyLoad()) {
                    continue;
                }
                returnResultMap.put(rest.getId(), getRemoteResult(rest));
            }

            for (SelectVo select : selectList) {
                try {
                    conn = getConnection();
                    // 如果SQL设置了延迟加载，第一次访问时不主动获取数据
                    if (isFirst && select.isLazyLoad()) {
                        continue;
                    }
                    String sqlText = select.getSql();
                    stmt = conn.prepareStatement(sqlText);
                    stmt.setQueryTimeout(select.getQueryTimeout());
                    StringBuffer sbParam = new StringBuffer();
                    if (select.getParamList().size() > 0) {
                        sbParam.append("(");
                        for (int p = 0; p < select.getParamList().size(); p++) {
                            if (select.getParamList().get(p) instanceof String) {
                                stmt.setObject(p + 1, select.getParamList().get(p));
                                sbParam.append(select.getParamList().get(p)).append(",");
                            } else {
                                // 数组参数有待处理
                                stmt.setObject(p + 1, ((String[])select.getParamList().get(p))[0]);
                                sbParam.append(((String[])select.getParamList().get(p))[0]).append(",");
                            }
                        }
                        sbParam.deleteCharAt(sbParam.toString().length() - 1);
                        sbParam.append(")");
                    }
                    /**
                     * 新增日志记录
                     */
                    if (logger.isDebugEnabled() || logger.isInfoEnabled()) {
                        logger.debug("REPORT RUN SQL::" + sqlText);
                        logger.debug("REPORT RUN SQL PARAM::" + sbParam.toString());
                    }

                    resultSet = stmt.executeQuery();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int count = metaData.getColumnCount();
                    String[] columns = new String[count];
                    Integer[] columnTypes = new Integer[count];

                    for (int i = 1; i <= count; i++) {
                        columnTypes[i - 1] = metaData.getColumnType(i);
                        columns[i - 1] = metaData.getColumnLabel(i);
                    }

                    List<Map<String, Object>> tmpList = new ArrayList<Map<String, Object>>();
                    Map<String, Map<String, Object>> checkMap = new HashMap<String, Map<String, Object>>();
                    Map<String, List> returnMap = new HashMap<String, List>();
                    while (resultSet.next()) {
                        Map<String, Object> resultMap = new HashMap<String, Object>();
                        for (int i = 1; i <= count; i++) {
                            resultMap.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                        }
                        if (select.getResultType() == SelectVo.RSEULT_TYPE_LIST) {
                            tmpList = resultMapRecursion(select.getResultMap(), tmpList, resultMap, checkMap);
                        } else {
                            returnMap = wrapResultMapToMap(select.getResultMap(), resultMap, returnMap);
                        }
                    }

                    /** select.getId() -> columnList */
                    Map<String,List<String>> showColumnsMap = new HashMap<>();
                    if(select.getId().equals("tableData")){
                        List<String> columnList = new ArrayList<>();
                        columnList.add("上报人");
                        columnList.add("标题");
                        showColumnsMap.put("tableData",columnList);
                    }
                    if(showColumnsMap.containsKey(select.getId())){
                        List<String> showColumnList = showColumnsMap.get(select.getId());
                        /** 筛选表格显示列 */
                        for(Map<String, Object> map : tmpList){
                            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
                            while (iterator.hasNext()){
                                if(!showColumnList.contains(iterator.next().getKey())){
                                    iterator.remove();
                                }
                            }
                        }
                        /** 排序 */
                        List<Map<String, Object>> sqList = new ArrayList<Map<String, Object>>();
                        for(Map<String, Object> map : tmpList){
                            Map<String,Object> map1 = new LinkedHashMap<>();
                            for(String s : showColumnList){
                                map1.put(s,map.get(s));
                            }
                            sqList.add(map1);
                        }

                        tmpList = sqList;
                    }

                    if (select.getResultType() == SelectVo.RSEULT_TYPE_LIST) {
                        returnResultMap.put(select.getId(), tmpList);
                    } else {
                        returnResultMap.put(select.getId(), returnMap);
                    }

                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                    throw e;
                } finally {
                    try {
                        if (resultSet != null)
                            resultSet.close();
                        if (stmt != null)
                            stmt.close();
                        if (conn != null)
                            conn.close();
                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        return returnResultMap;
    }

    private Map<String, List> wrapResultMapToMap(ResultMapVo resultMapVo, Map<String, Object> result,
        Map<String, List> returnMap) {
        String key = "";
        List<Map<String, Object>> resultList = null;
        if (resultMapVo.getGroupByList() != null && resultMapVo.getGroupByList().size() > 0) {
            for (int i = 0; i < resultMapVo.getGroupByList().size(); i++) {
                key += result.get(resultMapVo.getGroupByList().get(i));
                if (i < resultMapVo.getGroupByList().size() - 1) {
                    key += "-";
                }
            }
        } else {
            return null;
        }
        Map<String, Object> newResult = new HashMap<String, Object>();
        if (!key.equals("") && returnMap.containsKey(key)) {
            resultList = returnMap.get(key);
        } else {
            resultList = new ArrayList<Map<String, Object>>();
            returnMap.put(key, resultList);
        }

        // Iterator<Map.Entry<String, Object>> iter =
        // result.entrySet().iterator();
        for (String str : resultMapVo.getPropertyList()) {
            newResult.put(str, result.get(str));
        }
        resultList.add(newResult);
        return returnMap;
    }

    private List<Map<String, Object>> resultMapRecursion(ResultMapVo resultMapVo, List<Map<String, Object>> resultList,
        Map<String, Object> result, Map<String, Map<String, Object>> checkMap) {
        String key = "";
        if (resultList == null) {
            resultList = new ArrayList<Map<String, Object>>();
        }
        if (resultMapVo.getGroupByList() != null && resultMapVo.getGroupByList().size() > 0) {
            for (int i = 0; i < resultMapVo.getGroupByList().size(); i++) {
                key += result.get(resultMapVo.getGroupByList().get(i));
                if (i < resultMapVo.getGroupByList().size() - 1) {
                    key += "-";
                }
            }
        } else if (resultMapVo.getPropertyList() != null && resultMapVo.getPropertyList().size() > 0) {
            for (int i = 0; i < resultMapVo.getPropertyList().size(); i++) {
                key += result.get(resultMapVo.getPropertyList().get(i));
                if (i < resultMapVo.getPropertyList().size() - 1) {
                    key += "-";
                }
            }
        }
        boolean isExists = false;
        Map<String, Object> newResult = null;
        if (!key.equals("") && checkMap.containsKey(key)) {
            isExists = true;
            newResult = checkMap.get(key);
        }

        if (!isExists) {
            Iterator<Map.Entry<String, Object>> iter = result.entrySet().iterator();
            newResult = new HashMap<String, Object>();
            newResult.put("UUID", key);
            checkMap.put(key, newResult);
            boolean isAllColumnEmpty = true;
            for (String str : resultMapVo.getPropertyList()) {
                boolean needReadFile = false, needEncode = false;
                String tmp = str;
                if (str.indexOf("CONTENT_PATH:") > -1) {// 读取文件内容
                    str = str.replace("CONTENT_PATH:", "");
                    needReadFile = true;
                }
                if (str.indexOf("ENCODE_HTML:") > -1) {// 转义
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
                Iterator<Map.Entry<String, ResultMapVo>> resultIter = resultMapVo.getResultMap().entrySet().iterator();
                while (resultIter.hasNext()) {
                    Map.Entry<String, ResultMapVo> entry = resultIter.next();
                    Map<String, Map<String, Object>> subCheckMap = new HashMap<String, Map<String, Object>>();
                    newResult.put("CHECKMAP-" + entry.getKey(), subCheckMap);
                    newResult.put(entry.getKey(), resultMapRecursion(entry.getValue(),
                        new ArrayList<Map<String, Object>>(), result, subCheckMap));
                }
            }
            if (!isAllColumnEmpty) {
                resultList.add(newResult);
            }
        } else {
            if (resultMapVo.getResultMap() != null) {
                Iterator<Map.Entry<String, ResultMapVo>> resultIter = resultMapVo.getResultMap().entrySet().iterator();
                while (resultIter.hasNext()) {
                    Map.Entry<String, ResultMapVo> entry = resultIter.next();
                    resultMapRecursion(entry.getValue(), (List<Map<String, Object>>)newResult.get(entry.getKey()),
                        result, (Map<String, Map<String, Object>>)newResult.get("CHECKMAP-" + entry.getKey()));
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

    public static String encodeHtml(String str) {
        if (str != null && !"".equals(str)) {
            // str = str.replace("&", "&amp;");
            str = str.replace("<", "&lt;");
            str = str.replace(">", "&gt;");
            str = str.replace("'", "&#39;");
            str = str.replace("\"", "&quot;");
            return str;
        }
        return "";
    }
    // new

}
