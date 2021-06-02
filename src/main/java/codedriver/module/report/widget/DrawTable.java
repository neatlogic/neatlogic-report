package codedriver.module.report.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawTable implements TemplateMethodModelEx {
	// private static final Log logger = LogFactory.getLog(DrawTable.class);

	@SuppressWarnings("unchecked")
    @Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		String title = null, header = null, column = null;
		SimpleSequence ss = null;
		List<String> keyList = new ArrayList<String>();
		List<String> headerList = new ArrayList<String>();
		List<String> columnList = new ArrayList<String>();
		if (arguments.size() >= 1) {
			ss = arguments.get(0) instanceof SimpleSequence ? (SimpleSequence) arguments.get(0) : null;
			if (ss != null && ss.size() > 0) {
				// 取得第一行数据，得到表格列名
				SimpleHash sm = (SimpleHash) ss.get(0);
				Map<String, Object> colMap = sm.toMap();
				Iterator<Map.Entry<String, Object>> iter = colMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, Object> entry = iter.next();
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
			sb.append("<div class=\"ivu-card-head\">" + title + "</div>");
		}
		sb.append("<div class=\"ivu-card-body tstable-container\"><table class=\"tstable-body\">");

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
				sb.append("<th>" + head + "</th>");
			}
			sb.append("</tr></thead>");
		}

		if (columnList.size() > 0 && ss != null) {
			sb.append("<tbody>");
			for (int i = 0; i < ss.size(); i++) {
				if (ss.get(i) instanceof SimpleHash) {
					SimpleHash sm = (SimpleHash) ss.get(i);
					sb.append("<tr>");
					for (String col : columnList) {

						sb.append("<td>" + sm.get(col) + "</td>");
					}
					sb.append("</tr>");
				}
			}
			sb.append("</tbody>");

		} else {
			sb.append("无数据");
		}
		sb.append("</table></div></div>");
		return sb.toString();
	}

}
