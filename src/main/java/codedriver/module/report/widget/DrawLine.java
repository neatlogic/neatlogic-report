package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Paint;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.config.ReportConfig;
import codedriver.module.report.util.JfreeChartUtil;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawLine implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawLine.class);

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 1000;
		int height = 400;
		int tick = 0;
		boolean isShowValue = true;
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String title = "", xLabel = "", yLabel = "";
		if (arguments.size() >= 1) {
			if (arguments.get(0) instanceof SimpleSequence) {
				SimpleSequence ss = (SimpleSequence) arguments.get(0);
				Boolean display = false;
				if (ss.size() > 20) {
					display = true;
				}
				for (int i = 0; i < ss.size(); i++) {
					SimpleHash sm = (SimpleHash) ss.get(i);
					String series = null;
					SimpleNumber y = null;
					String x = null;
					if (sm.get("groupField") != null) {
						series = sm.get("groupField").toString();
					}
					if (sm.get("yField") != null) {
						y = (SimpleNumber) sm.get("yField");
					}
					if (sm.get("xField") != null) {
						x = sm.get("xField").toString();
					}
					if (series != null && y != null && x != null) {
						dataset.addValue(y.getAsNumber(), series, x);
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
				xLabel = configObj.getString("xLabel");
				yLabel = configObj.getString("yLabel");
				if (configObj.getIntValue("width") > 0) {
					width = configObj.getIntValue("width");
				}
				if (configObj.getIntValue("height") > 0) {
					height = configObj.getIntValue("height");
				}
				tick = configObj.getIntValue("tick");
				if (configObj.containsKey("isShowValue")) {
					isShowValue = configObj.getBooleanValue("isShowValue");
				}
			} catch (Exception ex) {
				// 非json格式
			}
		}

		if (canReturn) {
			StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme();

			JFreeChart chart = ChartFactory.createLineChart(title, xLabel, yLabel, dataset);
			standardChartTheme.apply(chart);
			chart.getLegend().setFrame(new BlockBorder(Color.white));
			CategoryPlot p = chart.getCategoryPlot();
			CategoryItemRenderer render = new DrawLine.CustomRenderer();
			render.setDefaultItemLabelsVisible(isShowValue);
			p.setRenderer(render);
			p.setOutlinePaint(null);
			CategoryAxis axis = (CategoryAxis) p.getDomainAxis();// X坐标轴
			if (tick > 0) {
				List<String> xLables = dataset.getColumnKeys();
				for (int k = 0; k < xLables.size(); k++) {
					if (k % tick != 0) {
						axis.setTickLabelPaint(xLables.get(k), Color.WHITE);
					}
				}
			}
			axis.setLowerMargin(0);
			axis.setUpperMargin(0);
			axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);// 倾斜45度
			ValueAxis yAxis = p.getRangeAxis();// 对Y轴做操作 q
			yAxis.setAutoRange(true); // 设置y轴自动获取范围
			try {
				byte[] bytes = ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
				return "<img class='img-responsive' src=\"data:image/png;base64," + Base64.encodeBase64String(bytes) + "\">";
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}

	static class CustomRenderer extends LineAndShapeRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 2892093102930999651L;
		private Paint[] colors;

		public CustomRenderer() {
			this.colors = JfreeChartUtil.CHART_COLORS;
		}

		public Paint getItemPaint(final int row, final int column) {
			return (this.colors[row % this.colors.length]);
		}

		public LegendItem getLegendItem(int dataset, int series) {
			LegendItem legendItem = super.getLegendItem(dataset, series);
			legendItem.setFillPaint(this.colors[series % this.colors.length]);
			return legendItem;
		}
	}

}
