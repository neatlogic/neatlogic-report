/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.report.widget;

import neatlogic.module.report.util.JfreeChartUtil;
import neatlogic.module.report.util.JfreeChartUtil.ChartColor;
import com.alibaba.fastjson.JSONObject;
import freemarker.template.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

public class DrawPie implements TemplateMethodModelEx {
	private static final Log logger = LogFactory.getLog(DrawPie.class);
	private final String actionType;
	//报表中所有数据源
	private Map<String, Object> reportMap;

	public DrawPie(Map<String, Object> reportMap, String actionType) {
		this.reportMap = reportMap;
		this.actionType = actionType;
	}

	@Override
	public Object exec(List arguments) throws TemplateModelException {
		boolean canReturn = true;
		int width = 500;
		int height = 500;
		DefaultPieDataset dataset = new DefaultPieDataset();
		String title = "", data = "";

		if (arguments.size() >= 1) {
			String config = arguments.get(0).toString();
			try {
				JSONObject configObj = JSONObject.parseObject(config);
				data = configObj.getString("data");
				title = configObj.getString("title");
				if (configObj.getIntValue("width") > 0) {
					width = configObj.getIntValue("width");
				}
				if (configObj.getIntValue("height") > 0) {
					height = configObj.getIntValue("height");
				}
			} catch (Exception ex) {
				// 非json格式
			    logger.error(ex.getMessage(), ex);
			}
		}

		List<Map<String, Object>> tbodyList = (List<Map<String, Object>>) reportMap.get(data);
		if (CollectionUtils.isNotEmpty(tbodyList)) {
			for (Map<String, Object> tbody : tbodyList) {
				Object typeField = tbody.get("typeField");
				Number valueField = (Number) tbody.get("valueField");
				if (typeField != null && valueField != null) {
					dataset.setValue(typeField.toString(), valueField);
				}
			}
		} else {
			canReturn = false;
		}
		if (canReturn) {
		   
		    
			StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);
			JFreeChart chart = ChartFactory.createPieChart(title, dataset);
			standardChartTheme.apply(chart);
			chart.getLegend().setFrame(new BlockBorder(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType)));
			PiePlot p = (PiePlot) chart.getPlot();
			p.setBackgroundAlpha(0.0f);
			CustomRenderer renderer = new CustomRenderer();
			renderer.setColor(p, dataset,actionType);
			return JfreeChartUtil.getChartString(actionType, chart, width, height);
            
		}
		return "";
	}

	static class CustomRenderer {
		// private Paint[] colors;

		public CustomRenderer() {
			// this.colors = JfreeChartUtil.CHART_COLORS;
		}

		public void setColor(PiePlot plot, DefaultPieDataset dataset,String actionType) {

			plot.setNoDataMessage("无数据");
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
			piePlot.setLabelPaint(ChartColor.LABEL_LINK_COLOR.getColor(actionType));
			piePlot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator("{0}：{1}({2})"));// 设置legend显示格式
			piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}({2})", NumberFormat.getNumberInstance(), new DecimalFormat("0.00%")));// 设置标签带%
			PieDataset pieDataset = piePlot.getDataset();
		    if (pieDataset != null)
		    {
		        for (int i = 0; i < pieDataset.getItemCount(); i++)
		        {
		            piePlot.setSectionOutlinesVisible(false);//去掉扇区border
		        }
		    }
		}
	}

}