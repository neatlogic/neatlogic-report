package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.config.ReportConfig;
import codedriver.module.report.util.JfreeChartUtil;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawPie implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawPie.class);

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 500;
		int height = 500;
		DefaultPieDataset dataset = new DefaultPieDataset();
		String title = "";
		if (arguments.size() >= 1) {
			if (arguments.get(0) instanceof SimpleSequence) {
				SimpleSequence ss = (SimpleSequence) arguments.get(0);
				for (int i = 0; i < ss.size(); i++) {
					SimpleHash sm = (SimpleHash) ss.get(i);
					if (sm.get("typeField") != null && sm.get("valueField") != null) {
						SimpleNumber v = (SimpleNumber) sm.get("valueField");
						dataset.setValue(sm.get("typeField").toString(), v.getAsNumber());
					}
				}
			} else {
				canReturn = false;
			}
		} else {
			canReturn = false;
		}

		if (arguments.size() >= 2) {
			String config = arguments.get(1).toString();
			try {
				JSONObject configObj = JSONObject.parseObject(config);
				title = configObj.getString("title");
				if (configObj.getIntValue("width") > 0) {
					width = configObj.getIntValue("width");
				}
				if (configObj.getIntValue("height") > 0) {
					height = configObj.getIntValue("height");
				}
			} catch (Exception ex) {
				// 非json格式
			}
		}

		if (canReturn) {
			StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme();
			JFreeChart chart = ChartFactory.createPieChart(title, dataset);
			standardChartTheme.apply(chart);
			chart.getLegend().setFrame(new BlockBorder(Color.white));
			PiePlot p = (PiePlot) chart.getPlot();

			CustomRenderer renderer = new CustomRenderer();
			renderer.setColor(p, dataset);
			try {
				byte[] bytes = ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
				return "<img src=\"data:image/png;base64," + Base64.encodeBase64String(bytes) + "\">";
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}

	static class CustomRenderer {
		// private Paint[] colors;

		public CustomRenderer() {
			// this.colors = JfreeChartUtil.CHART_COLORS;
		}

		public void setColor(PiePlot plot, DefaultPieDataset dataset) {

			plot.setNoDataMessage("数据加载失败");
			plot.setInsets(new RectangleInsets(10, 10, 5, 10));
			PiePlot piePlot = (PiePlot) plot;
			piePlot.setInsets(new RectangleInsets(0, 0, 0, 0));
			piePlot.setCircular(true);// 圆形

			// piePlot.setSimpleLabels(true);// 简单标签
			piePlot.setLabelGap(0.01);
			piePlot.setInteriorGap(0.05D);
			piePlot.setLegendItemShape(new Rectangle(10, 10));// 图例形状
			piePlot.setIgnoreNullValues(true);
			piePlot.setLabelBackgroundPaint(null);// 去掉背景色
			piePlot.setLabelShadowPaint(null);// 去掉阴影
			piePlot.setLabelOutlinePaint(null);// 去掉边框
			piePlot.setShadowPaint(null);
			piePlot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0}：{1}({2})"));// 设置legend显示格式
			piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({2})", NumberFormat.getNumberInstance(), new DecimalFormat("0.00%")));// 设置标签带%

		}
	}

}
