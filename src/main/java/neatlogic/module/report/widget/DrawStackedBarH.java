/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.report.widget;

import com.alibaba.fastjson.JSONObject;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import neatlogic.framework.report.exception.ReportDrawBarFieldLessAttrException;
import neatlogic.framework.report.exception.ReportDrawBarLessAttrException;
import neatlogic.framework.report.exception.ReportDrawBarLessFieldException;
import neatlogic.module.report.util.JfreeChartUtil;
import neatlogic.module.report.util.JfreeChartUtil.ChartColor;
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
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DrawStackedBarH implements TemplateMethodModelEx {
    private static final Log logger = LogFactory.getLog(DrawStackedBarH.class);
    private final String actionType;
    //报表中所有数据源
    private Map<String, Object> reportMap;

    public DrawStackedBarH(Map<String, Object> reportMap, String actionType) {
        this.reportMap = reportMap;
        this.actionType = actionType;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        int width = 1000;
        int height = 600;
        String title = "", xLabel = "", yLabel = "", data = "";
        int tick = 0;
        boolean isShowValue = true;
        CategoryDataset dataset = null;

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
            List<String> rowList = new ArrayList<>();
            List<String> columnList = new ArrayList<>();
            double[][] dataList = new double[tbodyList.size()][];
            int i = 0;
            for (Map<String, Object> tbody : tbodyList) {
                Object groupField = tbody.get("groupField");
                if (groupField != null) {
                    rowList.add(groupField.toString());
                } else {
                    throw new ReportDrawBarLessFieldException("groupField");
                }
                List<Map<String, Object>> valueItemList = (List<Map<String, Object>>) tbody.get("dataList");
                if (CollectionUtils.isNotEmpty(valueItemList)) {
                    double[] dList = new double[valueItemList.size()];
                    int j = 0;
                    for (Map<String, Object> valueItem : valueItemList) {
                        Object typeField = tbody.get("typeField");
                        if (typeField != null) {
                            if (!columnList.contains(typeField)) {
                                columnList.add(typeField.toString());
                            }
                        } else {
                            throw new ReportDrawBarFieldLessAttrException("dataList", "typeField");
                        }
                        Object valueField = valueItem.get("valueField");
                        if (valueField != null) {
                            dList[j] = Double.parseDouble(valueField.toString());
                        } else {
                            throw new ReportDrawBarFieldLessAttrException("dataList", "valueField");
                        }
                        j++;
                    }
                    dataList[i] = dList;
                } else {
                    throw new ReportDrawBarLessAttrException("dataList");
                }
                i++;
            }
            dataset = DatasetUtils.createCategoryDataset(rowList.toArray(new String[0]), columnList.toArray(new String[0]), dataList);
        }

        StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);

        JFreeChart chart = ChartFactory.createStackedBarChart(title, xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, true, false, false);
        standardChartTheme.apply(chart);
        chart.getLegend().setFrame(new BlockBorder(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType)));

        CategoryPlot p = chart.getCategoryPlot();
        CategoryItemRenderer renderer = new DrawStackedBarH.CustomRenderer(actionType);
        renderer.setDefaultItemLabelsVisible(isShowValue);
        p.setRenderer(renderer);
        p.setOutlinePaint(Color.white);
        p.setNoDataMessage("无数据");
        CategoryAxis axis = (CategoryAxis) p.getDomainAxis();// Y坐标轴
        axis.setLowerMargin(0);
        axis.setUpperMargin(0);
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);// 倾斜45度
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
        private static final long serialVersionUID = 8646461953824971641L;
        private final Paint[] colors;

        public CustomRenderer(String actionType) {
            this.setBarPainter(new StandardBarPainter());
            this.setShadowVisible(false);
            setDefaultShadowsVisible(false);
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
