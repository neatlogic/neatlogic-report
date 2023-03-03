package neatlogic.module.report.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONObject;

public class ReportSortFilter {
	public static String removeHtml(String htmlStr) {
		return removeHtml(htmlStr, null);
	}

	// 服务台排序功能，过滤掉非规定字段。
	private static List<String> needFilterList = new ArrayList<String>();
	private static List<String> needSortList = new ArrayList<String>();

	static {
		needFilterList.add("id");
		needFilterList.add("name");
		needFilterList.add("type");
		needFilterList.add("isenable");
		needFilterList.add("visitcount");

		needSortList.add("desc");
		needSortList.add("asc");
	}

	public static String getFilterSort(String sort) {
		String[] tmpSort = sort.split(",");
		StringBuffer returnStr = new StringBuffer();
		if (tmpSort != null && tmpSort.length > 0) {
			String[] tmp = null;
			for (String string : tmpSort) {
				tmp = string.split(" ");
				if (tmp != null && tmp.length > 1) {
					if (needFilterList.contains(tmp[0].toLowerCase()) && needSortList.contains(tmp[1].toLowerCase())) {
						returnStr.append(tmp[0]).append(" ").append(tmp[1]).append(",");
					}
				}
			}
		}
		if (returnStr.length() > 0) {
			returnStr.deleteCharAt(returnStr.length() - 1);
		}
		return returnStr.toString();
	}

	public static String removeHtml(String htmlStr, Integer length) {
		final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
		final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
		final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
		Pattern p_script = Pattern.compile(regEx_script, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		Pattern p_style = Pattern.compile(regEx_style, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签

		Pattern p_html = Pattern.compile(regEx_html, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签

		if (length != null && length > 0) {
			if (htmlStr.length() <= length) {
				return htmlStr;
			} else {
				htmlStr = htmlStr.substring(0, length) + "...";
			}
		}
		return htmlStr;
	}

	public static void verificationParams(String[] arrayParams, JSONObject jsonObj) {
		for (int i = 0; i < arrayParams.length; i++) {
			if (jsonObj == null || !jsonObj.containsKey(arrayParams[i]) || jsonObj.getString(arrayParams[i]).equals("")) {
				throw new RuntimeException("lack param: " + arrayParams[i]);
			}
		}

	}
}
