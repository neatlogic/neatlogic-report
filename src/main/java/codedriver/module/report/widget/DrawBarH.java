package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Paint;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
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

import codedriver.module.report.util.JfreeChartUtil;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawBarH implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawBarH.class);
	private String actionType;
	//报表中所有数据源
	private Map<String, Object> reportMap;
	
	public DrawBarH(Map<String, Object> reportMap, String actionType) {
	    this.reportMap = reportMap;
	    this.actionType = actionType;
	}

    @SuppressWarnings("unchecked")
    @Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		int width = 1000;
		int height = 600;
		int tick = 0;
		boolean isShowValue = true;
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		String title = "", xLabel = "", yLabel = "", data = "";

		if (arguments.size() >= 1) {
			String config = arguments.get(0).toString();
			try {
				JSONObject configObj = JSONObject.parseObject(config);
				data = configObj.getString("data");
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
			    logger.error(ex.getMessage(), ex);
			}
		}

		List<Map<String, Object>> tbodyList = (List<Map<String, Object>>) reportMap.get(data);
		if (CollectionUtils.isNotEmpty(tbodyList)) {
			for (Map<String, Object> tbody : tbodyList) {
				Number valuekey = (Number) tbody.get("yField");
				Object rowkey = tbody.get("xField");
				Object columnkey = tbody.get("groupField");
				if (valuekey != null && rowkey != null) {
					if (columnkey == null) {
						columnkey = "";
					}
					dataset.addValue(valuekey, rowkey.toString(), columnkey.toString());
				}
			}
		}
		StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);

		JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, true, false, false);
		standardChartTheme.apply(chart);
		chart.getLegend().setFrame(new BlockBorder(new Color(10, 10, 10)));

		CategoryPlot p = chart.getCategoryPlot();
		CategoryItemRenderer renderer = new DrawBarH.CustomRenderer(actionType);
		renderer.setDefaultItemLabelsVisible(isShowValue);
		p.setRenderer(renderer);
		p.setOutlinePaint(Color.white);
		p.setNoDataMessage("无数据");
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

		return JfreeChartUtil.getChartString(actionType, chart, width, height);
	}

	static class CustomRenderer extends BarRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 2435957652029996238L;
		private Paint[] colors;

		public CustomRenderer(String actionType) {
			this.setBarPainter(new StandardBarPainter());
			this.setShadowVisible(false);
			super.setDefaultShadowsVisible(false);
			this.setDefaultItemLabelsVisible(true);
			this.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			this.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_RIGHT));
			this.setItemLabelAnchorOffset(30D);
			this.colors = JfreeChartUtil.getCharColors(actionType);
			// this.setDefaultItemLabelFont(new Font("黑体", Font.PLAIN,
			// ReportConfig.JFREECHART_FONTSIZE + 2));
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
