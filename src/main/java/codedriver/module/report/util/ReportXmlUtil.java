/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.util;

import codedriver.framework.dto.RestVo;
import codedriver.framework.util.HtmlUtil;
import codedriver.module.report.dto.ResultMapVo;
import codedriver.module.report.dto.SelectVo;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportXmlUtil {
    static Logger logger = LoggerFactory.getLogger(ReportXmlUtil.class);

    private static void validateSubXml(List<Element> elementList, Map<String, Boolean> checkMap) {
        for (Element sub : elementList) {
            if (sub.getName().equals("id") || sub.getName().equals("result") || sub.getName().equals("collection")) {
                if (sub.attributeValue("property") == null) {
                    throw new RuntimeException("“" + sub.getName() + "”节点必须定义唯一的“property”属性");
                } else {
                    if (checkMap.containsKey(sub.attributeValue("property"))) {
                        throw new RuntimeException(
                            "property=" + sub.attributeValue("property") + "的" + sub.getName() + "节点已存在");
                    } else {
                        checkMap.put(sub.attributeValue("property"), true);
                    }
                }
                List<Element> sList = sub.elements();
                if (sList != null && sList.size() > 0) {
                    validateSubXml(sList, checkMap);
                }
            }
        }

    }

    public static boolean validateXml(String xml) throws DocumentException, SAXException, IOException {
        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        if (!root.getName().equals("mapper")) {
            throw new RuntimeException("xml根节点名称必须是“mapper”");
        }

        List<Element> resultMapList = root.elements("resultMap");
        List<Element> restList = root.elements("rest");
        Map<String, Boolean> checkResultMap = new HashMap<String, Boolean>();
        if (restList.size() <= 0) {
            if (resultMapList.size() <= 0) {
                throw new RuntimeException("请定义一个或多个“resultMap”节点");
            } else {

                for (Element resultmap : resultMapList) {
                    if (resultmap.attributeValue("id") == null) {
                        throw new RuntimeException("“resultMap”节点必须定义“id”属性");
                    } else {
                        if (checkResultMap.containsKey(resultmap.attributeValue("id"))) {
                            throw new RuntimeException("resultMap:" + resultmap.attributeValue("id") + "已存在");
                        } else {
                            checkResultMap.put(resultmap.attributeValue("id"), true);
                        }
                    }
                    if ("Map".equals(resultmap.attributeValue("resultType"))) {
                        if (resultmap.elements("collection").size() > 0) {
                            throw new RuntimeException("resultType等于Map的resultMap节点不能包含collection节点。");
                        }
                    }
                    List<Element> subResultElList = resultmap.elements();
                    Map<String, Boolean> subCheckMap = new HashMap<String, Boolean>();
                    if (subResultElList != null && subResultElList.size() > 0) {
                        validateSubXml(subResultElList, subCheckMap);
                    } else {
                        throw new RuntimeException("resultMap节点必须包含id或result或collection节点");
                    }
                }
            }
        }

        List<Element> selectList = root.elements("select");
        if (restList.size() <= 0) {
            if (selectList.size() <= 0) {
                throw new RuntimeException("请定义一个或多个“select”节点");
            } else {
                Map<String, Boolean> checkMap = new HashMap<String, Boolean>();
                for (Element select : selectList) {
                    if (select.attributeValue("id") == null) {
                        throw new RuntimeException("“select”节点必须定义唯一的“id”属性");
                    } else if (select.attributeValue("resultMap") == null) {
                        throw new RuntimeException("“select”节点必须定义“resultMap”属性");
                    } else if (!checkResultMap.containsKey(select.attributeValue("resultMap"))) {
                        throw new RuntimeException("id=“" + select.attributeValue("id") + "”的select节点引用了不存在的resultMap");
                    } else {
                        if (checkMap.containsKey(select.attributeValue("id"))) {
                            throw new RuntimeException("“select”节点的“id”属性必须唯一");
                        } else {
                            checkMap.put(select.attributeValue("id"), true);
                        }
                    }
                }
            }
        }

        if (restList.size() > 0) {
            Map<String, Boolean> checkMap = new HashMap<String, Boolean>();
            for (Element rest : restList) {
                if (rest.attributeValue("id") == null) {
                    throw new RuntimeException("“rest”节点必须定义唯一的“id”属性");
                } else if (rest.attributeValue("authtype") != null
                    && !rest.attributeValue("authtype").trim().equals("")) {
                    if (!rest.attributeValue("authtype").equals("basic")) {
                        throw new RuntimeException(
                            "id=“" + rest.attributeValue("id") + "”的rest节点authtype属性只能是basic或为空");
                    } else {
                        if (rest.attributeValue("username") == null
                            || rest.attributeValue("username").trim().equals("")) {
                            throw new RuntimeException("id=“" + rest.attributeValue("id") + "”的rest节点缺少username属性");
                        }
                        if (rest.attributeValue("password") == null
                            || rest.attributeValue("password").trim().equals("")) {
                            throw new RuntimeException("id=“" + rest.attributeValue("id") + "”的rest节点缺少password属性");
                        }
                    }

                } else {
                    if (checkMap.containsKey(rest.attributeValue("id"))) {
                        throw new RuntimeException("“rest”节点的“id”属性必须唯一");
                    } else {
                        checkMap.put(rest.attributeValue("id"), true);
                    }
                }
            }
        }

        List<Node> ifNotNullElementList = document.selectNodes("//ifNotNull");
        List<Node> ifNullElementList = document.selectNodes("//ifNull");
        List<Node> forEachElementList = document.selectNodes("//forEach");
        List<Node> ifElementList = document.selectNodes("//if");
        if (ifNotNullElementList != null && ifNotNullElementList.size() > 0) {
            for (Node node : ifNotNullElementList) {
                Element e = (Element)node;
                if (e.attributeValue("parameter") == null || e.attributeValue("parameter").trim().equals("")) {
                    throw new RuntimeException("“ifNotNull”节点的“parameter”属性不能为空。");
                }
            }
        }
        if (ifNullElementList != null && ifNullElementList.size() > 0) {
            for (Node node : ifNullElementList) {
                Element e = (Element)node;
                if (e.attributeValue("parameter") == null || e.attributeValue("parameter").trim().equals("")) {
                    throw new RuntimeException("“ifNull”节点的“parameter”属性不能为空。");
                }
            }
        }
        if (forEachElementList != null && forEachElementList.size() > 0) {
            for (Node node : forEachElementList) {
                Element e = (Element)node;
                if (e.attributeValue("parameter") == null || e.attributeValue("separator") == null
                    || e.attributeValue("parameter").trim().equals("")
                    || e.attributeValue("separator").trim().equals("")) {
                    throw new RuntimeException("“forEach”节点的“parameter”和“separator”属性不能为空。");
                }
            }
        }
        if (ifElementList != null && ifElementList.size() > 0) {
            for (Node node : ifElementList) {
                Element e = (Element)node;
                if (e.attributeValue("test") == null || e.attributeValue("test").trim().equals("")) {
                    throw new RuntimeException("“if”节点的“test”属性不能为空。");
                }
            }
        }
        /*
         * SchemaFactory schemaFactory =
         * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); Schema schema
         * = schemaFactory.newSchema(ReportAnalyser.class.getResource(
         * "/com/techsure/module/balantreport/report.xsd")); Validator validator =
         * schema.newValidator(); Source source = new StreamSource(new
         * StringReader(xml)); validator.validate(source);
         */

        return true;
    }

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
                    map = new HashMap<String, ResultMapVo>();
                }
                map.put(result.attributeValue("property"), analyseResultMap(result));
                resultMapVo.setResultMap(map);
            }
        }
        return resultMapVo;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public static Map<String, Object> analyseSql(String xml, Map<String, Object> paramMap)
        throws DocumentException, UnsupportedEncodingException {
        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();
        List<Element> resultMapNodeList = root.elements("resultMap");
        List<Element> sqlNodeList = root.elements("select");
        List<Element> restNodeList = root.elements("rest");
        String regex_param = "\\#\\{([^\\}]+?)\\}";
        String regex_dollarparam = "\\$\\{([^\\}]+?)\\}";
        String[] replace_regex =
            new String[] {"<[/]?if[^>]*?>", "<[/]?rest[^>]*?>", "<[/]?select[^>]*?>", "<[/]?forEach[^>]*?>",
                "<[/]?ifNotNull[^>]*?>", "<[/]?ifNull[^>]*?>", "<[/]?forEach[^>]*?>", "\\<\\!\\[CDATA\\[", "\\]\\]\\>"};
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List<SelectVo> selectList = new ArrayList<SelectVo>();
        List<RestVo> restList = new ArrayList<RestVo>();
        returnMap.put("select", selectList);
        returnMap.put("rest", restList);
        Map<String, ResultMapVo> resultMap = new HashMap<String, ResultMapVo>();
        Map<String, Integer> resultTypeMap = new HashMap<String, Integer>();

        if (restNodeList != null && restNodeList.size() > 0) {
            for (Element element : restNodeList) {
                String url = element.attributeValue("url");
                String authType = element.attributeValue("authtype") == null ? "" : element.attributeValue("authtype");
                boolean lazyLoad = element.attributeValue("lazyload") != null && Boolean.parseBoolean(element.attributeValue("lazyload"));
                String userName = element.attributeValue("username") == null ? "" : element.attributeValue("username");
                String password = element.attributeValue("password") == null ? "" : element.attributeValue("password");
                int readTimeOut = element.attributeValue("timeout") == null ? 60000 : Integer.parseInt(element.attributeValue("timeout"));
                RestVo.Builder restBuilder = new RestVo.Builder(url,authType).setLazyLoad(lazyLoad).setUsername(userName)
                        .setPassword(password).setId(element.attributeValue("id")).setReadTimeout(readTimeOut);
                List<Element> ifElList = element.elements("if");
                if (ifElList != null && ifElList.size() > 0) {
                    List<Element> removeObj = new ArrayList<Element>();
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
                        if (p != null) {
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
                List<Element> forEachList = element.elements("forEach");
                if (forEachList != null && forEachList.size() > 0) {
                    List<Element> removeObj = new ArrayList<Element>();
                    for (Element foreachEl : forEachList) {
                        boolean hasParam = false;
                        Object p = paramMap.get(foreachEl.attributeValue("parameter"));
                        if (p != null) {
                            hasParam = true;
                            String separator = foreachEl.attributeValue("separator");
                            String orgText = foreachEl.getText();
                            String newText = "";
                            if (p instanceof String) {
                                newText = orgText;
                            } else if (p instanceof String[]) {
                                for (int pi = 0; pi < ((String[])p).length; pi++) {
                                    newText += orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}",
                                        "#{" + foreachEl.attributeValue("parameter") + "#" + pi + "}");
                                    if (pi < ((String[])p).length - 1) {
                                        newText += separator;
                                    }
                                }
                            }
                            foreachEl.setText(newText);
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
                StringBuffer temp = null;
                Pattern pattern = null;
                Matcher matcher = null;
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
                    if (key.indexOf("#") > -1) {
                        key = key.substring(0, key.lastIndexOf("#"));
                    }
                    Object pp = paramMap.get(key);
                    if (pp instanceof String) {
                        matcher.appendReplacement(temp, URLEncoder.encode(pp.toString(), "UTF-8"));
                    } else if (pp instanceof String[]) {
                        if (matcher.group(1).indexOf("#") > -1) {
                            int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                            String[] ps = (String[])pp;
                            if (ps[s] != null) {
                                matcher.appendReplacement(temp, URLEncoder.encode(ps[s], "UTF-8"));
                            }
                        } else {
                            matcher.appendReplacement(temp, URLEncoder.encode(((String[])pp)[0], "UTF-8"));
                        }
                    } else if (pp instanceof List<?>) {
                        if (matcher.group(1).indexOf("#") > -1) {
                            int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                            List<String> ps = (List<String>)pp;
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
            Integer datasource = element.attributeValue("datasource") == null ? 0
                : Integer.parseInt(element.attributeValue("datasource"));
            Integer queryTimeout =
                element.attributeValue("timeout") == null ? 30 : Integer.parseInt(element.attributeValue("timeout"));
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
                List<Element> removeObj = new ArrayList<Element>();
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
                    if (p != null) {
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
                List<Element> removeObj = new ArrayList<Element>();
                for (Node node : forEachList) {
                    Element foreachEl = (Element)node;
                    boolean hasParam = false;
                    Object p = paramMap.get(foreachEl.attributeValue("parameter"));
                    if (p != null) {
                        hasParam = true;
                        String separator = foreachEl.attributeValue("separator");
                        String orgText = foreachEl.getText();
                        String newText = "";
                        if (p instanceof String) {
                            newText = orgText;
                        } else if (p instanceof String[]) {
                            for (int pi = 0; pi < ((String[])p).length; pi++) {
                                newText += orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}",
                                    "#{" + foreachEl.attributeValue("parameter") + "#" + pi + "}");
                                if (pi < ((String[])p).length - 1) {
                                    newText += separator;
                                }
                            }
                        } else if (p instanceof List<?>) {
                            int i = 0;
                            for (String pi : (List<String>)p) {
                                newText += orgText.replace("#{" + foreachEl.attributeValue("parameter") + "}",
                                    "#{" + foreachEl.attributeValue("parameter") + "#" + i + "}");
                                if (i < ((List<String>)p).size() - 1) {
                                    newText += separator;
                                }
                                i++;
                            }
                        }
                        foreachEl.setText(newText);
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
            StringBuffer temp = null;
            Pattern pattern = null;
            Matcher matcher = null;
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
                if (key.indexOf("#") > -1) {
                    key = key.substring(0, key.lastIndexOf("#"));
                }
                Object pp = paramMap.get(key);
                if (pp instanceof String) {
                    matcher.appendReplacement(temp, "?");
                    paramList.add(pp);
                } else if (pp instanceof String[]) {
                    if (matcher.group(1).indexOf("#") > -1) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                        String[] ps = (String[])pp;
                        if (ps[s] != null) {
                            paramList.add(ps[s]);
                            matcher.appendReplacement(temp, "?");
                        }
                    } else {
                        matcher.appendReplacement(temp, "?");
                        paramList.add(((String[])pp)[0]);
                    }
                } else if (pp instanceof List<?>) {
                    if (matcher.group(1).indexOf("#") > -1) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("#") + 1));
                        List<String> ps = (List<String>)pp;
                        if (ps.get(s) != null) {
                            paramList.add(ps.get(s));
                            matcher.appendReplacement(temp, "?");
                        }
                    } else {
                        matcher.appendReplacement(temp, "?");
                        paramList.add(((List<String>)pp).get(0));
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
                    matcher.appendReplacement(temp, (String)pp);
                } else if (pp instanceof String[]) {
                    if (matcher.group(1).indexOf("$") > -1) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("$") + 1));
                        String[] ps = (String[])pp;
                        if (ps[s] != null) {
                            matcher.appendReplacement(temp, ps[s]);
                        }
                    } else {
                        matcher.appendReplacement(temp, ((String[])pp)[0]);
                    }
                } else if (pp instanceof List<?>) {
                    if (matcher.group(1).indexOf("$") > -1) {
                        int s = Integer.parseInt(matcher.group(1).substring(matcher.group(1).lastIndexOf("$") + 1));
                        List<String> ps = (List<String>)pp;
                        if (ps.get(s) != null) {
                            matcher.appendReplacement(temp, ps.get(s));
                        }
                    } else {
                        matcher.appendReplacement(temp, ((List<String>)pp).get(0));
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
        String regex_param = "\\#\\{([^\\}]+?)\\}";
        // String regex_dollarparam = "\\$\\{([^\\}]+?)\\}";
        String[] replace_regex =
            new String[] {"<[/]?if[^>]*?>", "<[/]?rest[^>]*?>", "<[/]?select[^>]*?>", "<[/]?forEach[^>]*?>",
                "<[/]?ifNotNull[^>]*?>", "<[/]?ifNull[^>]*?>", "<[/]?forEach[^>]*?>", "\\<\\!\\[CDATA\\[", "\\]\\]\\>"};
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List<SelectVo> selectList = new ArrayList<SelectVo>();
        List<RestVo> restList = new ArrayList<RestVo>();
        returnMap.put("select", selectList);
        returnMap.put("rest", restList);
        Map<String, ResultMapVo> resultMap = new HashMap<String, ResultMapVo>();
        Map<String, Integer> resultTypeMap = new HashMap<String, Integer>();

        if (restNodeList != null && restNodeList.size() > 0) {
            for (Element element : restNodeList) {
                String url = element.attributeValue("url");
                String authType = element.attributeValue("authtype") == null ? "" : element.attributeValue("authtype");
                boolean lazyLoad = element.attributeValue("lazyload") != null && Boolean.parseBoolean(element.attributeValue("lazyload"));
                String userName = element.attributeValue("username") == null ? "" : element.attributeValue("username");
                String password = element.attributeValue("password") == null ? "" : element.attributeValue("password");
                int readTimeOut = element.attributeValue("timeout") == null ? 60000 : Integer.parseInt(element.attributeValue("timeout"));
                RestVo.Builder restBuilder = new RestVo.Builder(url,authType).setLazyLoad(lazyLoad).setUsername(userName)
                        .setPassword(password).setId(element.attributeValue("id")).setReadTimeout(readTimeOut);
                String result = element.asXML();
                result = HtmlUtil.decodeHtml(result);
                StringBuffer temp = null;
                Pattern pattern = null;
                Matcher matcher = null;
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
                    if (key.indexOf("#") > -1) {
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
            Integer datasource = element.attributeValue("datasource") == null ? 0
                : Integer.parseInt(element.attributeValue("datasource"));
            Integer queryTimeout =
                element.attributeValue("timeout") == null ? 30 : Integer.parseInt(element.attributeValue("timeout"));
            String needPage = element.attributeValue("needPage");
            String pageSize = element.attributeValue("pageSize");
            selectVo.setId(sqlId);
            selectVo.setQueryTimeout(queryTimeout);
            selectVo.setLazyLoad(Boolean.parseBoolean(lazyLoad));
            selectVo.setDatasource(datasource);
            selectVo.setNeedPage(needPage);
            selectVo.setPageSize(pageSize);
            String result = element.asXML();
            StringBuffer temp = null;
            Pattern pattern = null;
            Matcher matcher = null;
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
