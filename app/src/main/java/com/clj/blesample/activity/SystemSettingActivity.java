package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;

public class SystemSettingActivity extends AppCompatActivity {

    private Toolbar toolbar;

    // 实时数据下载
    private TextView realDataDownloadName;
    private CheckBox checkBoxSwitch;

    // 多参数配置
    private View mutilConfiguration;
    private Spinner parameter1;
    private Spinner parameter2;
    private Spinner parameter3;
    private Spinner parameter4;
    private Spinner parameter5;
    private Spinner parameter6;
    private Spinner parameter7;
    private Button mutilSetSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_set);

        // 实时数据下载
        realDataDownloadName = findViewById(R.id.download_name);
        checkBoxSwitch = findViewById(R.id.download_switch);

        // 多参数配置
        mutilConfiguration = findViewById(R.id.mutil_configuration);
        parameter1 = findViewById(R.id.parameter_1);
        parameter2 = findViewById(R.id.parameter_2);
        parameter3 = findViewById(R.id.parameter_3);
        parameter4 = findViewById(R.id.parameter_4);
        parameter5 = findViewById(R.id.parameter_5);
        parameter6 = findViewById(R.id.parameter_6);
        parameter7 = findViewById(R.id.parameter_7);
        mutilSetSave = findViewById(R.id.mutil_set_save);

        // 初始化导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.system_set));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 设置导航（返回图片）的点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 退出当前页面
                finish();
            }
        });

        // 初始化多参数
        initMutilInfo();

        // 获取 SharedPreferences
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        // 编辑器
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getString("realDataDownload", "").equals("true")) {
            // 说明此时是开启的
            realDataDownloadName.setTextColor(getResources().getColor(R.color.purple_500));
            checkBoxSwitch.setChecked(true);
        }
//        else {
            // 说明是关闭的或者没有数据
//        }

        // 实时数据下载功能点击
        checkBoxSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBoxSwitch.isChecked()) {
                    realDataDownloadName.setTextColor(getResources().getColor(R.color.purple_500));
                    editor.putString("realDataDownload", "true");
                } else {
                    realDataDownloadName.setTextColor(getResources().getColor(R.color.gray1));
                    editor.putString("realDataDownload", "false");
                }
                editor.commit();
            }
        });

        // 传感器配置
        mutilSetSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMutilSet();
                new AlertDialog.Builder(mutilConfiguration.getContext()).setMessage("保存成功！").setPositiveButton(getString(R.string.ensure),null).show();

            }
        });
    }

    public void initMutilInfo() {
        // 获取 SharedPreferences
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        // 编辑器
        SharedPreferences.Editor editor = pref.edit();

        if(pref.getString("mutilIsSet", "").equals("true")) {
            // 说明已经配置过了
            // 获取配置的信息: 这里存储的顺序
            int p1 = Integer.parseInt(pref.getString("mutilParameter1", ""));
            int p2 = Integer.parseInt(pref.getString("mutilParameter2", ""));
            int p3 = Integer.parseInt(pref.getString("mutilParameter3", ""));
            int p4 = Integer.parseInt(pref.getString("mutilParameter4", ""));
            int p5 = Integer.parseInt(pref.getString("mutilParameter5", ""));
            int p6 = Integer.parseInt(pref.getString("mutilParameter6", ""));
            int p7 = Integer.parseInt(pref.getString("mutilParameter7", ""));

            // 从 0 开始
            parameter1.setSelection(p1);
            parameter2.setSelection(p2);
            parameter3.setSelection(p3);
            parameter4.setSelection(p4);
            parameter5.setSelection(p5);
            parameter6.setSelection(p6);
            parameter7.setSelection(p7);
        } else {
            // editor.putString("mutilIsSet", "false");
            parameter1.setSelection(0);
            parameter2.setSelection(0);
            parameter3.setSelection(1);
            parameter4.setSelection(2);
            parameter5.setSelection(3);
            parameter6.setSelection(4);
            parameter7.setSelection(5);
        }
    }

    public void saveMutilSet() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("mutilParameter1", String.valueOf(parameter1.getSelectedItemId()));
        editor.putString("mutilParameter2", String.valueOf(parameter2.getSelectedItemId()));
        editor.putString("mutilParameter3", String.valueOf(parameter3.getSelectedItemId()));
        editor.putString("mutilParameter4", String.valueOf(parameter4.getSelectedItemId()));
        editor.putString("mutilParameter5", String.valueOf(parameter5.getSelectedItemId()));
        editor.putString("mutilParameter6", String.valueOf(parameter6.getSelectedItemId()));
        editor.putString("mutilParameter7", String.valueOf(parameter7.getSelectedItemId()));
        editor.putString("mutilIsSet", "true");
        editor.commit();

        int[] unit = {2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 0};
        int[] type = {9,7,2,3,4,5,6,7};
        if(pref.getString("mutilParameter1", "").equals("1")) {
            type[0] = 0;
            type[1] = 0;
        } else {
            type[0] = 9;
            type[1] = 7;
        }
        type[2] = unit[Integer.parseInt(pref.getString("mutilParameter2", ""))];
        type[3] = unit[Integer.parseInt(pref.getString("mutilParameter3", ""))];
        type[4] = unit[Integer.parseInt(pref.getString("mutilParameter4", ""))];
        type[5] = unit[Integer.parseInt(pref.getString("mutilParameter5", ""))];
        type[6] = unit[Integer.parseInt(pref.getString("mutilParameter6", ""))];
        type[7] = unit[Integer.parseInt(pref.getString("mutilParameter7", ""))];
        ((MyApplication)getApplication()).setMutilSensor(type);

        System.out.println("值为：" + pref.getString("mutilIsSet", ""));
    }

}
