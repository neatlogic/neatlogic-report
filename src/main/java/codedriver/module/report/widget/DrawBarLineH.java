package codedriver.module.report.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;

import codedriver.module.report.config.ReportConfig;
import codedriver.module.report.util.JfreeChartUtil;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
@Deprecated
public class DrawBarLineH implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawBarLineH.class);

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 1000;
		int height = 600;
		CategoryDataset datasetColumn = null;
		DefaultCategoryDataset dataSetLine = new DefaultCategoryDataset();

		String title = "", xLabel = "", yLabel = "";
		if (arguments.size() >= 1) {
			if (arguments.get(0) instanceof SimpleSequence) {
				SimpleSequence ss = (SimpleSequence) arguments.get(0);
				if (ss.size() > 0) {
					List<String> rowList = new ArrayList<String>();
					List<String> columnList = new ArrayList<String>();
					double[][] dataList = new double[ss.size()][];
					for (int i = 0; i < ss.size(); i++) {
						SimpleHash sm = (SimpleHash) ss.get(i);
						if (sm.containsKey("bar_row")) {
							rowList.add(sm.get("bar_row").toString());
						} else {
							throw new RuntimeException("堆积图数据集缺少bar_row字段");
						}
						if (sm.containsKey("bar_data")) {
							SimpleSequence valueItemList = (SimpleSequence) sm.get("bar_data");
							double[] dList = new double[valueItemList.size()];
							for (int j = 0; j < valueItemList.size(); j++) {
								SimpleHash valueItem = (SimpleHash) valueItemList.get(j);
								if (valueItem.containsKey("bar_column")) {
									if (!columnList.contains(valueItem.get("bar_column").toString())) {
										columnList.add(valueItem.get("bar_column").toString());
									}
								} else {
									throw new RuntimeException("堆积图数据集bar_data属性中缺少bar_column字段");
								}
								if (valueItem.containsKey("bar_value")) {
									dList[j] = Double.parseDouble(((SimpleNumber) valueItem.get("bar_value")).toString());
								} else {
									throw new RuntimeException("堆积图数据集bar_data属性中缺少bar_value字段");
								}
							}
							dataList[i] = dList;
						} else {
							throw new RuntimeException("堆积图数据集缺少bar_data属性");
						}
					}
					datasetColumn = DatasetUtils.createCategoryDataset(rowList.toArray(new String[rowList.size()]), columnList.toArray(new String[columnList.size()]), dataList);
				}

				SimpleSequence line = (SimpleSequence) arguments.get(1);
				if (line != null) {
					for (int i = 0; i < line.size(); i++) {
						SimpleHash sm = (SimpleHash) line.get(i);
						String series = null;
						SimpleNumber y = null;
						String x = null;
						if (sm.get("line_series") != null) {
							series = sm.get("line_series").toString();
						}
						if (sm.get("line_y") != null) {
							y = (SimpleNumber) sm.get("line_y");
						}
						if (sm.get("line_x") != null) {
							x = sm.get("line_x").toString();
						}
						if (series != null && y != null && x != null) {
							dataSetLine.addValue(y.getAsNumber(), series, x);
						}
					}
				}

				if (datasetColumn == null || datasetColumn.getRowCount() <= 0) {
					canReturn = false;
				}
			} else {
				canReturn = false;
			}
		} else {
			canReturn = false;
		}

		// has title
		if (arguments.size() >= 2) {
			title = arguments.get(2).toString();
		}

		// has xlabel
		if (arguments.size() >= 3) {
			xLabel = arguments.get(3).toString();
		}

		// has ylabel
		if (arguments.size() >= 4) {
			yLabel = arguments.get(4).toString();
		}

		// has width
		if (arguments.size() >= 5) {
			if (arguments.get(5) instanceof SimpleNumber) {
				SimpleNumber t = (SimpleNumber) arguments.get(5);
				width = t.getAsNumber().intValue();
			}
		}

		if (arguments.size() >= 6) {
			if (arguments.get(6) instanceof SimpleNumber) {
				SimpleNumber t = (SimpleNumber) arguments.get(6);
				height = t.getAsNumber().intValue();
			}
		}

		if (canReturn) {
			StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme();

			JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, datasetColumn, PlotOrientation.HORIZONTAL, true, false, false);
			standardChartTheme.apply(chart);
			chart.getLegend().setFrame(new BlockBorder(Color.white));

			// CategoryPlot p = chart.getCategoryPlot();
			// CategoryItemRenderer renderer = new DrawBarLineH.CustomRenderer();
			// p.setRenderer(renderer);
			// p.setOutlinePaint(Color.white);
			//
			// ValueAxis numberaxis = new NumberAxis("");
			// numberaxis.setUpperBound(100.00D); // 纵轴上限
			// numberaxis.setLowerBound(0.00D); // 纵轴下限
			// p.setRangeAxis(1, numberaxis);

			CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();

			// 设置图形的背景色
			categoryPlot.setBackgroundPaint(Color.WHITE);
			// 设置图形上竖线是否显示
			categoryPlot.setDomainGridlinesVisible(false);
			// 设置图形上竖线的颜色
			categoryPlot.setDomainGridlinePaint(Color.GRAY);
			// 设置图形上横线的颜色
			categoryPlot.setRangeGridlinePaint(Color.GRAY);

			// 设置柱状图的Y轴显示样式
			setNumberAxisToColumn(categoryPlot);

			CategoryAxis categoryaxis = categoryPlot.getDomainAxis();
			categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);// 横轴斜45度
			// 设置折线图的Y轴显示样式
			setNumberAxisLine(categoryPlot);

			categoryPlot.setDataset(1, dataSetLine);// 设置数据集索引
			categoryPlot.mapDatasetToRangeAxis(1, 1);// 将该索引映射到axis
			// 第一个参数指数据集的索引,第二个参数为坐标轴的索引
			LineAndShapeRenderer lineAndShapeRenderer = new LineAndShapeRenderer();
			// 数据点被填充即不是空心点
			lineAndShapeRenderer.setDefaultShapesFilled(true);
			// 数据点间连线可见
			lineAndShapeRenderer.setDefaultLinesVisible(true);
			// 设置折线拐点的形状，圆形
			lineAndShapeRenderer.setSeriesShape(0, new Ellipse2D.Double(-2D, -2D, 4D, 4D));

			// 设置某坐标轴索引上数据集的显示样式
			categoryPlot.setRenderer(1, lineAndShapeRenderer);
			// 设置两个图的前后顺序
			// ，DatasetRenderingOrder.FORWARD表示后面的图在前者上面，DatasetRenderingOrder.REVERSE表示
			// 表示后面的图在前者后面
			categoryPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

			try {
				byte[] bytes = ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
				return "<img class='img-responsive' src=\"data:image/png;base64," + Base64.encodeBase64String(bytes) + "\"/>";
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return "";
	}

	/**
	 * 设置折线图的Y轴显示样式
	 * 
	 * @param categoryplot
	 * @return
	 */
	private CategoryPlot setNumberAxisLine(CategoryPlot categoryplot) {
		ValueAxis numberaxis = new NumberAxis("");
		numberaxis.setUpperBound(100.00D); // 纵轴上限
		numberaxis.setLowerBound(0.00D); // 纵轴下限
		categoryplot.setRangeAxis(1, numberaxis);
		return categoryplot;
	}

	/**
	 * 设置柱状图的Y轴显示样式,NumberAxis为整数格式
	 * 
	 * @param categoryplot
	 * @return
	 */
	private CategoryPlot setNumberAxisToColumn(CategoryPlot categoryplot) {
		// 获取纵轴
		NumberAxis numberAxis = (NumberAxis) categoryplot.getRangeAxis();
		// 设置纵轴的刻度线
		numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// 数据轴的数据标签是否自动确定（默认为true）
		numberAxis.setAutoTickUnitSelection(true);
		// 数据轴的数据标签
		numberAxis.setStandardTickUnits(numberAxis.getStandardTickUnits());
		numberAxis.setLowerBound(0); // 数据轴上的显示最小值;
		numberAxis.setAutoRangeMinimumSize(1);// 1为一个间隔单位
		categoryplot.setRangeAxis(numberAxis);
		LayeredBarRenderer layeredBarRenderer = new LayeredBarRenderer();
		// 设置柱子的边框是否显示
		layeredBarRenderer.setDrawBarOutline(false);
		// 设置柱体宽度
		layeredBarRenderer.setMaximumBarWidth(0.08);
		// 设置柱体颜色
		layeredBarRenderer.setSeriesPaint(0, new Color(198, 219, 248));
		categoryplot.setRenderer(layeredBarRenderer);

		return categoryplot;
	}

	static class CustomRenderer extends StackedBarRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 3946096994457346387L;
		private Paint[] colors;

		public CustomRenderer() {
			this.setBarPainter(new StandardBarPainter());
			this.setDefaultShadowsVisible(false);
			this.setDefaultItemLabelsVisible(true);
			this.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
			this.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
			this.setItemLabelAnchorOffset(-10D);
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
