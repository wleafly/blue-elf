package com.clj.blesample.utils;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态折线图工具类
 */
public class DyLineChartUtils {

    private LineChart lineChart;
    private YAxis leftAxis;
    private XAxis xAxis;
    private YAxis rightAxis;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<String> timeList = new ArrayList<>(); //存储x轴的时间
    private Context context;

    public DyLineChartUtils(LineChart mLineChart, String name, int color, Context con) {
        this.context=con;
        this.lineChart = mLineChart;
        leftAxis = lineChart.getAxisLeft();
        rightAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();
        initLineChart();
        initLineDataSet(name, color);
    }
    /**
     * 初始化LineChart
     */
    private void initLineChart() {
        lineChart.setDoubleTapToZoomEnabled(false);
        // 不显示数据描述
        lineChart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        lineChart.setNoDataText("暂无数据");
        // 禁止x轴y轴同时进行缩放
        lineChart.setPinchZoom(false);
        // 启用/禁用缩放图表上的两个轴。
        lineChart.setScaleEnabled(false);
        // 设置为false以禁止通过在其上双击缩放图表。
        lineChart.getAxisRight().setEnabled(true);// 右侧Y轴
        lineChart.setDrawGridBackground(false);
        // 显示边界
        lineChart.setDrawBorders(true);
        // 折线图例 标签 设置 这里不显示图例
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        leftAxis.setValueFormatter(new LargeValueFormatter());
        rightAxis.setValueFormatter(new LargeValueFormatter());

//        leftAxis.setDrawGridLines(true);
//        leftAxis.setDrawAxisLine(true);
//        leftAxis.setDrawLabels(true);

        // X轴设置显示位置在底部
        xAxis.setEnabled(true);// 关闭x轴
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(8);
        xAxis.setDrawGridLines(true); // 不绘制网格线
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return "";
//            }
//        });
        // 保证Y轴从0开始，不然会上移一点
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
    }

    /**
     * 初始化折线(一条线)
     *
     * @param name
     * @param color
     */
    private void initLineDataSet(String name, int color) {
        lineDataSet = new LineDataSet(null, name);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setHighLightColor(color);
        //设置曲线填充
        lineDataSet.setDrawFilled(true);//填充底部颜色
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);// CUBIC_BEZIER
//        lineDataSet.setValueFormatter(new DefaultValueFormatter(4));
        lineDataSet.setValueFormatter(new MyValueFormatter());
        // 添加一个空的 LineData
        lineData = new LineData();
//        lineData.setValueFormatter(new DefaultValueFormatter(1));

        lineChart.setData(lineData);
//        lineDataSet.setDrawValues(true);

        LineChartMarkView mv = new LineChartMarkView(context, xAxis.getValueFormatter());
        mv.setChartView(lineChart);
        lineChart.setMarker(mv);
        lineChart.invalidate();
    }

    /**
     * 动态添加数据（一条折线图）
     *
     * @param number
     */
    public void addEntry(Double number) {
        number = (double)Math.round(number*100)/100;
        //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (lineDataSet.getEntryCount() == 0) {
            lineData.addDataSet(lineDataSet);
        }
        lineChart.setData(lineData);
        // 避免集合数据过多，及时清空（做这样的处理，并不知道有没有用，但还是这样做了）
        if (timeList.size() > 11) {
            timeList.clear();
        }
        lineDataSet.setDrawValues(true);// 折线上的值

        Entry entry = new Entry(lineDataSet.getEntryCount(), number.floatValue());
        lineData.addEntry(entry, 0);
        //通知数据已经改变
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        lineChart.setVisibleXRangeMaximum(8);
        //移到某个位置
        lineChart.moveViewToX(lineData.getEntryCount() - 5);
    }


    /**
     * 设置Y轴值
     */
    public void setYAxis(float max, float min, int labelCount) {
        if (max < min) {
            return;
        }
        leftAxis.setAxisMaximum(max);
        leftAxis.setAxisMinimum(min);
        leftAxis.setLabelCount(labelCount, false);
        rightAxis.setAxisMaximum(max);
        rightAxis.setAxisMinimum(min);
        rightAxis.setLabelCount(labelCount, false);
        lineChart.invalidate();
    }

    /**
     *  清除数据列表
     */
    public void destroyChart() {
        lineData.clearValues();
        lineDataSet.clear();
        timeList.clear();
//        lineChart.invalidate();
//        lineChart.clear();
    }

    public int getLineDataNum() {
        return lineDataSet.getEntryCount();
    }

}
