package neatlogic.module.report.util;

import neatlogic.framework.util.I18nUtils;
import neatlogic.module.report.constvalue.ActionType;
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

import java.awt.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JfreeChartUtil {
    private static final Log logger = LogFactory.getLog(JfreeChartUtil.class);

    private JfreeChartUtil() {

    }

    private static Font FONT = new Font("黑体", Font.PLAIN, 12);
    private static Font FONT_TITLE = new Font("黑体", Font.PLAIN, 14);
    public static ChartColor[] CHART_COLOR_ARRAY =
        {ChartColor.COLOR1, ChartColor.COLOR2, ChartColor.COLOR3, ChartColor.COLOR4, ChartColor.COLOR5,
            ChartColor.COLOR6, ChartColor.COLOR7, ChartColor.COLOR8, ChartColor.COLOR9, ChartColor.COLOR10};

    /**
     * 获取主题
     * 
     * @return
     */
    public static StandardChartTheme getStandardChartTheme(String actionType) {
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");

        // 设置标题字体
        standardChartTheme.setExtraLargeFont(FONT_TITLE);
        // 设置图例的字体
        standardChartTheme.setRegularFont(FONT);
        // 设置轴向的字体
        standardChartTheme.setLargeFont(FONT);
        standardChartTheme.setSmallFont(FONT);
        standardChartTheme.setTitlePaint(ChartColor.TITLE_COLOR.getColor(actionType));// 标题字体颜色
        // standardChartTheme.setSubtitlePaint(new Color(2, 2, 2));

        standardChartTheme.setLegendBackgroundPaint(ChartColor.LEGEND_BACKGROUND_COLOR.getColor(actionType));// 设置标注颜色
        standardChartTheme.setLegendItemPaint(ChartColor.LEGEND_ITEM_COLOR.getColor(actionType));// 标注字体颜色
        standardChartTheme.setChartBackgroundPaint(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType));// 背景颜色
        // 绘制颜色绘制颜色.轮廓供应商
        // paintSequence,outlinePaintSequence,strokeSequence,outlineStrokeSequence,shapeSequence

        Paint[] OUTLINE_PAINT_SEQUENCE = new Paint[] {Color.WHITE};
        // 绘制器颜色源
        Color[] chartColors = getCharColors(actionType);
        DefaultDrawingSupplier drawingSupplier = new DefaultDrawingSupplier(chartColors, chartColors,
            OUTLINE_PAINT_SEQUENCE, DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE, DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
        standardChartTheme.setDrawingSupplier(drawingSupplier);

        standardChartTheme.setPlotBackgroundPaint(ChartColor.PLOT_BACKGROUND_COLOR.getColor(actionType));// 绘制区域颜色
        standardChartTheme.setPlotOutlinePaint(ChartColor.PLOT_OUTLINE_COLOR.getColor(actionType));// 绘制区域外边框颜色
        standardChartTheme.setLabelLinkPaint(ChartColor.LABEL_LINK_COLOR.getColor(actionType));// 链接标签颜色
        standardChartTheme.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);

        standardChartTheme.setAxisOffset(new RectangleInsets(5, 12, 5, 12));
        standardChartTheme.setDomainGridlinePaint(ChartColor.DOMAIN_GRID_LINE_COLOR.getColor(actionType));// X坐标轴垂直网格颜色
        standardChartTheme.setRangeGridlinePaint(new Color(192, 192, 192));// Y坐标轴水平网格颜色

        standardChartTheme.setBaselinePaint(Color.WHITE);
        standardChartTheme.setCrosshairPaint(Color.BLUE);// 不确定含义

        standardChartTheme.setAxisLabelPaint(ChartColor.AXIS_LABEL_COLOR.getColor(actionType));// 坐标轴标题文字颜色
        standardChartTheme.setTickLabelPaint(ChartColor.TICK_LABEL_COLOR.getColor(actionType));// 刻度数字
        standardChartTheme.setBarPainter(new StandardBarPainter());// 设置柱状图渲染
        standardChartTheme.setXYBarPainter(new StandardXYBarPainter());// XYBar 渲染

        standardChartTheme.setItemLabelPaint(Color.WHITE);
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
        String element = g2.getSVGElement();
        element = replaceFill(element);
        element = relaceStroke(element);
        return element;
    }
    
    /**
    * @Author 89770
    * @Time 2020年12月14日  
    * @Description: 替换fill 颜色
    * @Param 
    * @return
     */
    private static String replaceFill(String svgStr) {
        for (ChartColor type : ChartColor.values()) {
            svgStr = svgStr.replaceAll(String.format("style=\"fill: rgb\\(%d,%d,%d\\);", type.getColorIndex(),
                type.getColorIndex(), type.getColorIndex()), String.format(" class=\"%s\" style=\"", type.getValue()));
        }
        return svgStr;
    }
    
    /**
     * @Author 89770
     * @Time 2020年12月14日  
     * @Description: 替换stroke 颜色
     * @Param 
     * @return
      */
     private static String relaceStroke(String svgStr) {
         for (ChartColor type : ChartColor.values()) {
             String strokeStr = StringUtils.EMPTY;
             String classStr = StringUtils.EMPTY;
             Pattern p = Pattern.compile(String.format("class=\"([^\"]+)\" (style=\"((?!stroke:)(?!><).)+;)stroke: rgb\\(%d,%d,%d\\);", type.getColorIndex(),type.getColorIndex(), type.getColorIndex()));
             Matcher m = p.matcher(svgStr);
             StringBuffer sb = new StringBuffer();
             //int i = 0;
             boolean result = m.find();
             //如果不存在class
             if(!result) {
                 p = Pattern.compile(String.format("(style=\"((?!stroke:)(?!><).)+;)stroke: rgb\\(%d,%d,%d\\);", type.getColorIndex(),type.getColorIndex(), type.getColorIndex()));
                 m = p.matcher(svgStr);
                 sb = new StringBuffer();
                 result = m.find();
                 classStr = type.getValue()+"Stroke";
                 if(result) {
                     strokeStr = m.group(1);
                 }
             }else {
                 classStr = String.format("%s %sStroke", m.group(1) ,type.getValue());
                 strokeStr = m.group(2);
             }
             while (result) {
                 //i++;
                 //System.out.println(m.group());
                 //System.out.println(m.group(1));
                 m.appendReplacement(sb, String.format(" class=\"%s\" %s",classStr, strokeStr));
                 //System.out.println("第" + i + "次匹配后 sb 的内容是：" + sb);
                 result = m.find();
             }
             m.appendTail(sb);
             svgStr = sb.toString();
         }
         return svgStr;
        //System.out.println("调用 m.appendTail(sb) 后 sb 的最终内容是 :" + sb.toString());
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
    public static String getChartString(String actionType, JFreeChart chart, int width, int height) {
        if (ActionType.VIEW.getValue().equals(actionType)) {
            return getChartAsSVG(chart, width, height);
        } else {
            return getChartAsPNG(chart, width, height);
        }
    }
    
    /**
     * 
    * @Author 89770
    * @Time 2020年12月10日  
    * @Description: 获取颜色板 
    * @Param 
    * @return
     */
    public static Color[] getCharColors(String actionType){
        Color[] chartColors = new Color[10];
        int i = 0;
        for(ChartColor c :CHART_COLOR_ARRAY) {
            chartColors[i] = c.getColor(actionType);
            i++;
        }
        return chartColors;
    }

    public enum ChartColor {

        TITLE_COLOR("titleColor", "enum.report.chartcolor.title_color", 1, new Color(51, 51, 51)),
        LEGEND_BACKGROUND_COLOR("legendBackgroundColor", "enum.report.chartcolor.legend_background_color", 2, Color.WHITE),
        LEGEND_ITEM_COLOR("legendItemColor", "enum.report.chartcolor.legend_item_color", 3, Color.BLACK),
        CHART_BACKGROUND_COLOR("chartBackgroundColor", "enum.report.chartcolor.chart_background_color", 4, Color.WHITE),
        PLOT_BACKGROUND_COLOR("plotBackgroundColor", "enum.report.chartcolor.plot_background_color", 5, Color.WHITE),
        PLOT_OUTLINE_COLOR("plotOutlineColor", "enum.report.chartcolor.plot_outline_color", 6, Color.WHITE,"stroke"),
        LABEL_LINK_COLOR("labelLinkColor", "enum.report.chartcolor.label_link_color", 7, new Color(8, 55, 114)),
        DOMAIN_GRID_LINE_COLOR("domainGridlineColor", "enum.report.chartcolor.domain_grid_line_color", 8, new Color(192, 208, 224)),
        RANG_GRID_LINE_COLOR("rangeGridlineColor", "enum.report.chartcolor.rang_grid_line_color", 9, new Color(192, 192, 192)),
        AXIS_LABEL_COLOR("axisLabelColor", "enum.report.chartcolor.axis_label_color", 10, new Color(51, 51, 51)),
        TICK_LABEL_COLOR("tickLabelColor", "enum.report.chartcolor.tick_label_color", 11, new Color(67, 67, 72)),

        // 色板
        COLOR1("color1", "enum.report.chartcolor.color1.a", 12, new Color(31, 129, 188)),
        COLOR2("color2", "enum.report.chartcolor.color2", 13, new Color(92, 92, 97)),
        COLOR3("color3", "enum.report.chartcolor.color3", 14, new Color(144, 237, 125)),
        COLOR4("color4", "enum.report.chartcolor.color4", 15, new Color(255, 188, 117)),
        COLOR5("color5", "enum.report.chartcolor.color5", 16, new Color(153, 158, 255)),
        COLOR6("color6", "enum.report.chartcolor.color6", 17, new Color(255, 117, 153)),
        COLOR7("color7", "enum.report.chartcolor.color7", 18, new Color(253, 236, 109)),
        COLOR8("color8", "enum.report.chartcolor.color8", 19, new Color(128, 133, 232)),
        COLOR9("color9", "enum.report.chartcolor.color9", 20, new Color(158, 90, 102)),
        COLOR10("color10", "enum.report.chartcolor.color10", 21, new Color(255, 204, 102));

        private String value;
        private String text;
        private int colorIndex;
        private Color exportColor;
        private String type;

        private ChartColor(String value, String text, int colorIndex, Color exportColor,String type) {
            this.value = value;
            this.text = text;
            this.colorIndex = colorIndex;
            this.exportColor = exportColor;
            this.type = type;
        }
        
        private ChartColor(String value, String text, int colorIndex, Color exportColor) {
            this.value = value;
            this.text = text;
            this.colorIndex = colorIndex;
            this.exportColor = exportColor;
            this.type = "fill";
        }

        public String getValue() {
            return value;
        }

        public String getText() {
            return I18nUtils.getMessage(text);
        }
        
        public String getType() {
            return type;
        }

        public Color getViewColor() {
            return new Color(colorIndex, colorIndex, colorIndex);
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public Color getExportColor() {
            return exportColor;
        }

        public Color getColor(String actionType) {
            if (ActionType.VIEW.getValue().equals(actionType)) {
                return new Color(colorIndex, colorIndex, colorIndex);
            } else {
                return exportColor;
            }
        }

        public static String getText(String _value) {
            for (ChartColor type : values()) {
                if (type.getValue().equals(_value)) {
                    return type.getText();
                }
            }
            return null;
        }

        public static Color getColor(String value, String actionType) {
            for (ChartColor type : values()) {
                if (type.getValue().equals(value)) {
                    if (ActionType.VIEW.getValue().equals(actionType)) {
                        return type.getViewColor();
                    } else {
                        return type.getExportColor();
                    }

                }
            }
            return null;
        }
        

    }
    
    
}
