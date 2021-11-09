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
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DrawTable implements TemplateMethodModelEx {
    // private static final Log logger = LogFactory.getLog(DrawTable.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        String title = null, header = null, column = null;
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
                title = configObj.getString("title");
                header = configObj.getString("header");
                column = configObj.getString("column");
            } catch (Exception ex) {
                // 非json格式
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"ivu-card ivu-card-dis-hover ivu-card-shadow\">");
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
        sb.append("</table></div></div></div>");
        return sb.toString();
    }

}
