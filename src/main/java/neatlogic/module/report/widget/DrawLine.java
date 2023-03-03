package neatlogic.module.report.widget;

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
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import com.alibaba.fastjson.JSONObject;

import neatlogic.module.report.util.JfreeChartUtil;
import neatlogic.module.report.util.JfreeChartUtil.ChartColor;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class DrawLine implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawLine.class);
	private String actionType ;
	//报表中所有数据源
	private Map<String, Object> reportMap;

    public DrawLine(Map<String, Object> reportMap, String actionType) {
        this.reportMap = reportMap;
        this.actionType = actionType;
    }
	@SuppressWarnings({"unchecked", "unused"})
    @Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
		int width = 1000;
		int height = 400;
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
			Boolean display = false;
			if (tbodyList.size() > 20) {
				display = true;
			}
			for (Map<String, Object> tbody : tbodyList) {
				Number y = (Number) tbody.get("yField");
				Object x = tbody.get("xField");
				Object series = tbody.get("groupField");
				if (series != null && y != null && x != null) {
					dataset.addValue(y, series.toString(), x.toString());
				}
			}
		}
		StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);

		JFreeChart chart = ChartFactory.createLineChart(title, xLabel, yLabel, dataset);
		standardChartTheme.apply(chart);
		chart.getLegend().setFrame(new BlockBorder(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType)));
		CategoryPlot p = chart.getCategoryPlot();
		CategoryItemRenderer render = new DrawLine.CustomRenderer(actionType);
		render.setDefaultItemLabelsVisible(isShowValue);
		p.setRenderer(render);
		p.setOutlinePaint(null);
		p.setNoDataMessage("无数据");
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

		return JfreeChartUtil.getChartString(actionType, chart, width, height);
	}

	static class CustomRenderer extends LineAndShapeRenderer {
		/**
		 * @Fields serialVersionUID : TODO
		 */
		private static final long serialVersionUID = 2892093102930999651L;
		private Paint[] colors;

		public CustomRenderer(String actionType) {
			this.colors = JfreeChartUtil.getCharColors(actionType);
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
