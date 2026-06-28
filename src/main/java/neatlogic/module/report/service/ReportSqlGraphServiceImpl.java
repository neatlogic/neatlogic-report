package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReportSqlGraphServiceImpl implements ReportSqlGraphService {

    private static final String DEFAULT_QUERY_ID = "queryData";
    private static final String DEFAULT_QUERY_LABEL = "查询数据";
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final Pattern RESULT_MAP_PATTERN = Pattern.compile("<resultMap\\s+([^>]*)>([\\s\\S]*?)</resultMap>", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESULT_FIELD_PATTERN = Pattern.compile("<(?:id|result|collection)\\s+([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_PATTERN = Pattern.compile("<(?:select|rest)\\s+([^>]*)>([\\s\\S]*?)</(?:select|rest)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR_PATTERN = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*([\"'])(.*?)\\2", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SELECT_ALIAS_PATTERN = Pattern.compile("(?i)\\bAS\\s+`?([A-Za-z_][A-Za-z0-9_]*)`?");
    private static final Set<String> JOIN_TYPE_SET = new HashSet<>(Arrays.asList("INNER JOIN", "LEFT JOIN"));
    private static final Set<String> OPERATOR_SET = new HashSet<>(Arrays.asList("=", "!=", "LIKE", "IN", "BETWEEN", ">=", "<=", ">", "<"));

    @Resource
    private ReportSqldefineService reportSqldefineService;

    @Override
    public JSONObject buildSql(JSONObject sqlGraphConfig) {
        JSONObject resultObj = new JSONObject();
        JSONArray errorList = new JSONArray();
        JSONArray tableList = new JSONArray();
        StringBuilder xml = new StringBuilder();
        xml.append("<mapper>\n");
        JSONArray queryList = normalizeQueryList(sqlGraphConfig);
        if (queryList == null || queryList.isEmpty()) {
            errorList.add("至少需要配置一个查询。");
        } else {
            for (Object obj : queryList) {
                if (!(obj instanceof JSONObject query)) {
                    continue;
                }
                buildQueryXml(query, xml, tableList, errorList);
            }
        }
        xml.append("</mapper>");
        resultObj.put("sql", errorList.isEmpty() ? xml.toString() : "");
        resultObj.put("tableList", tableList);
        resultObj.put("errorList", errorList);
        return resultObj;
    }

    @Override
    public JSONObject analyzeSql(String sql, JSONObject sqlGraphConfig) {
        JSONObject resultObj = new JSONObject();
        JSONArray errorList = new JSONArray();
        // XML 反向分析只提取数据源字段；只有匹配现有 Graph 节点别名时才同步字段配置。
        JSONArray tableList = analyzeTableList(sql);
        resultObj.put("tableList", tableList);
        resultObj.put("errorList", errorList);
        JSONObject mergedConfig = mergeOutputFields(sqlGraphConfig, tableList);
        if (mergedConfig != null) {
            resultObj.put("sqlGraphConfig", mergedConfig);
        }
        return resultObj;
    }

    private JSONArray normalizeQueryList(JSONObject sqlGraphConfig) {
        if (sqlGraphConfig == null) {
            return null;
        }
        JSONArray queryList = sqlGraphConfig.getJSONArray("queries");
        if (queryList != null && !queryList.isEmpty()) {
            JSONArray resultList = new JSONArray();
            Object firstQuery = queryList.get(0);
            if (firstQuery instanceof JSONObject query) {
                JSONObject normalizedQuery = new JSONObject();
                normalizedQuery.putAll(query);
                applyDefaultQueryMeta(normalizedQuery);
                resultList.add(normalizedQuery);
            }
            return resultList;
        }
        JSONArray nodeList = sqlGraphConfig.getJSONArray("nodes");
        if (nodeList == null || nodeList.isEmpty()) {
            return null;
        }
        JSONObject query = new JSONObject();
        query.put("nodes", nodeList);
        query.put("joins", sqlGraphConfig.getJSONArray("joins"));
        query.put("fields", sqlGraphConfig.getJSONArray("fields"));
        query.put("filters", sqlGraphConfig.getJSONArray("filters"));
        query.put("orders", sqlGraphConfig.getJSONArray("orders"));
        query.put("page", sqlGraphConfig.getJSONObject("page"));
        applyDefaultQueryMeta(query);
        JSONArray resultList = new JSONArray();
        resultList.add(query);
        return resultList;
    }

    private void applyDefaultQueryMeta(JSONObject query) {
        query.put("id", DEFAULT_QUERY_ID);
        query.put("label", DEFAULT_QUERY_LABEL);
    }

    private JSONArray analyzeTableList(String sql) {
        JSONArray tableList = new JSONArray();
        if (StringUtils.isBlank(sql)) {
            return tableList;
        }
        Map<String, JSONArray> resultMapColumnMap = parseResultMapColumnMap(sql);
        java.util.regex.Matcher selectMatcher = SELECT_PATTERN.matcher(sql);
        while (selectMatcher.find()) {
            Map<String, String> attrMap = parseAttrMap(selectMatcher.group(1));
            String id = attrMap.get("id");
            if (StringUtils.isBlank(id)) {
                continue;
            }
            JSONArray columnList = null;
            String resultMapId = attrMap.get("resultMap");
            if (StringUtils.isNotBlank(resultMapId)) {
                columnList = resultMapColumnMap.get(resultMapId);
            }
            if (columnList == null || columnList.isEmpty()) {
                columnList = parseSelectAliasColumnList(selectMatcher.group(2));
            }
            JSONObject tableObj = new JSONObject();
            tableObj.put("id", id);
            tableObj.put("label", Objects.equals(id, DEFAULT_QUERY_ID) ? DEFAULT_QUERY_LABEL : id);
            tableObj.put("columnList", columnList.stream()
                    .filter(JSONObject.class::isInstance)
                    .map(JSONObject.class::cast)
                    .map(field -> field.getString("property"))
                    .filter(StringUtils::isNotBlank)
                    .toList());
            tableObj.put("fields", columnList);
            tableList.add(tableObj);
        }
        return tableList;
    }

    private Map<String, JSONArray> parseResultMapColumnMap(String sql) {
        Map<String, JSONArray> resultMapColumnMap = new HashMap<>();
        java.util.regex.Matcher resultMapMatcher = RESULT_MAP_PATTERN.matcher(sql);
        while (resultMapMatcher.find()) {
            Map<String, String> resultMapAttrMap = parseAttrMap(resultMapMatcher.group(1));
            String resultMapId = resultMapAttrMap.get("id");
            if (StringUtils.isBlank(resultMapId)) {
                continue;
            }
            JSONArray columnList = new JSONArray();
            java.util.regex.Matcher fieldMatcher = RESULT_FIELD_PATTERN.matcher(resultMapMatcher.group(2));
            while (fieldMatcher.find()) {
                Map<String, String> fieldAttrMap = parseAttrMap(fieldMatcher.group(1));
                String property = fieldAttrMap.get("property");
                if (StringUtils.isBlank(property)) {
                    continue;
                }
                JSONObject fieldObj = new JSONObject();
                fieldObj.put("column", fieldAttrMap.get("column"));
                fieldObj.put("property", property);
                columnList.add(fieldObj);
            }
            resultMapColumnMap.put(resultMapId, columnList);
        }
        return resultMapColumnMap;
    }

    private JSONArray parseSelectAliasColumnList(String selectBody) {
        JSONArray columnList = new JSONArray();
        if (StringUtils.isBlank(selectBody)) {
            return columnList;
        }
        java.util.regex.Matcher aliasMatcher = SELECT_ALIAS_PATTERN.matcher(selectBody);
        while (aliasMatcher.find()) {
            String alias = aliasMatcher.group(1);
            JSONObject fieldObj = new JSONObject();
            fieldObj.put("column", alias);
            fieldObj.put("property", alias);
            columnList.add(fieldObj);
        }
        return columnList;
    }

    private JSONObject mergeOutputFields(JSONObject sqlGraphConfig, JSONArray tableList) {
        if (sqlGraphConfig == null || tableList == null || tableList.isEmpty()) {
            return null;
        }
        // 手写 XML 无法可靠还原 JOIN 和表节点，这里只按 Graph 生成的 alias_field 列别名回写输出字段。
        JSONObject graphData = sqlGraphConfig.getJSONObject("graphData");
        if (graphData == null) {
            return null;
        }
        Map<String, Set<String>> aliasFieldMap = buildAliasFieldMap(graphData);
        if (aliasFieldMap.isEmpty()) {
            return null;
        }
        JSONArray fieldList = new JSONArray();
        for (Object tableObj : tableList) {
            if (!(tableObj instanceof JSONObject table) || !Objects.equals(table.getString("id"), DEFAULT_QUERY_ID)) {
                continue;
            }
            JSONArray fields = table.getJSONArray("fields");
            if (fields == null) {
                continue;
            }
            for (Object fieldObj : fields) {
                if (!(fieldObj instanceof JSONObject field)) {
                    continue;
                }
                JSONObject graphField = matchGraphField(field, aliasFieldMap);
                if (graphField != null) {
                    fieldList.add(graphField);
                }
            }
        }
        if (fieldList.isEmpty()) {
            return null;
        }
        JSONObject mergedConfig = new JSONObject();
        mergedConfig.putAll(sqlGraphConfig);
        JSONObject queryConfig = new JSONObject();
        JSONObject oldQueryConfig = sqlGraphConfig.getJSONObject("queryConfig");
        if (oldQueryConfig != null) {
            queryConfig.putAll(oldQueryConfig);
        }
        queryConfig.put("fields", fieldList);
        mergedConfig.put("queryConfig", queryConfig);
        JSONArray queryList = sqlGraphConfig.getJSONArray("queries");
        if (queryList != null && !queryList.isEmpty() && queryList.get(0) instanceof JSONObject oldQuery) {
            JSONArray mergedQueryList = new JSONArray();
            JSONObject query = new JSONObject();
            query.putAll(oldQuery);
            query.put("fields", fieldList);
            mergedQueryList.add(query);
            mergedConfig.put("queries", mergedQueryList);
        }
        return mergedConfig;
    }

    private Map<String, Set<String>> buildAliasFieldMap(JSONObject graphData) {
        Map<String, Set<String>> aliasFieldMap = new HashMap<>();
        JSONArray nodeList = graphData.getJSONArray("nodes");
        if (nodeList == null) {
            return aliasFieldMap;
        }
        for (Object obj : nodeList) {
            if (!(obj instanceof JSONObject rawNode)) {
                continue;
            }
            JSONObject data = rawNode.getJSONObject("data");
            JSONObject node = data == null ? rawNode : data;
            String alias = node.getString("alias");
            JSONArray fields = node.getJSONArray("fields");
            if (StringUtils.isBlank(alias) || fields == null) {
                continue;
            }
            Set<String> fieldSet = new HashSet<>();
            for (Object fieldObj : fields) {
                if (fieldObj instanceof JSONObject field && StringUtils.isNotBlank(field.getString("name"))) {
                    fieldSet.add(field.getString("name"));
                }
            }
            aliasFieldMap.put(alias, fieldSet);
        }
        return aliasFieldMap;
    }

    private JSONObject matchGraphField(JSONObject xmlField, Map<String, Set<String>> aliasFieldMap) {
        String column = xmlField.getString("column");
        if (StringUtils.isBlank(column)) {
            return null;
        }
        for (Map.Entry<String, Set<String>> entry : aliasFieldMap.entrySet()) {
            String alias = entry.getKey();
            String prefix = alias + "_";
            if (!column.startsWith(prefix)) {
                continue;
            }
            String fieldName = StringUtils.substringAfter(column, prefix);
            if (entry.getValue().contains(fieldName)) {
                JSONObject graphField = new JSONObject();
                graphField.put("tableAlias", alias);
                graphField.put("fieldName", fieldName);
                graphField.put("property", StringUtils.defaultIfBlank(xmlField.getString("property"), fieldName));
                return graphField;
            }
        }
        return null;
    }

    private Map<String, String> parseAttrMap(String attrText) {
        Map<String, String> attrMap = new HashMap<>();
        if (StringUtils.isBlank(attrText)) {
            return attrMap;
        }
        java.util.regex.Matcher attrMatcher = ATTR_PATTERN.matcher(attrText);
        while (attrMatcher.find()) {
            attrMap.put(attrMatcher.group(1), unescapeXml(attrMatcher.group(3)));
        }
        return attrMap;
    }

    private void buildQueryXml(JSONObject query, StringBuilder xml, JSONArray tableList, JSONArray errorList) {
        String queryId = sanitizeIdentifier(query.getString("id"), DEFAULT_QUERY_ID);
        JSONArray nodeList = query.getJSONArray("nodes");
        if (nodeList == null || nodeList.isEmpty()) {
            errorList.add(queryId + " 至少需要一张表。");
            return;
        }
        LinkedHashMap<String, JSONObject> nodeMap = buildNodeMap(nodeList, errorList);
        if (nodeMap.isEmpty()) {
            errorList.add(queryId + " 没有合法表节点。");
            return;
        }
        JSONArray fieldList = query.getJSONArray("fields");
        List<JSONObject> outputFieldList = buildOutputFieldList(nodeMap, fieldList, errorList);
        if (CollectionUtils.isEmpty(outputFieldList)) {
            errorList.add(queryId + " 至少需要一个输出字段。");
            return;
        }
        String resultMapId = queryId + "Map";
        xml.append("  <resultMap id=\"").append(escapeXml(resultMapId)).append("\" type=\"java.util.LinkedHashMap\">\n");
        for (int i = 0; i < outputFieldList.size(); i++) {
            JSONObject field = outputFieldList.get(i);
            String tag = i == 0 ? "id" : "result";
            xml.append("    <").append(tag).append(" column=\"").append(escapeXml(field.getString("columnAlias")))
                    .append("\" property=\"").append(escapeXml(field.getString("property"))).append("\"/>\n");
        }
        xml.append("  </resultMap>\n");
        xml.append("  <select id=\"").append(escapeXml(queryId)).append("\" resultMap=\"").append(escapeXml(resultMapId)).append("\">\n");
        xml.append("    SELECT\n");
        for (int i = 0; i < outputFieldList.size(); i++) {
            JSONObject field = outputFieldList.get(i);
            xml.append("      ").append(field.getString("tableAlias")).append(".`").append(field.getString("fieldName")).append("` AS `")
                    .append(field.getString("columnAlias")).append("`");
            xml.append(i == outputFieldList.size() - 1 ? "\n" : ",\n");
        }
        JSONObject firstNode = nodeMap.values().iterator().next();
        xml.append("    FROM `").append(firstNode.getString("tableName")).append("` ").append(firstNode.getString("alias")).append("\n");
        buildJoinXml(query.getJSONArray("joins"), nodeMap, xml, errorList);
        buildWhereXml(query.getJSONArray("filters"), nodeMap, xml, errorList);
        buildOrderXml(query.getJSONArray("orders"), nodeMap, xml, errorList);
        xml.append("  </select>\n");
        JSONObject tableObj = new JSONObject();
        tableObj.put("id", queryId);
        tableObj.put("label", StringUtils.defaultIfBlank(query.getString("label"), DEFAULT_QUERY_LABEL));
        tableObj.put("columnList", outputFieldList.stream().map(field -> field.getString("property")).toList());
        tableList.add(tableObj);
    }

    private LinkedHashMap<String, JSONObject> buildNodeMap(JSONArray nodeList, JSONArray errorList) {
        LinkedHashMap<String, JSONObject> nodeMap = new LinkedHashMap<>();
        for (Object obj : nodeList) {
            if (!(obj instanceof JSONObject rawNode)) {
                continue;
            }
            JSONObject data = rawNode.getJSONObject("data");
            JSONObject node = data == null ? rawNode : data;
            String moduleId = node.getString("moduleId");
            String tableName = StringUtils.defaultIfBlank(node.getString("tableName"), node.getString("name"));
            String alias = sanitizeIdentifier(node.getString("alias"), "t" + (nodeMap.size() + 1));
            if (!isIdentifier(tableName)) {
                errorList.add("表名不合法：" + tableName);
                continue;
            }
            JSONObject table = reportSqldefineService.getTable(moduleId, tableName);
            if (table == null) {
                errorList.add("找不到表定义：" + moduleId + "/" + tableName);
                continue;
            }
            JSONObject normalized = new JSONObject();
            normalized.put("moduleId", moduleId);
            normalized.put("tableName", tableName);
            normalized.put("alias", alias);
            normalized.put("label", table.getString("label"));
            normalized.put("table", table);
            nodeMap.put(alias, normalized);
        }
        return nodeMap;
    }

    private List<JSONObject> buildOutputFieldList(Map<String, JSONObject> nodeMap, JSONArray fieldList, JSONArray errorList) {
        List<JSONObject> resultList = new ArrayList<>();
        if (fieldList == null || fieldList.isEmpty()) {
            JSONObject firstNode = nodeMap.values().iterator().next();
            JSONArray fields = firstNode.getJSONObject("table").getJSONArray("fields");
            if (fields != null) {
                for (Object obj : fields) {
                    if (resultList.size() >= 8) {
                        break;
                    }
                    if (obj instanceof JSONObject field && !StringUtils.defaultString(field.getString("type")).toLowerCase(Locale.ROOT).contains("text")) {
                        JSONObject outputField = normalizeOutputField(firstNode, field.getString("name"), field.getString("name"), field.getString("description"), errorList);
                        if (outputField != null) {
                            resultList.add(outputField);
                        }
                    }
                }
            }
            return resultList;
        }
        for (Object obj : fieldList) {
            if (!(obj instanceof JSONObject field)) {
                continue;
            }
            String tableAlias = StringUtils.defaultIfBlank(field.getString("tableAlias"), field.getString("alias"));
            JSONObject node = nodeMap.get(tableAlias);
            if (node == null) {
                errorList.add("输出字段所属表不存在：" + tableAlias);
                continue;
            }
            String fieldName = StringUtils.defaultIfBlank(field.getString("fieldName"), field.getString("name"));
            String property = StringUtils.defaultIfBlank(field.getString("property"), StringUtils.defaultIfBlank(field.getString("label"), fieldName));
            JSONObject outputField = normalizeOutputField(node, fieldName, property, null, errorList);
            if (outputField != null) {
                resultList.add(outputField);
            }
        }
        return resultList;
    }

    private JSONObject normalizeOutputField(JSONObject node, String fieldName, String property, String description, JSONArray errorList) {
        if (!isIdentifier(fieldName) || !hasField(node.getJSONObject("table"), fieldName)) {
            errorList.add("字段不存在：" + node.getString("tableName") + "." + fieldName);
            return null;
        }
        JSONObject result = new JSONObject();
        result.put("tableAlias", node.getString("alias"));
        result.put("fieldName", fieldName);
        result.put("columnAlias", node.getString("alias") + "_" + fieldName);
        result.put("property", StringUtils.defaultIfBlank(property, StringUtils.defaultIfBlank(description, fieldName)));
        return result;
    }

    private void buildJoinXml(JSONArray joinList, Map<String, JSONObject> nodeMap, StringBuilder xml, JSONArray errorList) {
        if (joinList == null) {
            return;
        }
        for (Object obj : joinList) {
            if (!(obj instanceof JSONObject join)) {
                continue;
            }
            String sourceAlias = StringUtils.defaultIfBlank(join.getString("sourceTable"), join.getString("sourceAlias"));
            String targetAlias = StringUtils.defaultIfBlank(join.getString("targetTable"), join.getString("targetAlias"));
            JSONObject sourceNode = nodeMap.get(sourceAlias);
            JSONObject targetNode = nodeMap.get(targetAlias);
            if (sourceNode == null || targetNode == null) {
                errorList.add("JOIN 表不存在：" + sourceAlias + " -> " + targetAlias);
                continue;
            }
            String sourceField = join.getString("sourceField");
            String targetField = join.getString("targetField");
            if (!hasField(sourceNode.getJSONObject("table"), sourceField) || !hasField(targetNode.getJSONObject("table"), targetField)) {
                errorList.add("JOIN 字段不存在：" + sourceAlias + "." + sourceField + " = " + targetAlias + "." + targetField);
                continue;
            }
            String joinType = StringUtils.upperCase(StringUtils.defaultIfBlank(join.getString("joinType"), "INNER JOIN"));
            if (!JOIN_TYPE_SET.contains(joinType)) {
                joinType = "INNER JOIN";
            }
            xml.append("    ").append(joinType).append(" `").append(targetNode.getString("tableName")).append("` ").append(targetAlias)
                    .append(" ON ").append(sourceAlias).append(".`").append(sourceField).append("` = ").append(targetAlias).append(".`").append(targetField).append("`\n");
        }
    }

    private void buildWhereXml(JSONArray filterList, Map<String, JSONObject> nodeMap, StringBuilder xml, JSONArray errorList) {
        if (filterList == null || filterList.isEmpty()) {
            return;
        }
        xml.append("    <where>\n");
        for (Object obj : filterList) {
            if (!(obj instanceof JSONObject filter)) {
                continue;
            }
            String tableAlias = StringUtils.defaultIfBlank(filter.getString("tableAlias"), filter.getString("alias"));
            JSONObject node = nodeMap.get(tableAlias);
            String fieldName = StringUtils.defaultIfBlank(filter.getString("fieldName"), filter.getString("name"));
            String paramName = sanitizeIdentifier(filter.getString("paramName"), fieldName);
            String operator = StringUtils.upperCase(StringUtils.defaultIfBlank(filter.getString("operator"), "="));
            if (node == null || !hasField(node.getJSONObject("table"), fieldName)) {
                errorList.add("过滤字段不存在：" + tableAlias + "." + fieldName);
                continue;
            }
            if (!OPERATOR_SET.contains(operator)) {
                errorList.add("不支持的过滤操作符：" + operator);
                continue;
            }
            xml.append("      <if test=\"").append(paramName).append(" != null");
            if (!"IN".equals(operator) && !"BETWEEN".equals(operator)) {
                xml.append(" and ").append(paramName).append(" != ''");
            }
            xml.append("\">\n");
            if ("LIKE".equals(operator)) {
                xml.append("        AND ").append(tableAlias).append(".`").append(fieldName).append("` LIKE CONCAT('%',#{").append(paramName).append("},'%')\n");
            } else if ("IN".equals(operator)) {
                xml.append("        AND ").append(tableAlias).append(".`").append(fieldName).append("` IN\n");
                xml.append("        <foreach collection=\"").append(paramName).append("\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>\n");
            } else if ("BETWEEN".equals(operator)) {
                xml.append("        AND ").append(tableAlias).append(".`").append(fieldName).append("` &gt;= #{").append(paramName).append("[0]}\n");
                xml.append("        AND ").append(tableAlias).append(".`").append(fieldName).append("` &lt;= #{").append(paramName).append("[1]}\n");
            } else {
                xml.append("        AND ").append(tableAlias).append(".`").append(fieldName).append("` ").append(escapeOperator(operator)).append(" #{").append(paramName).append("}\n");
            }
            xml.append("      </if>\n");
        }
        xml.append("    </where>\n");
    }

    private void buildOrderXml(JSONArray orderList, Map<String, JSONObject> nodeMap, StringBuilder xml, JSONArray errorList) {
        if (orderList == null || orderList.isEmpty()) {
            return;
        }
        List<String> orderSqlList = new ArrayList<>();
        for (Object obj : orderList) {
            if (!(obj instanceof JSONObject order)) {
                continue;
            }
            String tableAlias = StringUtils.defaultIfBlank(order.getString("tableAlias"), order.getString("alias"));
            JSONObject node = nodeMap.get(tableAlias);
            String fieldName = StringUtils.defaultIfBlank(order.getString("fieldName"), order.getString("name"));
            if (node == null || !hasField(node.getJSONObject("table"), fieldName)) {
                errorList.add("排序字段不存在：" + tableAlias + "." + fieldName);
                continue;
            }
            String direction = "DESC".equalsIgnoreCase(order.getString("direction")) ? "DESC" : "ASC";
            orderSqlList.add(tableAlias + ".`" + fieldName + "` " + direction);
        }
        if (CollectionUtils.isNotEmpty(orderSqlList)) {
            xml.append("    ORDER BY ").append(StringUtils.join(orderSqlList, ", ")).append("\n");
        }
    }

    private boolean hasField(JSONObject table, String fieldName) {
        if (!isIdentifier(fieldName)) {
            return false;
        }
        JSONArray fields = table.getJSONArray("fields");
        if (fields == null) {
            return false;
        }
        for (Object obj : fields) {
            if (obj instanceof JSONObject field && Objects.equals(fieldName, field.getString("name"))) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeIdentifier(String value, String defaultValue) {
        return isIdentifier(value) ? value : defaultValue;
    }

    private boolean isIdentifier(String value) {
        return StringUtils.isNotBlank(value) && IDENTIFIER_PATTERN.matcher(value).matches();
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String unescapeXml(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("&quot;", "\"").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
    }

    private String escapeOperator(String operator) {
        return operator.replace("<", "&lt;").replace(">", "&gt;");
    }
}
