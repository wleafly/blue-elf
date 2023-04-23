package com.clj.blesample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.R;
import com.clj.blesample.entity.Chart;
import com.clj.blesample.utils.DyLineChartUtils;
import com.github.mikephil.charting.charts.LineChart;

import java.util.List;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ViewHolder> {
    private List<Chart> chartList;
    private Context context;
    private Boolean isNeedCreateChat=true;

    public void setNeedCreateChat(Boolean needCreateChat) {
        isNeedCreateChat = needCreateChat;
    }

    public ChartAdapter(List<Chart> chartList, Context context) {
        this.chartList = chartList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView param;
        TextView value;
        CheckBox chat_show_or_fold;
        LinearLayout hide_content;
        LineChart chart;
        TextView address;
        TextView temperature;
        View itemView;
        public ViewHolder(View view) {
            super(view);
            itemView = view;
            param = view.findViewById(R.id.main_param);
            value = view.findViewById(R.id.value);
            chat_show_or_fold = view.findViewById(R.id.chat_show_or_fold);
            hide_content = view.findViewById(R.id.hide_content);
            chart = view.findViewById(R.id.chart);
            address = view.findViewById(R.id.address);
            temperature = view.findViewById(R.id.temperature);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        holder.chat_show_or_fold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = holder.getAdapterPosition();
                if (b){
                    holder.hide_content.setVisibility(View.VISIBLE);
                }else {
                    holder.hide_content.setVisibility(View.GONE);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chart chart = chartList.get(position);
        if (chart.getParam().equals("ORP")){
            holder.temperature.setVisibility(View.GONE);
        }
        if (chart.getParam().equals("COD")){
            holder.itemView.findViewById(R.id.cod_linear).setVisibility(View.VISIBLE);
            if (chart.getMud()!=null) ((TextView)holder.itemView.findViewById(R.id.mud)).setText("浊度:"+chart.getMud()+"NTV");
            if (chart.getBod()!=null) ((TextView)holder.itemView.findViewById(R.id.bod)).setText("BOD:"+chart.getBod());
        }
        holder.value.setText(chart.getValue());
        holder.param.setText(chart.getParam());
        holder.address.setText("地址："+chart.getAddress());
        holder.temperature.setText("温度："+chart.getTemperature()+"℃");
        if (isNeedCreateChat){
            DyLineChartUtils chartUtil = new DyLineChartUtils(holder.chart, "", Color.BLUE, context);
            chartUtil.setYAxis(chart.getMax(), 0, 8);
            chart.setChartUtil(chartUtil);
        }

    }

    @Override
    public int getItemCount() {
        return chartList.size();
    }

    public void updateChart(int position,double value,String temperature){
        Chart chart = chartList.get(position);
        DyLineChartUtils chartUtil = chart.getChartUtil();
        chartUtil.addEntry(value);
        chart.setValue(value+chart.getUnit());
        chart.setTemperature(temperature);
        notifyDataSetChanged();
    }

    public void updateCodChart(int position,double value,String temperature,String mud,String bod){
        System.out.println(position);
        System.out.println(chartList);
        Chart chart = chartList.get(position);
        DyLineChartUtils chartUtil = chart.getChartUtil();
        chartUtil.addEntry(value);
        chart.setValue(value+chart.getUnit());
        chart.setTemperature(temperature);
        chart.setMud(mud);
        chart.setBod(bod);
        notifyDataSetChanged();
    }


}
