package codedriver.module.report.util;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.StandardChartTheme;

public class JfreeChartUtil {

	private JfreeChartUtil() {

	}

	/**
	 * 获取主题
	 * 
	 * @return
	 */
	public static StandardChartTheme getStandardChartTheme() {
		StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
		standardChartTheme.setExtraLargeFont(new Font("黑体", Font.PLAIN, 12 + 4));
		standardChartTheme.setRegularFont(new Font("黑体", Font.PLAIN, 12));
		standardChartTheme.setLargeFont(new Font("黑体", Font.PLAIN, 12 + 2));
		standardChartTheme.setRangeGridlinePaint(Color.decode("#dddddd"));
		standardChartTheme.setDomainGridlinePaint(Color.decode("#dddddd"));
		standardChartTheme.setChartBackgroundPaint(Color.white);
		standardChartTheme.setPlotBackgroundPaint(Color.white);

		return standardChartTheme;
	}
}
