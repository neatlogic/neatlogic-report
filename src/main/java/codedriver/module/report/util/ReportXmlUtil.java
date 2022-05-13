/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.util;

import codedriver.framework.dto.RestVo;
import codedriver.framework.util.HtmlUtil;
import codedriver.module.report.dto.ResultMapVo;
import codedriver.module.report.dto.SelectVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportXmlUtil {
    //static Logger logger = LoggerFactory.getLogger(ReportXmlUtil.class);


    private static ResultMapVo analyseResultMap(Element element) {
        ResultMapVo resultMapVo = new ResultMapVo();
        String resultMapId = element.attributeValue("id");
        List<Element> resultNodeList = element.elements();
        resultMapVo.setId(resultMapId);
        for (Element result : resultNodeList) {
            if (result.getName().equals("id")) {
                resultMapVo.addGroupBy(result.attributeValue("property"));
                resultMapVo.addProperty(result.attributeValue("property"));
            } else if (result.getName().equals("result")) {
                resultMapVo.addProperty(result.attributeValue("property"));
            } else if (result.getName().equals("collection")) {
                Map<String, ResultMapVo> map = resultMapVo.getResultMap();
                if (map == null) {
                    map = new HashMap<>();
                }
                map.put(result.attributeValue("property"), analyseResultMap(result));
                resultMapVo.setResultMap(map);
            }
        }
        return resultMapVo;
    }

    public static Map<String, Object> analyseSql(String xml, Map<String, Object> paramMap) throws DocumentException, UnsupportedEncodingException {
        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        List<Element> resultMapNodeList = root.elements("resultMap");
        List<Element> sqlNodeList = root.elements("select");
        List<Element> restNodeList = root.elements("rest");
        String regex_param = "#\\{([^}]+?)}";
        String regex_dollarparam = "\\$\\{([^}]+?)}";
        String[] replace_regex = new String[]{"<[/]?if[^>]*?>", "<[/]?rest[^>]*?>", "<[/]?select[^>]*?>", "<[/]?forEach[^>]*?>", "<[/]?ifNotNull[^>]*?>", "<[/]?ifNull[^>]*?>", "<[/]?forEach[^>]*?>", "\\<\\!\\[CDATA\\[", "\\]\\]\\>"};
        Map<String, Object> returnMap = new HashMap<>();
        List<SelectVo> selectList = new ArrayList<>();
        List<RestVo> restList = new ArrayList<>();
        returnMap.put("select", selectList);
        returnMap.put("rest", restList);
        Map<String, ResultMapVo> resultMap = new HashMap<>();
        Map<String, Integer> resultTypeMap = new HashMap<>();

        if (restNodeList != null && restNodeList.size() > 0) {
            for (Element element : restNodeList) {
                String url = element.attributeValue("url");
                String authType = element.attributeValue("authtype") == null ? "" : element.attributeValue("authtype");
                boolean lazyLoad = element.attributeValue("lazyload") != null && Boolean.parseBoolean(element.attributeValue("lazyload"));
                String userName = element.attributeValue("username") == null ? "" : element.attributeValue("username");
                String password = element.attributeValue("password") == null ? "" : element.attributeValue("password");
                int readTimeOut = element.attributeValue("timeout") == null ? 60000 : Integer.parseInt(element.attributeValue("timeout"));
                RestVo.Builder restBuilder = new RestVo.Builder(url, authType).setLazyLoad(lazyLoad).setUsername(userName).setPassword(password).setId(element.attributeValue("id")).setReadTimeout(readTimeOut);
                List<Element> ifElList = element.elements("if");
                if (ifElList != null && ifElList.size() > 0) {
                    List<Element> removeObj = new ArrayList<>();
                    for (Element ifEl : ifElList) {
                        String testExp = ifEl.attributeValue("test");
                        if (!ReportFreemarkerUtil.evaluateExpression(testExp, paramMap)) {
                            removeObj.add(ifEl);
                        }
                    }
                    for (Element i : removeObj) {
                        ifElList.remove(i);
                    }
                }

                List<Element> notNullElList = element.elements("ifNotNull");
                if (notNullElList != null && notNullElList.size() > 0) {
                    List<Element> removeObj = new ArrayList<>();
                    for (Element notNullEl : notNullElList) {
                        boolean hasParam = false;
                        Object p = paramMap.get(notNullEl.attributeValue("parameter"));
                        if (p != null && (!StringUtils.equals(StringUtils.EMPTY, p.toString()))) {
                            hasParam = true;
                        }
                        if (!hasParam) {
                            removeObj.add(notNullEl);
                        }
                    }
                    for (Element i : removeObj) {
                        notNullElList.remove(i);
                    }
                }
                List<Element> nullElList = element.elements("ifNull");
                if (nullElList != null && nullElList.size() > 0) {
                    List<Element> removeObj = new ArrayList<>();
                    for (Element nullEl : nullElList) {
                        boolean isNull = false;
                        Object p = paramMap.get(nullEl.attributeValue("parameter"));
                        if (p == null) {
                            isNull = true;
                        }
                        if (!isNull) {
                            removeObj.add(nullEl);
                        }
                    }
                    for (Element i : removeObj) {
                        nullElList.remove(i);
                    }
                }
                List<Element> forEachList = element.elements("forEach");
                if (forEachList != null && forEachList.size() > 0) {
                    List<Element> removeObj = new ArrayList<>();
                    for (Element foreachEl : forEachList) {
                        boolean hasParam = false;
                        Object p = paramMap.get(foreachEl.attributeValue("parameter"));
                        if (p != null && (!StringUtils.equals(StringUtils.EMPTY, p.toString()))) {
                            hasParam = true;
                            String separator = foreachEl.attributeValue("separator");
                            String orgText = foreachEl.getText();
                            StringBuilder newText = new StringBuilder();
                            if (p instanceof String) {
                                newText = new StringBuilder(orgText);
                            } else if (p instanceof String[]) {
                                for (int pi = 0; pi < ((String[]) p).length; pi++) {
                                    newText.append(orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}", "#{" + foreachEl.attributeValue("parameter") + "#" + pi + "}"));
                                    if (pi < ((String[]) p).length - 1) {
                                        newText.append(separator);
                                    }
                                }
                            }
                            foreachEl.setText(newText.toString());
                        }
                        if (!hasParam) {
                            removeObj.add(foreachEl);
                        }
                    }
                    for (Element i : removeObj) {
                        forEachList.remove(i);
                    }
                }
                String result = element.asXML();
                result = HtmlUtil.decodeHtml(result);
                StringBuffer temp;
                Pattern pattern;
                Matcher matcher;
                for (String regex : replace_regex) {
                    temp = new StringBuffer();
                    pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(result);
                    while (matcher.find()) {
                        matcher.appendReplacement(temp, "");
                    }
                    matcher.appendTail(temp);
                    result = temp.toString();
                }

                temp = new StringBuffer();
                pattern = Pattern.compile(regex_param, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(result);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.contains("#")) {
                        key = key.substring(0, key.lastIndexOf("#"));
                    }
                    Object pp = paramMap.get(key);
                    if (pp instanceof String) {
                        matcher.appendReplacement(temp, URLEncoder.encode(pp.toString(), "UTF-8"));
                    } else if (pp instanceof String[]) {
                        if (matcher.group(1).contains("#")) {
                            int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                            String[] ps = (String[]) pp;
                            if (ps[s] != null) {
                                matcher.appendReplacement(temp, URLEncoder.encode(ps[s], "UTF-8"));
                            }
                        } else {
                            matcher.appendReplacement(temp, URLEncoder.encode(((String[]) pp)[0], "UTF-8"));
                        }
                    } else if (pp instanceof List<?>) {
                        if (matcher.group(1).contains("#")) {
                            int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                            List<String> ps = (List<String>) pp;
                            if (ps.get(s) != null) {
                                matcher.appendReplacement(temp, URLEncoder.encode(ps.get(s), "UTF-8"));
                            }
                        } else {
                            String value = pp.toString();
                            matcher.appendReplacement(temp, URLEncoder.encode((value), "UTF-8"));
                        }
                    } else {
                        // continue rest;
                        matcher.appendReplacement(temp, "");
                    }
                }
                matcher.appendTail(temp);
                JSONObject payload = JSONObject.parseObject(temp.toString());
                restBuilder.setPayload(payload);
                restList.add(restBuilder.build());
            }
        }

        for (Element element : resultMapNodeList) {
            resultMap.put(element.attributeValue("id"), analyseResultMap(element));
            if (element.attributeValue("resultType") != null && element.attributeValue("resultType").equals("Map")) {
                resultTypeMap.put(element.attributeValue("id"), 1);
            } else {
                resultTypeMap.put(element.attributeValue("id"), 0);
            }
        }

        sql:
        for (Element element : sqlNodeList) {
            if (resultMap.get(element.attributeValue("resultMap")) == null) {// 如果没有resultMap,跳出循环
                continue;
            }
            SelectVo selectVo = new SelectVo();
            String sqlId = element.attributeValue("id");
            String lazyLoad = element.attributeValue("lazyload") == null ? "false" : element.attributeValue("lazyload");
            Integer datasource = element.attributeValue("datasource") == null ? 0 : Integer.parseInt(element.attributeValue("datasource"));
            int queryTimeout = element.attributeValue("timeout") == null ? 30 : Integer.parseInt(element.attributeValue("timeout"));
            String needPage = element.attributeValue("needPage");
            String pageSize = element.attributeValue("pageSize");
            selectVo.setId(sqlId);
            selectVo.setQueryTimeout(queryTimeout);
            selectVo.setLazyLoad(Boolean.parseBoolean(lazyLoad));
            selectVo.setDatasource(datasource);
            selectVo.setNeedPage(needPage);
            selectVo.setPageSize(pageSize);
            List<Element> ifElList = element.elements("if");
            if (ifElList != null && ifElList.size() > 0) {
                List<Element> removeObj = new ArrayList<>();
                for (Element ifEl : ifElList) {
                    String testExp = ifEl.attributeValue("test");
                    if (!ReportFreemarkerUtil.evaluateExpression(testExp, paramMap)) {
                        removeObj.add(ifEl);
                    }
                }
                for (Element i : removeObj) {
                    ifElList.remove(i);
                }
            }
            List<Element> notNullElList = element.elements("ifNotNull");
            if (notNullElList != null && notNullElList.size() > 0) {
                List<Element> removeObj = new ArrayList<Element>();
                for (Element notNullEl : notNullElList) {
                    boolean hasParam = false;
                    Object p = paramMap.get(notNullEl.attributeValue("parameter"));
                    if (p != null && (!StringUtils.equals(StringUtils.EMPTY, p.toString()))) {
                        hasParam = true;
                    }
                    if (!hasParam) {
                        removeObj.add(notNullEl);
                    }
                }
                for (Element i : removeObj) {
                    notNullElList.remove(i);
                }
            }
            List<Element> nullElList = element.elements("ifNull");
            if (nullElList != null && nullElList.size() > 0) {
                List<Element> removeObj = new ArrayList<Element>();
                for (Element nullEl : nullElList) {
                    boolean isNull = false;
                    Object p = paramMap.get(nullEl.attributeValue("parameter"));
                    if (p == null) {
                        isNull = true;
                    }
                    if (!isNull) {
                        removeObj.add(nullEl);
                    }
                }
                for (Element i : removeObj) {
                    nullElList.remove(i);
                }
            }
            // List<Element> forEachList = element.elements("forEach");
            List<Node> forEachList = element.selectNodes("//forEach");
            if (forEachList != null && forEachList.size() > 0) {
                List<Element> removeObj = new ArrayList<>();
                for (Node node : forEachList) {
                    Element foreachEl = (Element) node;
                    boolean hasParam = false;
                    Object p = paramMap.get(foreachEl.attributeValue("parameter"));
                    if (p != null && (!StringUtils.equals(StringUtils.EMPTY, p.toString()))) {
                        hasParam = true;
                        String separator = foreachEl.attributeValue("separator");
                        String orgText = foreachEl.getText();
                        StringBuilder newText = new StringBuilder();
                        if (p instanceof String) {
                            newText = new StringBuilder(orgText);
                        } else if (p instanceof String[]) {
                            for (int pi = 0; pi < ((String[]) p).length; pi++) {
                                newText.append(orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}", "#{" + foreachEl.attributeValue("parameter") + "#" + pi + "}"));
                                if (pi < ((String[]) p).length - 1) {
                                    newText.append(separator);
                                }
                            }
                        } else if (p instanceof List<?>) {
                            int i = 0;
                            for (String pi : (List<String>) p) {
                                newText.append(orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}", "#{" + foreachEl.attributeValue("parameter") + "#" + i + "}"));
                                if (i < ((List<String>) p).size() - 1) {
                                    newText.append(separator);
                                }
                                i++;
                            }
                        }
                        foreachEl.setText(newText.toString());
                    }
                    if (!hasParam) {
                        removeObj.add(foreachEl);
                    }
                }
                for (Element i : removeObj) {
                    forEachList.remove(i);
                }
            }
            String result = element.asXML();
            StringBuffer temp;
            Pattern pattern;
            Matcher matcher;
            for (String regex : replace_regex) {
                temp = new StringBuffer();
                pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(result);
                while (matcher.find()) {
                    matcher.appendReplacement(temp, "");
                }
                matcher.appendTail(temp);
                result = temp.toString();
            }

            temp = new StringBuffer();
            pattern = Pattern.compile(regex_param, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(result);
            List<Object> paramList = new ArrayList<Object>();
            while (matcher.find()) {
                String key = matcher.group(1);
                if (key.contains("#")) {
                    key = key.substring(0, key.lastIndexOf("#"));
                }
                Object pp = paramMap.get(key);
                if (pp instanceof String) {
                    matcher.appendReplacement(temp, "?");
                    paramList.add(pp);
                } else if (pp instanceof String[]) {
                    if (matcher.group(1).contains("#")) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                        String[] ps = (String[]) pp;
                        if (ps[s] != null) {
                            paramList.add(ps[s]);
                            matcher.appendReplacement(temp, "?");
                        }
                    } else {
                        matcher.appendReplacement(temp, "?");
                        paramList.add(((String[]) pp)[0]);
                    }
                } else if (pp instanceof List<?>) {
                    if (matcher.group(1).contains("#")) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                        List<String> ps = (List<String>) pp;
                        if (ps.get(s) != null) {
                            paramList.add(ps.get(s));
                            matcher.appendReplacement(temp, "?");
                        }
                    } else {
                        matcher.appendReplacement(temp, "?");
                        paramList.add(((List<String>) pp).get(0));
                    }
                } else {
                    continue sql;
                }
            }
            matcher.appendTail(temp);

            pattern = Pattern.compile(regex_dollarparam, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            result = temp.toString();
            temp = new StringBuffer();
            matcher = pattern.matcher(result);
            while (matcher.find()) {
                String key = matcher.group(1);
                Object pp = paramMap.get(key);
                if (pp instanceof String) {
                    matcher.appendReplacement(temp, (String) pp);
                } else if (pp instanceof String[]) {
                    if (matcher.group(1).contains("$")) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("$") + 1));
                        String[] ps = (String[]) pp;
                        if (ps[s] != null) {
                            matcher.appendReplacement(temp, ps[s]);
                        }
                    } else {
                        matcher.appendReplacement(temp, ((String[]) pp)[0]);
                    }
                } else if (pp instanceof List<?>) {
                    if (matcher.group(1).contains("$")) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("$") + 1));
                        List<String> ps = (List<String>) pp;
                        if (ps.get(s) != null) {
                            matcher.appendReplacement(temp, ps.get(s));
                        }
                    } else {
                        matcher.appendReplacement(temp, ((List<String>) pp).get(0));
                    }
                } else {
                    continue sql;
                }
            }
            matcher.appendTail(temp);

            selectVo.setParamList(paramList);
            // if (temp.toString().toLowerCase().indexOf("limit") == -1) {
            // 修复bug：调用存储过程返回结果时会自动加上limit
            /*
             * if (ReportConfig.REPORT_MAXROW > 0 &&
             * temp.toString().toLowerCase().indexOf("select") > -1) {
             * selectVo.setSql(temp.toString() + " limit " + ReportConfig.REPORT_MAXROW); }
             * else { selectVo.setSql(temp.toString()); }
             */
            // } else {
            selectVo.setSql(temp.toString());
            // }

            selectVo.setSql(selectVo.getSql().replace("&gt;", ">").replace("&lt;", "<"));
            selectVo.setResultMap(resultMap.get(element.attributeValue("resultMap")));
            selectVo.setResultType(resultTypeMap.get(element.attributeValue("resultMap")));
            selectVo.setParamMap(paramMap);
            selectList.add(selectVo);
        }

        return returnMap;
    }

    @SuppressWarnings("unused")
    public static Map<String, Object> analyseSql(String xml) throws DocumentException {
        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        List<Element> resultMapNodeList = root.elements("resultMap");
        List<Element> sqlNodeList = root.elements("select");
        List<Element> restNodeList = root.elements("rest");
        String regex_param = "#\\{([^}]+?)}";
        // String regex_dollarparam = "\\$\\{([^\\}]+?)\\}";
        String[] replace_regex = new String[]{"<[/]?if[^>]*?>", "<[/]?rest[^>]*?>", "<[/]?select[^>]*?>", "<[/]?forEach[^>]*?>", "<[/]?ifNotNull[^>]*?>", "<[/]?ifNull[^>]*?>", "<[/]?forEach[^>]*?>", "\\<\\!\\[CDATA\\[", "\\]\\]\\>"};
        Map<String, Object> returnMap = new HashMap<>();
        List<SelectVo> selectList = new ArrayList<>();
        List<RestVo> restList = new ArrayList<>();
        returnMap.put("select", selectList);
        returnMap.put("rest", restList);
        Map<String, ResultMapVo> resultMap = new HashMap<>();
        Map<String, Integer> resultTypeMap = new HashMap<>();

        if (restNodeList != null && restNodeList.size() > 0) {
            for (Element element : restNodeList) {
                String url = element.attributeValue("url");
                String authType = element.attributeValue("authtype") == null ? "" : element.attributeValue("authtype");
                boolean lazyLoad = element.attributeValue("lazyload") != null && Boolean.parseBoolean(element.attributeValue("lazyload"));
                String userName = element.attributeValue("username") == null ? "" : element.attributeValue("username");
                String password = element.attributeValue("password") == null ? "" : element.attributeValue("password");
                int readTimeOut = element.attributeValue("timeout") == null ? 60000 : Integer.parseInt(element.attributeValue("timeout"));
                RestVo.Builder restBuilder = new RestVo.Builder(url, authType).setLazyLoad(lazyLoad).setUsername(userName).setPassword(password).setId(element.attributeValue("id")).setReadTimeout(readTimeOut);
                String result = element.asXML();
                result = HtmlUtil.decodeHtml(result);
                StringBuffer temp;
                Pattern pattern;
                Matcher matcher;
                for (String regex : replace_regex) {
                    temp = new StringBuffer();
                    pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(result);
                    while (matcher.find()) {
                        matcher.appendReplacement(temp, "");
                    }
                    matcher.appendTail(temp);
                    result = temp.toString();
                }

                temp = new StringBuffer();
                pattern = Pattern.compile(regex_param, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(result);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if (key.contains("#")) {
                        key = key.substring(0, key.lastIndexOf("#"));
                    }
                }
                matcher.appendTail(temp);
                JSONObject payload = JSONObject.parseObject(temp.toString());
                restBuilder.setPayload(payload);
                restList.add(restBuilder.build());
            }
        }

        for (Element element : resultMapNodeList) {
            resultMap.put(element.attributeValue("id"), analyseResultMap(element));
            if (element.attributeValue("resultType") != null && element.attributeValue("resultType").equals("Map")) {
                resultTypeMap.put(element.attributeValue("id"), 1);
            } else {
                resultTypeMap.put(element.attributeValue("id"), 0);
            }
        }

        sql:
        for (Element element : sqlNodeList) {
            if (resultMap.get(element.attributeValue("resultMap")) == null) {// 如果没有resultMap,跳出循环
                continue;
            }
            SelectVo selectVo = new SelectVo();
            String sqlId = element.attributeValue("id");
            String lazyLoad = element.attributeValue("lazyload") == null ? "false" : element.attributeValue("lazyload");
            Integer datasource = element.attributeValue("datasource") == null ? 0 : Integer.parseInt(element.attributeValue("datasource"));
            Integer queryTimeout = element.attributeValue("timeout") == null ? 30 : Integer.parseInt(element.attributeValue("timeout"));
            String needPage = element.attributeValue("needPage");
            String pageSize = element.attributeValue("pageSize");
            selectVo.setId(sqlId);
            selectVo.setQueryTimeout(queryTimeout);
            selectVo.setLazyLoad(Boolean.parseBoolean(lazyLoad));
            selectVo.setDatasource(datasource);
            selectVo.setNeedPage(needPage);
            selectVo.setPageSize(pageSize);
            String result = element.asXML();
            StringBuffer temp;
            Pattern pattern;
            Matcher matcher;
            for (String regex : replace_regex) {
                temp = new StringBuffer();
                pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(result);
                while (matcher.find()) {
                    matcher.appendReplacement(temp, "");
                }
                matcher.appendTail(temp);
                result = temp.toString();
            }

            selectVo.setResultMap(resultMap.get(element.attributeValue("resultMap")));
            selectVo.setResultType(resultTypeMap.get(element.attributeValue("resultMap")));
            selectList.add(selectVo);
        }

        return returnMap;
    }

    // public static void main(String[] argv) throws DocumentException {
    // String sql = "<sql>";
    // sql += "<resultMap id=\"lala\"><id column=\"user_id\"
    // property=\"userId\"></id><result column=\"user_name\"
    // property=\"user\"/><collection property=\"teamList\"><result
    // column=\"team_name\" property=\"teamName\"/></collection></resultMap>";
    // sql += "<select id=\"test2\" resultMap=\"lala\">select * from test where
    // 1=1 <if test=\"userId == 'CHENQW'\">and userId =
    // #{userId}</if></select></sql>";
    // Document document = DocumentHelper.parseText(sql);
    // Element root = document.getRootElement();
    // List<Element> selectList = root.elements("select");
    // Map<String, Object> paramMap = new HashMap();
    // String[] tids = new String[] { "1", "2" };
    // // paramMap.put("teamId", "1");
    // paramMap.put("userId", "CHENQW");
    // Map<String,Object> returnMap = analyseSql(sql, paramMap);
    // System.out.println(returnMap.get(""));
    // }

}
