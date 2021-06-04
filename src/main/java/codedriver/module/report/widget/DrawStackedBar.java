package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.report.util.JfreeChartUtil;
import codedriver.module.report.util.JfreeChartUtil.ChartColor;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawStackedBar implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawStackedBar.class);
	private String actionType ;

    public DrawStackedBar(String actionType) {
        this.actionType = actionType ;
    }
	@SuppressWarnings({"unchecked", "rawtypes"})
    @Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 1000;
		int height = 400;
		int tick = 0;
		String title = "", xLabel = "", yLabel = "";
		boolean isShowValue = true;

		CategoryDataset dataset = null;
		if (arguments.size() >= 1) {
			if (arguments.get(0) instanceof SimpleSequence) {
				SimpleSequence ss = (SimpleSequence) arguments.get(0);
				if (ss.size() > 0) {
					List<String> rowList = new ArrayList<String>();
					List<String> columnList = new ArrayList<String>();
					double[][] dataList = new double[ss.size()][];
					for (int i = 0; i < ss.size(); i++) {
						SimpleHash sm = (SimpleHash) ss.get(i);
						if (sm.containsKey("groupField")) {
							rowList.add(sm.get("groupField").toString());
						} else {
							throw new RuntimeException("堆积图数据集缺少groupField字段");
						}
						if (sm.containsKey("dataList")) {
							SimpleSequence valueItemList = (SimpleSequence) sm.get("dataList");
							double[] dList = new double[valueItemList.size()];
							for (int j = 0; j < valueItemList.size(); j++) {
								SimpleHash valueItem = (SimpleHash) valueItemList.get(j);
								if (valueItem.containsKey("typeField")) {
									if (!columnList.contains(valueItem.get("typeField").toString())) {
										columnList.add(valueItem.get("typeField").toString());
									}
								} else {
									throw new RuntimeException("堆积图数据集dataList属性中缺少typeField字段");
								}
								if (valueItem.containsKey("valueField")) {
									dList[j] = Double.parseDouble(((SimpleNumber) valueItem.get("valueField")).toString());
								} else {
									throw new RuntimeException("堆积图数据集dataList属性中缺少valueField字段");
								}
							}
							dataList[i] = dList;
						} else {
							throw new RuntimeException("堆积图数据集缺少dataList属性");
						}
					}
					dataset = DatasetUtils.createCategoryDataset(rowList.toArray(new String[rowList.size()]), columnList.toArray(new String[columnList.size()]), dataList);
				}
			}
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
			    logger.error(ex.getMessage(), ex);
			}
		}

		StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);

		JFreeChart chart = ChartFactory.createStackedBarChart(title, xLabel, yLabel, dataset);
		standardChartTheme.apply(chart);
		chart.getLegend().setFrame(new BlockBorder(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType)));

		CategoryPlot p = chart.getCategoryPlot();
		CategoryItemRenderer renderer = new DrawStackedBar.CustomRenderer(actionType);
		renderer.setDefaultItemLabelsVisible(isShowValue);
		p.setRenderer(renderer);
		p.setOutlinePaint(Color.white);
		p.setNoDataMessage("无数据");
		CategoryAxis axis = (CategoryAxis) p.getDomainAxis();// X坐标轴
		axis.setLowerMargin(0);
		axis.setUpperMargin(0);
		axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);// 倾斜45度
		if (tick > 0) {
			List<String> xLables = dataset.getColumnKeys();
			for (int k = 0; k < xLables.size(); k++) {
				if (k % tick != 0) {
					axis.setTickLabelPaint(xLables.get(k), Color.WHITE);
				}
			}
		}
		return JfreeChartUtil.getChartString(actionType, chart, width, height);

	}

	static class CustomRenderer extends StackedBarRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 3946096994457346387L;
		private Paint[] colors;

		public CustomRenderer(String actionType) {
			this.setBarPainter(new StandardBarPainter());
			this.setShadowVisible(false);
			super.setDefaultShadowsVisible(false);
			this.setDefaultItemLabelsVisible(true);
			this.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			this.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
			this.setItemLabelAnchorOffset(-10D);
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
