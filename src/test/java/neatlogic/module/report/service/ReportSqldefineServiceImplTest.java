package neatlogic.module.report.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 验证报表 SQL 定义按请求语言读取独立资源文件。
 */
public class ReportSqldefineServiceImplTest {
    private Locale previousLocale;
    private ReportSqldefineServiceImpl service;

    /** 初始化测试服务并保存默认语言。 */
    @Before
    public void initialize() {
        previousLocale = Locale.getDefault();
        service = new ReportSqldefineServiceImpl();
    }

    /** 恢复测试前的默认语言。 */
    @After
    public void restore() {
        Locale.setDefault(previousLocale);
    }

    /** 中文请求应读取无后缀基准文件。 */
    @Test
    public void shouldLoadChineseResource() {
        Locale.setDefault(Locale.CHINESE);

        JSONObject table = service.getTable("report", "report");

        assertNotNull(table);
        assertEquals("报表模版表", table.getString("label"));
        assertEquals("", service.getCurrentLanguageSuffix());
    }

    /** 英文请求应读取 -en 文件，并与中文缓存隔离。 */
    @Test
    public void shouldLoadEnglishResourceWithIndependentCache() {
        Locale.setDefault(Locale.CHINESE);
        JSONObject chineseTable = service.getTable("report", "report");

        Locale.setDefault(Locale.ENGLISH);
        JSONObject englishTable = service.getTable("report", "report");

        assertNotNull(chineseTable);
        assertNotNull(englishTable);
        assertEquals("Report Template Table", englishTable.getString("label"));
        assertFalse(chineseTable.getString("label").equals(englishTable.getString("label")));
        assertEquals("-en", service.getCurrentLanguageSuffix());
    }

    /** 英文关键词只匹配英文索引中的文案。 */
    @Test
    public void shouldSearchEnglishIndex() {
        Locale.setDefault(Locale.ENGLISH);

        JSONArray tableList = service.searchTable("report", "Report Template Table");

        assertFalse(tableList.isEmpty());
        assertEquals("report", tableList.getJSONObject(0).getString("name"));
    }

    /** 英文索引或详情缺失时不应回退到中文资源。 */
    @Test
    public void shouldNotFallbackWhenEnglishResourceIsMissing() {
        Locale.setDefault(Locale.ENGLISH);

        assertTrue(service.searchTable("apm", null).isEmpty());
        assertNull(service.getTable("report", "missing_table"));
    }
}
