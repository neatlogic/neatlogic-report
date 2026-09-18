package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportSqldefineServiceImpl implements ReportSqldefineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSqldefineServiceImpl.class);
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final Map<String, JSONObject> tableCache = new ConcurrentHashMap<>();
    private final Map<String, JSONArray> tableIndexCache = new ConcurrentHashMap<>();

    /** 按当前请求语言搜索可用于报表设计的数据库表定义。 */
    @Override
    public JSONArray searchTable(String moduleId, String keyword) {
        JSONArray resultList = new JSONArray();
        String lowerKeyword = StringUtils.lowerCase(StringUtils.trimToEmpty(keyword));
        for (Object obj : getTableIndexList(getCurrentLanguageSuffix())) {
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

    /** 按当前请求语言获取指定数据库表的完整定义。 */
    @Override
    public JSONObject getTable(String moduleId, String name) {
        if (StringUtils.isBlank(moduleId) || StringUtils.isBlank(name)) {
            return null;
        }
        String suffix = getCurrentLanguageSuffix();
        String cacheKey = suffix + ":" + moduleId + ":" + name;
        return tableCache.computeIfAbsent(cacheKey, key -> loadTable(moduleId, name, suffix));
    }

    /**
     * 获取当前请求语言；非请求线程使用 JVM 默认语言。
     *
     * @return 当前语言
     */
    private Locale getCurrentLocale() {
        RequestContext requestContext = RequestContext.get();
        if (requestContext != null && requestContext.getLocale() != null) {
            return requestContext.getLocale();
        }
        return Locale.getDefault();
    }

    /**
     * 获取当前语言对应的资源文件后缀。中文使用基准文件，其他语言统一使用英文文件。
     *
     * @return 空字符串或英文文件后缀
     */
    String getCurrentLanguageSuffix() {
        Locale locale = getCurrentLocale();
        return locale != null && Locale.CHINESE.getLanguage().equals(locale.getLanguage()) ? StringUtils.EMPTY : "-en";
    }

    /**
     * 获取指定语言对应的表索引缓存。
     *
     * @param suffix 资源文件语言后缀
     * @return 表索引
     */
    private JSONArray getTableIndexList(String suffix) {
        return tableIndexCache.computeIfAbsent(suffix, this::loadTableIndexList);
    }

    /**
     * 从当前语言的模块索引文件加载全部表摘要。
     *
     * @param suffix 资源文件语言后缀
     * @return 表摘要列表
     */
    private JSONArray loadTableIndexList(String suffix) {
        JSONArray resultList = new JSONArray();
        try {
            Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/*/sqldefine/index" + suffix + ".json");
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
            LOGGER.error("加载报表 SQL 表定义索引失败，资源后缀：{}", suffix, ex);
            throw new IllegalStateException("Load report sqldefine index failed", ex);
        }
        return resultList;
    }

    /**
     * 从当前语言的独立资源文件加载表详情。
     *
     * @param moduleId 模块标识
     * @param name 表名
     * @param suffix 资源文件语言后缀
     * @return 表详情，资源不存在时返回 null
     */
    private JSONObject loadTable(String moduleId, String name, String suffix) {
        try {
            Resource[] resources = resolver.getResources("classpath*:neatlogic/resources/" + moduleId + "/sqldefine/tables/" + name + suffix + ".json");
            if (resources.length == 0) {
                return null;
            }
            return readJson(resources[0]);
        } catch (Exception ex) {
            LOGGER.error("加载报表 SQL 表定义失败，模块：{}，表：{}，资源后缀：{}", moduleId, name, suffix, ex);
            throw new IllegalStateException("Load report sqldefine table failed: " + moduleId + "/" + name, ex);
        }
    }

    private JSONObject readJson(Resource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            return JSONObject.parseObject(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
