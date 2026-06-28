package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportSqldefineServiceImpl implements ReportSqldefineService {

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final Map<String, JSONObject> tableCache = new ConcurrentHashMap<>();
    private volatile JSONArray tableIndexCache;

    @Override
    public JSONArray searchTable(String moduleId, String keyword) {
        JSONArray resultList = new JSONArray();
        String lowerKeyword = StringUtils.lowerCase(StringUtils.trimToEmpty(keyword));
        for (Object obj : getTableIndexList()) {
            if (!(obj instanceof JSONObject table)) {
                continue;
            }
            if (StringUtils.isNotBlank(moduleId) && !StringUtils.equals(moduleId, table.getString("moduleId"))) {
                continue;
            }
            if (StringUtils.isNotBlank(lowerKeyword)
                    && !StringUtils.contains(StringUtils.lowerCase(table.getString("name")), lowerKeyword)
                    && !StringUtils.contains(StringUtils.lowerCase(table.getString("label")), lowerKeyword)
                    && !StringUtils.contains(StringUtils.lowerCase(table.getString("description")), lowerKeyword)) {
                continue;
            }
            resultList.add(table);
        }
        return resultList;
    }

    @Override
    public JSONObject getTable(String moduleId, String name) {
        if (StringUtils.isBlank(moduleId) || StringUtils.isBlank(name)) {
            return null;
        }
        String cacheKey = moduleId + ":" + name;
        return tableCache.computeIfAbsent(cacheKey, key -> loadTable(moduleId, name));
    }

    private JSONArray getTableIndexList() {
        if (tableIndexCache == null) {
            synchronized (this) {
                if (tableIndexCache == null) {
                    tableIndexCache = loadTableIndexList();
                }
            }
        }
        return tableIndexCache;
    }

    private JSONArray loadTableIndexList() {
        JSONArray resultList = new JSONArray();
        try {
            Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/*/sqldefine/index.json");
            for (Resource resource : resources) {
                JSONObject indexObj = readJson(resource);
                String moduleId = indexObj.getString("moduleId");
                JSONArray tableList = indexObj.getJSONArray("tables");
                if (StringUtils.isBlank(moduleId) || tableList == null) {
                    continue;
                }
                for (Object obj : tableList) {
                    if (obj instanceof JSONObject table) {
                        JSONObject tableObj = new JSONObject();
                        tableObj.put("moduleId", moduleId);
                        tableObj.put("name", table.getString("name"));
                        tableObj.put("label", table.getString("label"));
                        tableObj.put("description", table.getString("description"));
                        resultList.add(tableObj);
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Load report sqldefine index failed", ex);
        }
        return resultList;
    }

    private JSONObject loadTable(String moduleId, String name) {
        try {
            Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/" + moduleId + "/sqldefine/tables/" + name + ".json");
            if (resources.length == 0) {
                return null;
            }
            return readJson(resources[0]);
        } catch (Exception ex) {
            throw new IllegalStateException("Load report sqldefine table failed: " + moduleId + "/" + name, ex);
        }
    }

    private JSONObject readJson(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return JSONObject.parseObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
