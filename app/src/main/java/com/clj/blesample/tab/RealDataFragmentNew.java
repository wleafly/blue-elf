package com.clj.blesample.tab;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.clj.blesample.R;
import com.clj.blesample.utils.DyLineChartUtils;
import com.github.mikephil.charting.charts.LineChart;

public class RealDataFragmentNew extends Fragment {
    private LineChart lineChartDemo; // 样例
    private DyLineChartUtils dyDemo; // 样例
    int[] colors = {Color.RED, Color.LTGRAY, Color.YELLOW, Color.GRAY, Color.GREEN, Color.DKGRAY, Color.CYAN, Color.BLUE, Color.BLACK};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.real_time_data_new, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CheckBox chat_show_or_fold = view.findViewById(R.id.chat_show_or_fold);
        LinearLayout chat = view.findViewById(R.id.hide_content);

        chat_show_or_fold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    chat.setVisibility(View.VISIBLE);
                    initLineChartDemo();
                    //定时任务
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            dyDemo.addEntry(Math.random()*100);
//
//                        }
//                    },  1000,5000);
                }else {
                    chat.setVisibility(View.GONE);
                }
            }
        });

        lineChartDemo = view.findViewById(R.id.dy_line_chart);
        dyDemo = new DyLineChartUtils(lineChartDemo, "", colors[7], getContext());
        dyDemo.setYAxis(100, 0, 8);


    }

    // 初始化折线图demo数据
        public void initLineChartDemo() {
    //        double[] demo = {48.3, 56.8, 42.5, 70.6, 14.5, 52.2, 43.9, 48.8};
            for (int i = 0; i < 8; i++) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dyDemo.addEntry(Math.random()*100);
                    }
                }, 180*i);
            }

        }
}
