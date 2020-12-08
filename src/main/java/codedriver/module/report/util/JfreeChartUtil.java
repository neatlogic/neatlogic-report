package codedriver.module.report.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import codedriver.module.report.constvalue.ActionType;


public class JfreeChartUtil {
    private static final Log logger = LogFactory.getLog(JfreeChartUtil.class);
    private JfreeChartUtil() {

    }

    private static Font FONT = new Font("黑体", Font.PLAIN, 12);
    private static Font FONT_TITLE = new Font("黑体", Font.PLAIN, 14);
    public static Color[] CHART_COLORS = {new Color(31, 129, 188), new Color(92, 92, 97), new Color(144, 237, 125),
        new Color(255, 188, 117), new Color(153, 158, 255), new Color(255, 117, 153), new Color(253, 236, 109),
        new Color(128, 133, 232), new Color(158, 90, 102), new Color(255, 204, 102)};// 颜色

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

        Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[] {Color.WHITE};
        // 绘制器颜色源
        DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(CHART_COLORS, CHART_COLORS,
            OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
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

    /**
     * @Author 89770
     * @Time 2020年12月7日
     * @Description: chart转svg
     * @Param
     * @return
     */
    private static String getChartAsSVG(JFreeChart chart, int width, int height) {
        SVGGraphics2D g2 = new SVGGraphics2D(width, height);
        Rectangle r = new Rectangle(0, 0, width, height);
        chart.draw(g2, r);
        return g2.getSVGElement();
    }

    /**
    * @Author 89770
    * @Time 2020年12月8日  
    * @Description: chart转png 
    * @Param 
    * @return
     */
    private static String getChartAsPNG(JFreeChart chart, int width, int height) {
        try {
            byte[] bytes = ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
            return "<img src=\"data:image/png;base64," + Base64.encodeBase64String(bytes) + "\">";
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }
    
    /**
    * @Author 89770
    * @Time 2020年12月8日  
    * @Description: 获取chart String, 查看、发邮件获取svg，导出获取png
    * @Param 
    * @return
     */
    public static String getChartString(String actionType,JFreeChart chart, int width, int height) {
        if(ActionType.VIEW.getValue().equals(actionType)) {
            return getChartAsSVG(chart,width,height);
        }else {
            return getChartAsPNG(chart,width,height);
        }
    }
}
