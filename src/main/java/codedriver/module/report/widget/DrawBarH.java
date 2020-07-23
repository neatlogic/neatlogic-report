package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Font;
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
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.config.ReportConfig;
import codedriver.module.report.util.JfreeChartUtil;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawBarH implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawBarH.class);

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 1000;
		int height = 600;
		int tick = 0;
		boolean isShowValue = true;
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String title = "", xLabel = "", yLabel = "";
		if (arguments.size() >= 1) {
			if (arguments.get(0) instanceof SimpleSequence) {
				SimpleSequence ss = (SimpleSequence) arguments.get(0);
				for (int i = 0; i < ss.size(); i++) {
					SimpleHash sm = (SimpleHash) ss.get(i);
					SimpleNumber valuekey = null;
					String rowkey = null, columnkey = "";
					if (sm.get("xField") != null) {
						valuekey = (SimpleNumber) sm.get("xField");
					}
					if (sm.get("yField") != null) {
						rowkey = sm.get("yField").toString();
					}
					if (sm.get("groupField") != null) {
						columnkey = sm.get("groupField").toString();
					}
					if (valuekey != null && rowkey != null) {
						dataset.addValue(valuekey.getAsNumber(), rowkey, columnkey);
					}
				}
				if (dataset.getRowCount() <= 0) {
					canReturn = false;
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

			JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, true, false, false);
			standardChartTheme.apply(chart);
			chart.getLegend().setFrame(new BlockBorder(Color.white));

			CategoryPlot p = chart.getCategoryPlot();
			CategoryItemRenderer renderer = new DrawBarH.CustomRenderer();
			renderer.setDefaultItemLabelsVisible(isShowValue);
			p.setRenderer(renderer);
			p.setOutlinePaint(Color.white);
			
			CategoryAxis yxis = (CategoryAxis) p.getDomainAxis();// Y坐标轴
			yxis.setLowerMargin(0);
			yxis.setUpperMargin(0);
			yxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);// 倾斜45度
			if (tick > 0) {
				List<String> xLables = dataset.getColumnKeys();
				for (int k = 0; k < xLables.size(); k++) {
					if (k % tick != 0) {
						yxis.setTickLabelPaint(xLables.get(k), Color.WHITE);
					}
				}
			}
			
			try {
				byte[] bytes = ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
				return "<img class='img-responsive' src=\"data:image/png;base64," + Base64.encodeBase64String(bytes) + "\"/>";
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}

	static class CustomRenderer extends BarRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 2435957652029996238L;
		private Paint[] colors;

		public CustomRenderer() {
			this.setBarPainter(new StandardBarPainter());
			this.setShadowVisible(false);
			this.setDefaultShadowsVisible(false);
			this.setDefaultItemLabelsVisible(true);
			this.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			this.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_RIGHT));
			this.setItemLabelAnchorOffset(30D);
			this.colors = ReportConfig.CHART_COLOR;
			this.setDefaultItemLabelFont(new Font("黑体", Font.PLAIN, ReportConfig.JFREECHART_FONTSIZE + 2));
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
