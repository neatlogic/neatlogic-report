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

import neatlogic.module.report.config.ReportConfig;
import neatlogic.module.report.util.JfreeChartUtil;
import neatlogic.module.report.util.JfreeChartUtil.ChartColor;
import freemarker.template.*;
import org.jfree.chart.ChartFactory;
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
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Deprecated
public class DrawStackedBarLineH implements TemplateMethodModelEx {
    //private static final Log logger = LogFactory.getLog(DrawStackedBarLineH.class);
    private final String actionType;
    //报表中所有数据源
    private Map<String, Object> reportMap;

    public DrawStackedBarLineH(Map<String, Object> reportMap, String actionType) {
        this.actionType = actionType;
        this.reportMap = reportMap;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        int width = 1000;
        int height = 600;
        String title = "", xLabel = "", yLabel = "", data = "";
        CategoryDataset dataset = null;
        DefaultCategoryDataset dataSetLine = new DefaultCategoryDataset();
        if (arguments.size() >= 1) {
            if (arguments.get(0) instanceof SimpleSequence) {
                SimpleSequence ss = (SimpleSequence) arguments.get(0);
                if (ss.size() > 0) {
                    List<String> rowList = new ArrayList<>();
                    List<String> columnList = new ArrayList<>();
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
                                    dList[j] =
                                            Double.parseDouble(valueItem.get("bar_value").toString());
                                } else {
                                    throw new RuntimeException("堆积图数据集bar_data属性中缺少bar_value字段");
                                }
                            }
                            dataList[i] = dList;
                        } else {
                            throw new RuntimeException("堆积图数据集缺少bar_data属性");
                        }
                    }
                    dataset = DatasetUtils.createCategoryDataset(rowList.toArray(new String[0]),
                            columnList.toArray(new String[0]), dataList);
                }
            }
            if (arguments.get(0) instanceof SimpleSequence) {
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
            }

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

        StandardChartTheme standardChartTheme = JfreeChartUtil.getStandardChartTheme(actionType);

        JFreeChart chart = ChartFactory.createStackedBarChart(title, xLabel, yLabel, dataset,
                PlotOrientation.HORIZONTAL, true, false, false);
        standardChartTheme.apply(chart);
        chart.getLegend().setFrame(new BlockBorder(ChartColor.CHART_BACKGROUND_COLOR.getColor(actionType)));

        CategoryPlot p = chart.getCategoryPlot();
        CategoryItemRenderer renderer = new DrawStackedBarLineH.CustomRenderer(actionType);
        p.setRenderer(renderer);
        p.setOutlinePaint(Color.white);
        p.setNoDataMessage("无数据");

        CategoryAxis categoryaxis = p.getDomainAxis();
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);// 横轴斜45度

        p.setDataset(1, dataSetLine);// 设置数据集索引
        p.mapDatasetToRangeAxis(1, 1);// 将该索引映射到axis

        CategoryItemRenderer Linerenderer = new DrawStackedBarLineH.CustomRenderer(actionType); // 设置方形数据点
        p.setRenderer(Linerenderer);
        p.setOutlinePaint(Color.white);

        double maxNum = 0d;
        for (int j = 0; j < dataSetLine.getRowKeys().size(); j++) { // 获取最大值
            for (int i = 0; i < dataSetLine.getColumnKeys().size(); i++) {
                Object temp = dataSetLine.getValue(j, i);
                if (temp != null) {
                    double tempInt = Double.parseDouble((temp.toString()));
                    if (tempInt > maxNum) {
                        maxNum = tempInt;
                    }
                }
            }
        }
        ValueAxis numberaxis = new NumberAxis("");
        numberaxis.setUpperBound(maxNum + 1); // 纵轴上限
        numberaxis.setLowerBound(0.00D); // 纵轴下限
        p.setRangeAxis(1, numberaxis);
        LineAndShapeRenderer lineAndShapeRenderer = new DrawStackedBarLineH.LineCustomRenderer();
        lineAndShapeRenderer.setSeriesShape(0, new Ellipse2D.Double(-2D, -2D, 4D, 4D));
        p.setRenderer(1, lineAndShapeRenderer);
        p.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        return JfreeChartUtil.getChartString(actionType, chart, width, height);
    }

    static class CustomRenderer extends StackedBarRenderer {
        private static final long serialVersionUID = 8646461953824971641L;
        private final Paint[] colors;

        public CustomRenderer(String actionType) {
            this.setBarPainter(new StandardBarPainter());
            setDefaultShadowsVisible(false);
            this.setDefaultItemLabelsVisible(true);
            this.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            this.setDefaultPositiveItemLabelPosition(
                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
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

    static class LineCustomRenderer extends LineAndShapeRenderer {
        private static final long serialVersionUID = 2892093102930999651L;
        private final Paint[] colors;

        public LineCustomRenderer() {
            this.colors = ReportConfig.CHART_COLOR;
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
