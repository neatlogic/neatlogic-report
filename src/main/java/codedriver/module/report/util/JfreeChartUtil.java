package codedriver.module.report.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.ui.RectangleInsets;

public class JfreeChartUtil {

	private JfreeChartUtil() {

	}

	private static Font FONT = new Font("黑体", Font.PLAIN, 12);
	private static Font FONT_TITLE = new Font("黑体", Font.PLAIN, 14);
	public static Color[] CHART_COLORS = { new Color(31, 129, 188), new Color(92, 92, 97), new Color(144, 237, 125), new Color(255, 188, 117), new Color(153, 158, 255), new Color(255, 117, 153), new Color(253, 236, 109), new Color(128, 133, 232), new Color(158, 90, 102), new Color(255, 204, 102) };// 颜色

	/**
	 * 获取主题
	 * 
	 * @return
	 */
	public static StandardChartTheme getStandardChartTheme() {
		StandardChartTheme standardChartTheme = new StandardChartTheme("CN");

		// 设置标题字体
		standardChartTheme.setExtraLargeFont(FONT_TITLE);
		// 设置图例的字体
		standardChartTheme.setRegularFont(FONT);
		// 设置轴向的字体
		standardChartTheme.setLargeFont(FONT);
		standardChartTheme.setSmallFont(FONT);
		standardChartTheme.setTitlePaint(new Color(51, 51, 51));
		standardChartTheme.setSubtitlePaint(new Color(85, 85, 85));

		standardChartTheme.setLegendBackgroundPaint(Color.WHITE);// 设置标注
		standardChartTheme.setLegendItemPaint(Color.BLACK);//
		standardChartTheme.setChartBackgroundPaint(Color.WHITE);
		// 绘制颜色绘制颜色.轮廓供应商
		// paintSequence,outlinePaintSequence,strokeSequence,outlineStrokeSequence,shapeSequence

		Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[] { Color.WHITE };
		// 绘制器颜色源
		DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(CHART_COLORS, CHART_COLORS, OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
		standardChartTheme.setDrawingSupplier(drawingSupplier);

		standardChartTheme.setPlotBackgroundPaint(Color.WHITE);// 绘制区域
		standardChartTheme.setPlotOutlinePaint(Color.WHITE);// 绘制区域外边框
		standardChartTheme.setLabelLinkPaint(new Color(8, 55, 114));// 链接标签颜色
		standardChartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);

		standardChartTheme.setAxisOffset(new RectangleInsets(5, 12, 5, 12));
		standardChartTheme.setDomainGridlinePaint(new Color(192, 208, 224));// X坐标轴垂直网格颜色
		standardChartTheme.setRangeGridlinePaint(new Color(192, 192, 192));// Y坐标轴水平网格颜色

		standardChartTheme.setBaselinePaint(Color.WHITE);
		standardChartTheme.setCrosshairPaint(Color.BLUE);// 不确定含义
		standardChartTheme.setAxisLabelPaint(new Color(51, 51, 51));// 坐标轴标题文字颜色
		standardChartTheme.setTickLabelPaint(new Color(67, 67, 72));// 刻度数字
		standardChartTheme.setBarPainter(new StandardBarPainter());// 设置柱状图渲染
		standardChartTheme.setXYBarPainter(new StandardXYBarPainter());// XYBar 渲染

		standardChartTheme.setItemLabelPaint(Color.black);
		standardChartTheme.setThermometerPaint(Color.white);// 温度计

		return standardChartTheme;
	}
}
