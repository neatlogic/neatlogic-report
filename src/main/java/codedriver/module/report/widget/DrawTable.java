/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.report.widget;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DrawTable implements TemplateMethodModelEx {
    // private static final Log logger = LogFactory.getLog(DrawTable.class);

    //过滤条件
    private JSONObject filter;

    private Map<String, Map<String, Object>> pageMap;

    public DrawTable() {}

    public DrawTable(JSONObject filter, Map<String, Map<String, Object>> pageMap) {
        this.filter = filter;
        this.pageMap = pageMap;
    }

    public JSONObject getFilter() {
        if (filter == null) {
            filter = new JSONObject();
        }
        return filter;
    }

    public void setFilter(JSONObject filter) {
        this.filter = filter;
    }

    public Map<String, Map<String, Object>> getPageMap() {
        return pageMap;
    }

    public void setPageMap(Map<String, Map<String, Object>> pageMap) {
        this.pageMap = pageMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String title = null, header = null, column = null, id = null;
        Boolean needPage = null;
        SimpleSequence ss = null;
        List<String> keyList = new ArrayList<>();
        List<String> headerList = new ArrayList<>();
        List<String> columnList = new ArrayList<>();
        if (arguments.size() >= 1) {
            ss = arguments.get(0) instanceof SimpleSequence ? (SimpleSequence) arguments.get(0) : null;
            if (ss != null && ss.size() > 0) {
                // 取得第一行数据，得到表格列名
                SimpleHash sm = (SimpleHash) ss.get(0);
                Map<String, Object> colMap = sm.toMap();
                for (Map.Entry<String, Object> entry : colMap.entrySet()) {
                    if (!entry.getKey().equals("UUID")) {// UUID是系统生成字段
                        keyList.add(entry.getKey());
                    }
                }
            }
        }

        if (arguments.size() >= 2) {
            String config = arguments.get(1).toString();
            try {
                JSONObject configObj = JSONObject.parseObject(config);
                id = configObj.getString("id");
                title = configObj.getString("title");
                header = configObj.getString("header");
                column = configObj.getString("column");
                needPage = configObj.getBoolean("needPage");
            } catch (Exception ex) {
                // 非json格式
            }
        }
        needPage = needPage == null ? false : needPage;
        Integer currentPage = 1;
        Integer pageSize = 20;
        Integer pageCount = 0;
        Integer rowNum = 0;
        if (needPage) {
            if (MapUtils.isNotEmpty(pageMap)) {
                Map<String, Object> basePageMap = pageMap.get(id);
                if (MapUtils.isNotEmpty(basePageMap)) {
                    currentPage = (Integer) basePageMap.get("currentPage");
                    pageSize = (Integer) basePageMap.get("pageSize");
                    pageCount = (Integer) basePageMap.get("pageCount");
                    rowNum = (Integer) basePageMap.get("rowNum");
                    currentPage = currentPage == null ? 1 : currentPage;
                    pageSize = pageSize == null ? 20 : pageSize;
                    pageCount = pageCount == null ? 0 : pageCount;
                    rowNum = rowNum == null ? 0 : rowNum;
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=\"" + id + "\" class=\"ivu-card ivu-card-dis-hover ivu-card-shadow\">");
        if (StringUtils.isNotBlank(title)) {
            sb.append("<div class=\"ivu-card-head\">").append(title).append("</div>");
        }
        sb.append("<div class=\"ivu-card-body tstable-container tstable-normal border tstable-no-fixedHeader block-large\"><div class=\"tstable-main bg-op\"><table class=\"table-main tstable-body\">");

        if (header == null || header.trim().equals("")) {
            headerList = keyList;
        } else {
            headerList = Arrays.asList(header.split(","));
        }

        if (column == null || column.equals("")) {
            columnList = keyList;
        } else {
            columnList = Arrays.asList(column.split(","));
        }

        if (headerList.size() > 0) {
            sb.append("<thead><tr class=\"th-left\">");
            for (String head : headerList) {
                sb.append("<th>").append(head).append("</th>");
            }
            sb.append("</tr></thead>");
        }

        if (columnList.size() > 0 && ss != null) {
            sb.append("<tbody class=\"tbody-main\">");
            for (int i = 0; i < ss.size(); i++) {
                if (ss.get(i) instanceof SimpleHash) {
                    SimpleHash sm = (SimpleHash) ss.get(i);
                    sb.append("<tr>");
                    for (String col : columnList) {
                        sb.append("<td>").append(sm.get(col)).append("</td>");
                    }
                    sb.append("</tr>");
                }
            }
            sb.append("</tbody>");

        } else {
            sb.append("<tbody class=\"tbody-main\"><tr><td>无数据</td></tr></tbody>");
        }
        sb.append("</table></div></div>");

        if (needPage) {
            sb.append("<div><div class='tstable-page text-right'><ul tableid='" + id + "' class='ivu-page mini'><span class='ivu-page-total'>共 ");
            sb.append(rowNum);
            sb.append(" 条</span>");
            int prevPage = currentPage - 1;
            sb.append("<li title='上一页' page='" + prevPage + "' class='page ivu-page-prev" + (prevPage < 1 ? " ivu-page-disabled'" : "'") + "><a><i class='ivu-icon ivu-icon-ios-arrow-back'></i></a></li>");
            for (int i = 1; i <= pageCount; i++) {
                sb.append("<li title='" + i + "' page='" + i + "' class='page ivu-page-item");
                if (Objects.equals(currentPage, i)) {
                    sb.append(" ivu-page-item-active");
                }
                sb.append("'><a>" + i + "</a></li>");
            }
            int nextPage = currentPage + 1;
            sb.append("<li title='下一页' page='" + nextPage + "' class='page ivu-page-next" + (nextPage > pageCount ? " ivu-page-disabled'" : "'") + "><a><i class='ivu-icon ivu-icon-ios-arrow-forward'></i></a></li>");
            sb.append("<div class='ivu-page-options'><div class='ivu-page-options-sizer'><div class='ivu-select ivu-select-single ivu-select-small'><div tabindex='0' class='ivu-select-selection'><input type='hidden' value='20'><div><span class='ivu-select-selected-value'>");
            sb.append(pageSize);
            sb.append(" 条/页</span><i class='ivu-icon ivu-icon-ios-arrow-down ivu-select-arrow'></i></div></div></div></div></div>");
            sb.append("</ul></div></div>");
            sb.append("<script>");
            sb.append("$(function(){");
            sb.append("$('.page').click(function(){");
            sb.append("var pageCount = " + pageCount + ";");
            sb.append("var currentPage = $(this).attr('page');");
            sb.append("if (currentPage < 1 || currentPage > pageCount) { return;}");
            sb.append("var tableId = $(this).parent().attr('tableid');");
            sb.append("var paramObj = " + getFilter().toJSONString() + ";");
            sb.append("paramObj.tableId = tableId;");
            sb.append("paramObj.currentPage = currentPage;");
            sb.append("paramObj.pageSize = " + pageSize + ";");
            JSONObject ajaxObj = new JSONObject();
            ajaxObj.put("url", "api/binary/report/table/get");
            ajaxObj.put("type", "POST");
            ajaxObj.put("contentType", "application/json");
            ajaxObj.put("async", false);
            sb.append("var ajax = " + ajaxObj.toJSONString() + ";");
            sb.append("ajax.data = JSON.stringify(paramObj);");
            sb.append("htmlobj=$.ajax(ajax);");
            sb.append("$('#' + tableId).parent().html(htmlobj.responseText);");
            sb.append("});");
            sb.append("});");
            sb.append("</script>");
        }
        sb.append("</div>");
        return sb.toString();
    }

}
