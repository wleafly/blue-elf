package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.clj.blesample.application.MyApplication;
import com.clj.blesample.domain.SensorData;
import com.clj.blesample.tab.DynamicLineChartManager;
import com.clj.blesample.tab.RealDataFragment;
import com.clj.blesample.utils.DyLineChartUtils;
import com.clj.blesample.utils.OpenFileUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.MyLog;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.clj.blesample.R;

public class HistoryDataActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private BleDevice bleDevice;
    private BleManager bleManager;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;
    private int chipType = 0; // 芯片类型

    private Boolean globalDeviceIsConnect; // 设备是否连接
    private String globalDeviceName; // 全局的设备名称
    private int globalDeviceAddress; // 全局的设备地址
    private int globalDeviceType; // 全局的设备类型

    private ProgressDialog progressDialogReal;
    private Runnable runnable;
    private Handler handler;
    private Boolean isF6Success = false;

    private DynamicLineChartManager dynamicLineChartManager1;
    private DynamicLineChartManager dynamicLineChartManager2;
    private List<Float> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合
    private List<SensorData> sensorData = new ArrayList<>(); // 传感器数据


    private int dataNumber = 0;
    private int dataValidNumber = 0;

    // 有效数据数组，分别对应：未连接，电导率1，电导率2，PH，ORP，溶解氧，氨氮，浊度，盐度，化学需氧量COD，余氯，叶绿素，蓝绿藻，透明度，悬浮物，水中油，多参数
    private int[] validData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // 单参数数据上下限：电导率1，温度，电导率2，温度，  PH，温度，ORP，溶解氧，温度，  氨氮，温度，浊度，温度，盐度，温度，
    // 化学需氧量COD，温度，浊度(COD),BOD,余氯，温度，  叶绿素，温度，蓝绿藻，温度，透明度，温度，  悬浮物，温度，水中油,温度
    private int[] singleChartMax = {100,50,100,50,  14,50,100,20,50,   100,50,100,50,70,50,
                                    100,50,100,500,10,50,   400,50,300,50,100,50,   100,50,40,50};
    private int[] singleChartMin = {0,0,0,0,  0,0,0,0,0,  0,0,0,0,0,0,  0,0,0,0,0,0,  0,0,0,0,0,0,  0,0,0,0};
    private int[] singleChartLabelCount = {8,8,8,8,   8,8,8,8,8,   8,8,8,8,8,8,   8,8,8,8,8,8,  8,8,8,8,8,8,  8,8,8,8};

    // 多参数数据上下限：温度，COD，浊度(COD),电导率/盐度,PH,ORP,溶解氧,NHN,浊度
    private int[] mutilChartMax = {50,100,100,100,14,100,20,100,100};
    private int[] mutilChartMin = {0,0,0,0,0,0,0,0,0};
    private int[] mutilChartLabelCount = {8,8,8,8,8,8,8,8,8};

    private LineChart lineChart;
    private LineChart lineChart0; // 温度折线图
    private DyLineChartUtils dy;
    private DyLineChartUtils dy0;
    private DyLineChartUtils dyCOD;
    private DyLineChartUtils dyCODNT;
    private DyLineChartUtils dyEC;
    private DyLineChartUtils dyPH;
    private DyLineChartUtils dyORP;
    private DyLineChartUtils dyDO;
    private DyLineChartUtils dyNH;
    private DyLineChartUtils dyNT;

    // 多参数折线图
    private LineChart lineChartCOD; // COD折线图
    private LineChart lineChartCODNT; // NT(COD)折线图
    private LineChart lineChartEC; // EC折线图
    private LineChart lineChartPH; // PH折线图
    private LineChart lineChartORP; // ORP折线图
    private LineChart lineChartDO; // DO折线图
    private LineChart lineChartNH; // NH折线图
    private LineChart lineChartNT; // NT折线图

    private LineChart[] lineCharts;

    private int[] validNumber = {0, 0, 0, 0, 0, 0, 0, 0, 0};

    private View historyLayout;
    private Button btn; // 清除历史数据按钮
    private TextView dataTotal;
    private TextView dataValid;
    private Button downloadBtn;
    private List<String> historyData = new ArrayList<String>();

    private String name1;
    private String name2;
    private int type; // 单参数、多参数

    // 属性值
    private View singleLayout; // 单参数
    private View singleLayout1; // 单参数第一行
    private TextView singleEC1;
    private TextView singleEC1Temp;
    private TextView singleEC2;
    private TextView singleEC2Temp;
    private TextView singlePH;
    private TextView singlePHTemp;

    private View singleLayout2; // 单参数第二行
    private TextView singleORP;
    private TextView singleRDO;
    private TextView singleRDOTemp;
    private TextView singleNHN;
    private TextView singleNHNTemp;
    private TextView singleZS;
    private TextView singleZSTemp;

    private View singleLayout3; // 单参数第三行
    private TextView singleCOD;
    private TextView singleCODTemp;
    private TextView singleCODZS;
    private TextView singleCODBOD;
    private TextView singleCH;
    private TextView singleCHTemp;

    private View singleLayout4; // 单参数第四行
    private TextView singleRC;
    private TextView singleRCTemp;
    private TextView singleCY;
    private TextView singleCYTemp;
    private TextView singleTSS;
    private TextView singleTSSTemp;

    private View singleLayout5; // 单参数第五行
    private TextView singleTSM;
    private TextView singleTSMTemp;
    private TextView singleOW;
    private TextView singleOWTemp;
    private TextView singleSAL;
    private TextView singleSALTemp;

    private View mutilLayout; // 多参数
    private View mutilLayout1; // 多参数第一行
    private TextView mutilTemp;
    private TextView mutilCOD;
    private TextView mutilCODZS;
    private TextView mutilECSAL;

    private View mutilLayout2; // 多参数第二行
    private TextView mutilPH;
    private TextView mutilORP;
    private TextView mutilRDO;
    private TextView mutilNHN;
    private TextView mutilZS;

    private TextView dataValidEC1;
    private TextView dataValidEC2;
    private TextView dataValidPH;
    private TextView dataValidORP;
    private TextView dataValidRDO;
    private TextView dataValidNHN;
    private TextView dataValidZS;
    private TextView dataValidSAL;
    private TextView dataValidCOD;
    private TextView dataValidRC;
    private TextView dataValidCH;
    private TextView dataValidCY;
    private TextView dataValidTSS;
    private TextView dataValidTSM;
    private TextView dataValidOW;
    private TextView dataValidMutil;

    // 重新规划的单参数折线图
    private LineChart lineChartSingleEC1;
    private LineChart lineChartSingleEC1Temp;
    private LineChart lineChartSingleEC2;
    private LineChart lineChartSingleEC2Temp;
    private LineChart lineChartSinglePH;
    private LineChart lineChartSinglePHTemp;
    private LineChart lineChartSingleORP;
    private LineChart lineChartSingleRDO;
    private LineChart lineChartSingleRDOTemp;
    private LineChart lineChartSingleNHN;
    private LineChart lineChartSingleNHNTemp;
    private LineChart lineChartSingleZS;
    private LineChart lineChartSingleZSTemp;
    private LineChart lineChartSingleSAL;
    private LineChart lineChartSingleSALTemp;
    private LineChart lineChartSingleCOD;
    private LineChart lineChartSingleCODTemp;
    private LineChart lineChartSingleCODZS;
    private LineChart lineChartSingleCODBOD;
    private LineChart lineChartSingleRC;
    private LineChart lineChartSingleRCTemp;
    private LineChart lineChartSingleCH;
    private LineChart lineChartSingleCHTemp;
    private LineChart lineChartSingleCY;
    private LineChart lineChartSingleCYTemp;
    private LineChart lineChartSingleTSS;
    private LineChart lineChartSingleTSSTemp;
    private LineChart lineChartSingleTSM;
    private LineChart lineChartSingleTSMTemp;
    private LineChart lineChartSingleOW;
    private LineChart lineChartSingleOWTemp;

    private DyLineChartUtils dySingleEC1;
    private DyLineChartUtils dySingleEC1Temp;
    private DyLineChartUtils dySingleEC2;
    private DyLineChartUtils dySingleEC2Temp;
    private DyLineChartUtils dySinglePH;
    private DyLineChartUtils dySinglePHTemp;
    private DyLineChartUtils dySingleORP;
    private DyLineChartUtils dySingleRDO;
    private DyLineChartUtils dySingleRDOTemp;
    private DyLineChartUtils dySingleNHN;
    private DyLineChartUtils dySingleNHNTemp;
    private DyLineChartUtils dySingleZS;
    private DyLineChartUtils dySingleZSTemp;
    private DyLineChartUtils dySingleSAL;
    private DyLineChartUtils dySingleSALTemp;
    private DyLineChartUtils dySingleCOD;
    private DyLineChartUtils dySingleCODTemp;
    private DyLineChartUtils dySingleCODZS;
    private DyLineChartUtils dySingleCODBOD;
    private DyLineChartUtils dySingleRC;
    private DyLineChartUtils dySingleRCTemp;
    private DyLineChartUtils dySingleCH;
    private DyLineChartUtils dySingleCHTemp;
    private DyLineChartUtils dySingleCY;
    private DyLineChartUtils dySingleCYTemp;
    private DyLineChartUtils dySingleTSS;
    private DyLineChartUtils dySingleTSSTemp;
    private DyLineChartUtils dySingleTSM;
    private DyLineChartUtils dySingleTSMTemp;
    private DyLineChartUtils dySingleOW;
    private DyLineChartUtils dySingleOWTemp;

    private LineChart[] lcSingles = {lineChartSingleEC1, lineChartSingleEC1Temp, lineChartSingleEC2, lineChartSingleEC2Temp, lineChartSinglePH,
            lineChartSinglePHTemp, lineChartSingleORP, lineChartSingleRDO, lineChartSingleRDOTemp, lineChartSingleNHN,
            lineChartSingleNHNTemp, lineChartSingleZS, lineChartSingleZSTemp, lineChartSingleSAL, lineChartSingleSALTemp,
            lineChartSingleCOD, lineChartSingleCODTemp, lineChartSingleCODZS, lineChartSingleCODBOD, lineChartSingleRC,
            lineChartSingleRCTemp, lineChartSingleCH, lineChartSingleCHTemp, lineChartSingleCY, lineChartSingleCYTemp,
            lineChartSingleTSS, lineChartSingleTSSTemp, lineChartSingleTSM, lineChartSingleTSMTemp, lineChartSingleOW,
            lineChartSingleOWTemp};

    private DyLineChartUtils[] dySingles = {dySingleEC1, dySingleEC1Temp, dySingleEC2, dySingleEC2Temp, dySinglePH, dySinglePHTemp,
            dySingleORP, dySingleRDO, dySingleRDOTemp, dySingleNHN, dySingleNHNTemp, dySingleZS, dySingleZSTemp,
            dySingleSAL, dySingleSALTemp, dySingleCOD, dySingleCODTemp, dySingleCODZS, dySingleCODBOD, dySingleRC,
            dySingleRCTemp, dySingleCH, dySingleCHTemp, dySingleCY, dySingleCYTemp, dySingleTSS, dySingleTSSTemp,
            dySingleTSM, dySingleTSMTemp, dySingleOW, dySingleOWTemp};



    private LineChart[] lcMutils = {lineChart0, lineChartCOD, lineChartCODNT, lineChartEC, lineChartPH, lineChartORP, lineChartDO, lineChartNH, lineChartNT};


    private DyLineChartUtils[] dys = {dy0, dyCOD, dyCODNT, dyEC, dyPH, dyORP, dyDO, dyNH, dyNT};



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_history_data);

        name1 = getString(R.string.data_init);
        name2 = getString(R.string.data_temperature);

//        historyData.clear(); // 清空历史数据数组

        dataTotal = findViewById(R.id.data_total);
        dataValid = findViewById(R.id.data_valid);
        lineChart = findViewById(R.id.dy_line_chart);

        lineChart0 = findViewById(R.id.dy_line_chart0);
        lineChartCOD = findViewById(R.id.dy_line_chart_cod);
        lineChartCODNT = findViewById(R.id.dy_line_chart_cod_nt);
        lineChartEC = findViewById(R.id.dy_line_chart_ec);
        lineChartPH = findViewById(R.id.dy_line_chart_ph);
        lineChartORP = findViewById(R.id.dy_line_chart_orp);
        lineChartDO = findViewById(R.id.dy_line_chart_do);
        lineChartNH = findViewById(R.id.dy_line_chart_nh);
        lineChartNT = findViewById(R.id.dy_line_chart_nt);


        lineCharts = new LineChart[]{lineChart0, lineChartCOD, lineChartCODNT, lineChartEC, lineChartPH, lineChartORP, lineChartDO, lineChartNH, lineChartNT};


        singleLayout = findViewById(R.id.single_layout); // 单参数
        singleLayout1 = findViewById(R.id.single_layout_1); // 单参数第一行
        singleEC1 = findViewById(R.id.single_EC1);
        singleEC1Temp = findViewById(R.id.single_EC1_temp);
        singleEC2 = findViewById(R.id.single_EC2);
        singleEC2Temp = findViewById(R.id.single_EC2_temp);
        singlePH = findViewById(R.id.single_PH);
        singlePHTemp = findViewById(R.id.single_PH_temp);

        singleLayout2 = findViewById(R.id.single_layout_2); // 单参数第二行
        singleORP = findViewById(R.id.single_ORP);
        singleRDO = findViewById(R.id.single_RDO);
        singleRDOTemp = findViewById(R.id.single_RDO_temp);
        singleNHN = findViewById(R.id.single_NHN);
        singleNHNTemp = findViewById(R.id.single_NHN_temp);
        singleZS = findViewById(R.id.single_ZS);
        singleZSTemp = findViewById(R.id.single_ZS_temp);

        singleLayout3 = findViewById(R.id.single_layout_3); // 单参数第三行
        singleCOD = findViewById(R.id.single_COD);
        singleCODTemp = findViewById(R.id.single_COD_temp);
        singleCODZS = findViewById(R.id.single_COD_ZS);
        singleCODBOD = findViewById(R.id.single_COD_BOD);
        singleCH = findViewById(R.id.single_CH);
        singleCHTemp = findViewById(R.id.single_CH_temp);

        singleLayout4 = findViewById(R.id.single_layout_4); // 单参数第四行
        singleRC = findViewById(R.id.single_RC);
        singleRCTemp = findViewById(R.id.single_RC_temp);
        singleCY = findViewById(R.id.single_CY);
        singleCYTemp = findViewById(R.id.single_CY_temp);
        singleTSS = findViewById(R.id.single_TSS);
        singleTSSTemp = findViewById(R.id.single_TSS_temp);

        singleLayout5 = findViewById(R.id.single_layout_5); // 单参数第五行
        singleTSM = findViewById(R.id.single_TSM);
        singleTSMTemp = findViewById(R.id.single_TSM_temp);
        singleOW = findViewById(R.id.single_OW);
        singleOWTemp = findViewById(R.id.single_OW_temp);
        singleSAL = findViewById(R.id.single_SAL);
        singleSALTemp = findViewById(R.id.single_SAL_temp);

        mutilLayout = findViewById(R.id.mutil_layout); // 多参数
        mutilLayout1 = findViewById(R.id.mutil_layout_1); // 多参数第一行
        mutilTemp = findViewById(R.id.mutil_TEMP);
        mutilCOD = findViewById(R.id.mutil_COD);
        mutilCODZS = findViewById(R.id.mutil_COD_ZS);
        mutilECSAL = findViewById(R.id.mutil_EC_OR_SAL);

        mutilLayout2 = findViewById(R.id.mutil_layout_2); // 多参数第二行
        mutilPH = findViewById(R.id.mutil_PH);
        mutilORP = findViewById(R.id.mutil_ORP);
        mutilRDO = findViewById(R.id.mutil_RDO);
        mutilNHN = findViewById(R.id.mutil_NHN);
        mutilZS = findViewById(R.id.mutil_ZS);

        dataValidEC1 = findViewById(R.id.data_valid_EC1);
        dataValidEC2 = findViewById(R.id.data_valid_EC2);
        dataValidPH = findViewById(R.id.data_valid_PH);
        dataValidORP = findViewById(R.id.data_valid_ORP);
        dataValidRDO = findViewById(R.id.data_valid_RDO);
        dataValidNHN = findViewById(R.id.data_valid_NHN);
        dataValidZS = findViewById(R.id.data_valid_ZS);
        dataValidSAL = findViewById(R.id.data_valid_SAL);
        dataValidCOD = findViewById(R.id.data_valid_COD);
        dataValidRC = findViewById(R.id.data_valid_RC);
        dataValidCH = findViewById(R.id.data_valid_CH);
        dataValidCY = findViewById(R.id.data_valid_CY);
        dataValidTSS = findViewById(R.id.data_valid_TSS);
        dataValidTSM = findViewById(R.id.data_valid_TSM);
        dataValidOW = findViewById(R.id.data_valid_OW);
        dataValidMutil = findViewById(R.id.data_valid_MUTIL);

        // 重新规划的单参数折线图
        lineChartSingleEC1 = findViewById(R.id.dy_line_chart_single_EC1);
        lineChartSingleEC1Temp = findViewById(R.id.dy_line_chart_single_EC1_temp);
        lineChartSingleEC2 = findViewById(R.id.dy_line_chart_single_EC2);
        lineChartSingleEC2Temp = findViewById(R.id.dy_line_chart_single_EC2_temp);
        lineChartSinglePH = findViewById(R.id.dy_line_chart_single_PH);
        lineChartSinglePHTemp = findViewById(R.id.dy_line_chart_single_PH_temp);
        lineChartSingleORP = findViewById(R.id.dy_line_chart_single_ORP);
        lineChartSingleRDO = findViewById(R.id.dy_line_chart_single_RDO);
        lineChartSingleRDOTemp = findViewById(R.id.dy_line_chart_single_RDO_temp);
        lineChartSingleNHN = findViewById(R.id.dy_line_chart_single_NHN);
        lineChartSingleNHNTemp = findViewById(R.id.dy_line_chart_single_NHN_temp);
        lineChartSingleZS = findViewById(R.id.dy_line_chart_single_ZS);
        lineChartSingleZSTemp = findViewById(R.id.dy_line_chart_single_ZS_temp);
        lineChartSingleSAL = findViewById(R.id.dy_line_chart_single_SAL);
        lineChartSingleSALTemp = findViewById(R.id.dy_line_chart_single_SAL_temp);
        lineChartSingleCOD = findViewById(R.id.dy_line_chart_single_COD);
        lineChartSingleCODTemp = findViewById(R.id.dy_line_chart_single_COD_temp);
        lineChartSingleCODZS = findViewById(R.id.dy_line_chart_single_COD_ZS);
        lineChartSingleCODBOD = findViewById(R.id.dy_line_chart_single_COD_BOD);
        lineChartSingleRC = findViewById(R.id.dy_line_chart_single_RC);
        lineChartSingleRCTemp = findViewById(R.id.dy_line_chart_single_RC_temp);
        lineChartSingleCH = findViewById(R.id.dy_line_chart_single_CH);
        lineChartSingleCHTemp = findViewById(R.id.dy_line_chart_single_CH_temp);
        lineChartSingleCY = findViewById(R.id.dy_line_chart_single_CY);
        lineChartSingleCYTemp = findViewById(R.id.dy_line_chart_single_CY_temp);
        lineChartSingleTSS = findViewById(R.id.dy_line_chart_single_TSS);
        lineChartSingleTSSTemp = findViewById(R.id.dy_line_chart_single_TSS_temp);
        lineChartSingleTSM = findViewById(R.id.dy_line_chart_single_TSM);
        lineChartSingleTSMTemp = findViewById(R.id.dy_line_chart_single_TSM_temp);
        lineChartSingleOW = findViewById(R.id.dy_line_chart_single_OW);
        lineChartSingleOWTemp = findViewById(R.id.dy_line_chart_single_OW_temp);

        // 折线图显示函数
//        displayLineChart(-1);

//        proData.setVisibility(View.GONE);

        historyLayout = findViewById(R.id.history_layout);
        btn = findViewById(R.id.delete_history);
        downloadBtn = findViewById(R.id.download_history);

        final View listData = findViewById(R.id.data_list); // 数据列表
        final View lineChartData = findViewById(R.id.data_line); // 折线图
//        final View rankChartData = findViewById(R.id.data_rank); // 排位图
//        final View distributeChartData = findViewById(R.id.data_distribute); // 分布曲线图

        // 初始化页面
        // 隐藏所有的属性标签
        hideAttributeTag();
        // 隐藏所有的有效数据文字




//        dataTotal.setText(R.string.data_total + "0" + R.string.data_number);
//        dataValid.setText(getString(R.string.no_data));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.history_data_activity));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 设置导航（返回图片）的点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 退出当前页面
                BleManager.getInstance().stopNotify(bleDevice, characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());
                finish();
            }
        });


        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication)getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication)getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication)getApplication()).getBasicDeviceType();

        bleDevice = ((MyApplication)getApplication()).getBasicBleDevice();
        bleManager = ((MyApplication)getApplication()).getBasicBleManager();
        gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        if (gatt != null) {
            for (BluetoothGattService s : gatt.getServices()) {
                service = s;
                if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                    // 安信可芯片
                    BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
                    int ii = 0;
                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                        System.out.println("特征值值内容：" + c.getUuid().toString());
                        cs[ii ++] = c;
                    }
                    characteristicRead = cs[1];
                    characteristic = cs[0];
                    chipType = 1;
                    break;
                }
                chipType = 2;
            }
        }
        if(chipType == 2) {
            // BT02-E104芯片
            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
                String uid = c.getUuid().toString().split("-")[0];
                if ("0000fff1".equals(uid)) {
                    //                if ("5833ff02".equals(uid)) {
                    characteristicRead = c;
                } else if ("0000fff2".equals(uid)) {
                    //                } else if ("5833ff03".equals(uid)) {
                    characteristic = c;
                }
            }
        }
        System.out.println("characteristic的数据--->getUuid:" + characteristic.getUuid().toString());
        System.out.println("characteristicRead的数据--->getUuid:" + characteristicRead.getUuid().toString());

        // 初始化
//        initWay(0);
//        initWay(1);
        initDyLineChart();

        // 初始化文字
        initMutilSensorNameSet();

        // 单参数电导率1
        singleEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleEC1);

                hideValidDataBox();
                dataValidEC1.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleEC1.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleEC1Temp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数电导率1温度
        singleEC1Temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleEC1Temp);

                hideValidDataBox();
                dataValidEC1.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleEC1Temp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数电导率2
        singleEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleEC2);

                hideValidDataBox();
                dataValidEC2.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleEC2.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleEC2Temp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数电导率2温度
        singleEC2Temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleEC2Temp);

                hideValidDataBox();
                dataValidEC2.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleEC2Temp.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleEC2Temp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数PH
        singlePH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singlePH);

                hideValidDataBox();
                dataValidPH.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSinglePH.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singlePHTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数PH温度
        singlePHTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singlePHTemp);

                hideValidDataBox();
                dataValidPH.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSinglePHTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数ORP
        singleORP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleORP);

                hideValidDataBox();
                dataValidORP.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleORP.setVisibility(View.VISIBLE);

            }
        });

        // 单参数RDO
        singleRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleRDO);

                hideValidDataBox();
                dataValidRDO.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleRDO.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleRDOTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数RDO温度
        singleRDOTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleRDOTemp);

//                hideValidDataBox();
//                dataValidRDO.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleRDOTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数NHN
        singleNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleNHN);

                hideValidDataBox();
                dataValidNHN.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleNHN.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleNHNTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数NHN温度
        singleNHNTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleNHNTemp);

                hideValidDataBox();
                dataValidNHN.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleNHNTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数浊度
        singleZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleZS);

                hideValidDataBox();
                dataValidZS.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleZS.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleZSTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数浊度温度
        singleZSTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleZSTemp);

                hideValidDataBox();
                dataValidZS.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleZSTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数盐度
        singleSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleSAL);

                hideValidDataBox();
                dataValidSAL.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleSAL.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleSALTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数盐度温度
        singleSALTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleSALTemp);

                hideValidDataBox();
                dataValidSAL.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleSALTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数COD
        singleCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCOD);

                hideValidDataBox();
                dataValidCOD.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCOD.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleCODTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数COD温度
        singleCODTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCODTemp);

                hideValidDataBox();
                dataValidCOD.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCODTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数COD内置浊度
        singleCODZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCODZS);

                hideValidDataBox();
                dataValidCOD.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCODZS.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleCODTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数COD内置BOD
        singleCODBOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCODBOD);

                hideValidDataBox();
                dataValidCOD.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCODBOD.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleCODTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数余氯
        singleRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleRC);

                hideValidDataBox();
                dataValidRC.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleRC.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleRCTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数余氯温度
        singleRCTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleRCTemp);

                hideValidDataBox();
                dataValidRC.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleRCTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数叶绿素
        singleCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCH);

                hideValidDataBox();
                dataValidCH.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCH.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleCHTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数叶绿素温度
        singleCHTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCHTemp);

                hideValidDataBox();
                dataValidCH.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCHTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数蓝绿藻
        singleCY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCY);

                hideValidDataBox();
                dataValidCY.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCY.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleCYTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数蓝绿藻温度
        singleCYTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleCYTemp);

                hideValidDataBox();
                dataValidCY.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleCYTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数透明度
        singleTSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleTSS);

                hideValidDataBox();
                dataValidTSS.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleTSS.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleTSSTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数透明度温度
        singleTSSTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleTSSTemp);

                hideValidDataBox();
                dataValidTSS.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleTSSTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数悬浮物
        singleTSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleTSM);

                hideValidDataBox();
                dataValidTSM.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleTSM.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleTSMTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数悬浮物温度
        singleTSMTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleTSMTemp);

                hideValidDataBox();
                dataValidTSM.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleTSMTemp.setVisibility(View.VISIBLE);

            }
        });

        // 单参数水中油
        singleOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleOW);

                hideValidDataBox();
                dataValidOW.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleOW.setVisibility(View.VISIBLE);

                hideAllTempTag();
                singleOWTemp.setVisibility(View.VISIBLE);
            }
        });

        // 单参数水中油温度
        singleOWTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(singleOWTemp);

                hideValidDataBox();
                dataValidOW.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartSingleOWTemp.setVisibility(View.VISIBLE);

            }
        });

        // 多参数温度
        mutilTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilTemp);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChart0.setVisibility(View.VISIBLE);

            }
        });

        // 多参数COD
        mutilCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilCOD);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartCOD.setVisibility(View.VISIBLE);

            }
        });

        // 多参数COD内置浊度
        mutilCODZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilCODZS);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartCODNT.setVisibility(View.VISIBLE);

            }
        });

        // 多参数电导率/温度
        mutilECSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilECSAL);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartEC.setVisibility(View.VISIBLE);

            }
        });

        // 多参数PH
        mutilPH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilPH);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartPH.setVisibility(View.VISIBLE);

            }
        });

        // 多参数ORP
        mutilORP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilORP);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartORP.setVisibility(View.VISIBLE);

            }
        });

        // 多参数RDO
        mutilRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilRDO);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartDO.setVisibility(View.VISIBLE);

            }
        });

        // 多参数NHN
        mutilNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilNHN);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartNH.setVisibility(View.VISIBLE);

            }
        });

        // 多参数ZS
        mutilZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAttributeToCommon();
                displayAttributeToHighLight(mutilZS);

                hideValidDataBox();
                dataValidMutil.setVisibility(View.VISIBLE);

                hideLineChart();
                lineChartNT.setVisibility(View.VISIBLE);

            }
        });


        // 获取传感器的设备类型，渲染对应的页面
        int type1 = ((MyApplication)getApplication()).getBasicDeviceType(); // 传感器类型
        type = ((MyApplication)getApplication()).getBasicType(); // 设备类型



        // 下载历史数据
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 遍历所有的数据进行下载
                new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.history_data_download_confirm)).setNegativeButton(getString(R.string.ensure),
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialogReal = new ProgressDialog(historyLayout.getContext());
                                progressDialogReal.setIcon(R.mipmap.ic_launcher);
                                progressDialogReal.setTitle(getString(R.string.history_data_download));
                                progressDialogReal.setMessage(getString(R.string.start_download));
                                progressDialogReal.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                                progressDialogReal.setCancelable(true);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                                progressDialogReal.show();
//                                for(int i = 0; i < historyData.size(); i ++) {
//                                    progressDialogReal.setMessage(getString(R.string.downloading) + "      "+ i + "/" + historyData.size());
//                                    MyLog.writeLogToNotifyFile(historyData.get(i));
//                                }
                                if(historyData.size() == 0) {
                                    progressDialogReal.setMessage(getString(R.string.no_valid_data));
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialogReal.dismiss();
                                        }
                                    },1500);
                                } else {
                                    MyLog.categoryToDownload(historyData, ((MyApplication) getApplication()).getMutilSensor());
                                    progressDialogReal.setMessage(getString(R.string.download_success));
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialogReal.dismiss();
//                                            new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.file_in) + getString(R.string.file_folder) + getString(R.string.file_open) ).setNegativeButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    openAssignFolder("/storage/emulated/0/蓝牙");
//                                                }
//                                            }).setPositiveButton(getString(R.string.cancel),null).show();

                                        }
                                    },1500);
                                }
                                }

                        }).setPositiveButton(getString(R.string.cancel),null).show();

            }
        });

        if(false) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleManager.getInstance().stopNotify(bleDevice, characteristicRead.getService().getUuid().toString(), characteristicRead.getUuid().toString());

                    // 获取所有的历史数据
                    final String hex = "fb";

                    getHistoryData(hex);

                }
            }, 500);
        }
        // 首先获取有多少条历史数据
        getHistoryDataByF6();

        // 延时读取f6
        handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                if(isF6Success) {
                    System.out.println("再一次执行f6程序");

                    getHistoryDataByF6();

                }
                handler.removeCallbacks(runnable);
            }
        };
        handler.postDelayed(runnable, 4000);//每4秒执行一次runnable.

        // 点击数据列表，以列表的形式显示数据
        listData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                selectTextViewStyle(1);
            }
        });

        // 点击折线图，以折线图的形式显示数据
        lineChartData.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                selectTextViewStyle(2);
//                initWay1();
//                showLineChart();

                //死循环添加数据
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (true) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    list.add((int) (Math.random() * 50) + 10);
//                                    list.add((int) (Math.random() * 80) + 10);
//                                    dynamicLineChartManager2.addEntry(list);
//                                    list.clear();
//                                }
//                            });
//                        }
//                    }
//                }).start();
            }

        });

        // 点击排名图，以排名图的形式显示数据
//        rankChartData.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void onClick(View v) {
//                selectTextViewStyle(3);
//
//                HorizontalBarChart mHorizontalBarChart = (HorizontalBarChart) findViewById(R.id.mHorizontalBarChart);
//
//                //设置相关属性
////                mHorizontalBarChart.setOnChartValueSelectedListener(this);
//                mHorizontalBarChart.setDrawBarShadow(false);
//                mHorizontalBarChart.setDrawValueAboveBar(true);
//                mHorizontalBarChart.getDescription().setEnabled(false);
//                mHorizontalBarChart.setMaxVisibleValueCount(60);
//                mHorizontalBarChart.setPinchZoom(false);
//                mHorizontalBarChart.setDrawGridBackground(false);
//
//                //x轴
//                XAxis xl = mHorizontalBarChart.getXAxis();
//                xl.setPosition(XAxis.XAxisPosition.BOTTOM);
//                xl.setDrawAxisLine(true);
//                xl.setDrawGridLines(false);
//                xl.setGranularity(10f);
//
//                //y轴
//                YAxis yl = mHorizontalBarChart.getAxisLeft();
//                yl.setDrawAxisLine(true);
//                yl.setDrawGridLines(true);
//                yl.setAxisMinimum(0f);
//
//                //y轴
//                YAxis yr = mHorizontalBarChart.getAxisRight();
//                yr.setDrawAxisLine(true);
//                yr.setDrawGridLines(false);
//                yr.setAxisMinimum(0f);
//
//                //设置数据
////                setData(12, 50);
//                int count = 12;
//                float range = 50;
//                float barWidth = 9f;
//                float spaceForBar = 10f;
//                ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
//                for (int i = 0; i < count; i++) {
//                    float val = (float) (Math.random() * range);
//                    yVals1.add(new BarEntry(i * spaceForBar, val));
//                }
//                BarDataSet set1;
//                if (mHorizontalBarChart.getData() != null &&
//                        mHorizontalBarChart.getData().getDataSetCount() > 0) {
//                    set1 = (BarDataSet) mHorizontalBarChart.getData().getDataSetByIndex(0);
//                    set1.setValues(yVals1);
//                    mHorizontalBarChart.getData().notifyDataChanged();
//                    mHorizontalBarChart.notifyDataSetChanged();
//                } else {
//                    set1 = new BarDataSet(yVals1, "DataSet 1");
//
//                    ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
//                    dataSets.add(set1);
//
//                    BarData data = new BarData(dataSets);
//                    data.setValueTextSize(10f);
//                    data.setBarWidth(barWidth);
//                    mHorizontalBarChart.setData(data);
//                }
//
//                mHorizontalBarChart.setFitBars(true);
//                mHorizontalBarChart.animateY(2500);
//
//                Legend l = mHorizontalBarChart.getLegend();
//                l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
//                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
//                l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
//                l.setDrawInside(false);
//                l.setFormSize(8f);
//                l.setXEntrySpace(4f);
//
//            }
//        });

        // 点击分布曲线图，以分布曲线图显示数据
//        distributeChartData.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void onClick(View v) {
//                selectTextViewStyle(4);
//
//            }
//        });

        // 清除历史记录
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.history_data_delete_confirm)).setNegativeButton(getString(R.string.ensure),
                        new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        HexUtil.hexStringToBytes("fd"),
                                        new BleWriteCallback() {
                                            @Override
                                            public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                addText(input, "write success, current: " + current
//                                                        + " total: " + total
//                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                        // 通知用户数据清除完成
                                                        new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.history_data_delete_success)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                        lineChart.fitScreen();
//                                                        for(LineChart l : lineCharts) {
//                                                            l.fitScreen();
//                                                        }


                                                        // 隐藏属性框
                                                        hideAllTempTag();
                                                        hideAttributeTag();
                                                        hideLineChart();
                                                        hideValidDataBox();
                                                        // 隐藏折线图

//                                                        dy.destroyChart();
//                                                        for(DyLineChartUtils d : dys) {
//                                                            d.destroyChart();
//                                                        }
                                                        dataTotal.setText(getString(R.string.data_total) + 0 + getString(R.string.data_number));
//                                                        dataValid.setText(getString(R.string.data_total_effective)  + 0 + getString(R.string.data_number));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onWriteFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                      addText(input, exception.toString());
                                                        new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.history_data_delete_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                                                        destoryWay(type);
                                                    }
                                                });
                                            }
                                        });


                            }
                        }).setPositiveButton(getString(R.string.cancel),null).show();



            }
        });


    }

    // 点击按钮，修改列表头样式:折线图、直方图等
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void selectTextViewStyle(int num) {
        TextView listDataT = (TextView) findViewById(R.id.data_list);
        TextView lineChartDataT = (TextView) findViewById(R.id.data_line); // 折线图
//        TextView rankChartDataT = (TextView) findViewById(R.id.data_rank); // 排位图
//        TextView distributeChartDataT = (TextView) findViewById(R.id.data_distribute); // 分布曲线图
        View listLayout = findViewById(R.id.list_layout);
//        View lineLayout = findViewById(R.id.line_layout);
        View horizontalBarLayout = findViewById(R.id.horizontalBar_layout);

        listDataT.setTextColor(0xFF000000);
        lineChartDataT.setTextColor(0xFF000000);
//        rankChartDataT.setTextColor(0xFF000000);
//        distributeChartDataT.setTextColor(0xFF000000);

        Drawable d = getResources().getDrawable(R.drawable.textview_bottom_border);
        listDataT.setBackground(null);
        lineChartDataT.setBackground(null);
//        rankChartDataT.setBackground(null);
//        distributeChartDataT.setBackground(null);

        listLayout.setVisibility(View.GONE);
//        lineLayout.setVisibility(View.GONE);
        horizontalBarLayout.setVisibility(View.GONE);

        if(num == 1) {
            listDataT.setTextColor(0xFF36a7fe);
            listDataT.setBackground(d);
            listLayout.setVisibility(View.VISIBLE);
        }else if(num == 2) {
            lineChartDataT.setTextColor(0xFF36a7fe);
            lineChartDataT.setBackground(d);
//            lineLayout.setVisibility(View.VISIBLE);
        }
//        else if(num == 3) {
//            rankChartDataT.setTextColor(0xFF36a7fe);
//            rankChartDataT.setBackground(d);
//            horizontalBarLayout.setVisibility(View.VISIBLE);
//        }else if(num == 4) {
//            distributeChartDataT.setTextColor(0xFF36a7fe);
//            distributeChartDataT.setBackground(d);
//        }
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }

    // 获取设备单位
    public String getDeviceUnit(int a) {
        if(a == 0) {
            return "";
        }else if(a == 1) {
            return "uS/cm";
        }else if(a == 2) {
            return "mS/cm";
        }else if(a == 3) {
            return "PH";
        }else if(a == 4) {
            return "mv";
        }else if(a == 5) {
            return "mg/L";
        }else if(a == 6) {
            return "mg/L";
        }else if(a == 7) {
            return "NTU";
        }else if(a == 8) {
            return "PSU";
        }else if(a == 9) {
            return "mg/L";
        }else if(a == 10) {
            return "mg/L";
        }else if(a == 11) {
            return "ug/L";
        }else if(a == 12) {
            return "kcells/ml";
        }else if(a == 13) {
            return "mm";
        }else if(a == 14) {
            return "mg/L";
        }else if(a == 15) {
            return "mg/LF";
        }
        return "----";
    }

    // 清除折线图信息
    public void destoryWay(int n) {
        // 单参数
        if(n == 0) {
            if(lineChart != null) {
                lineChart.fitScreen();
                lineChart0.fitScreen();
                System.out.println("单参数折线图删除成功");
            }
        }

        // 多参数
        if(n == 1) {
            for(int i = 0; i < 8; i ++) {
                if(lineCharts[i] != null) {
                    lineCharts[i].fitScreen();
                }
            }
            System.out.println("多参数折线图删除成功");
        }
    }


    // 电导率1
    public void displayHistoryDataEC1(String data) {
        // 如果是电导率1：设备类型，传感器类型，EC1，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("1")) {

            // 1、显示对应属性按钮
            displayAttributeTag(1);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }

            // EC1数值渲染到折线图中
            digits = data.replaceAll("[^0-9.,]", "");
            if(data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 0, d);
            changeLineChartSingleLowerLimit(0, 0, d);
            // 渲染数据
            dySingleEC1.setYAxis(singleChartMax[0], singleChartMin[0], singleChartLabelCount[0]);
            dySingleEC1.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 1, d);
            changeLineChartSingleLowerLimit(0, 1, d);
            // 渲染数据
            dySingleEC1Temp.setYAxis(singleChartMax[1], singleChartMin[1], singleChartLabelCount[1]);
            dySingleEC1Temp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[1] ++;
            dataValidEC1.setText(getString(R.string.data_total_effective)  + validData[1] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));

        }

    }

    // 电导率2
    public void displayHistoryDataEC2(String data) {
        // 如果是电导率2：设备类型，传感器类型，EC2，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("2")) {

            // 1、显示属性名称
            displayAttributeTag(2);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 2, d);
            changeLineChartSingleLowerLimit(0, 2, d);
            // 渲染数据
            dySingleEC2.setYAxis(singleChartMax[2], singleChartMin[2], singleChartLabelCount[2]);
            dySingleEC2.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 3, d);
            changeLineChartSingleLowerLimit(0, 3, d);
            // 渲染数据
            dySingleEC2Temp.setYAxis(singleChartMax[3], singleChartMin[3], singleChartLabelCount[3]);
            dySingleEC2Temp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[2] ++;
            dataValidEC2.setText(getString(R.string.data_total_effective)  + validData[2] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // PH
    public void displayHistoryDataPH(String data) {
        // 如果是PH：设备类型，传感器类型，PH，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("3")) {

            // 1、显示属性值
            displayAttributeTag(3);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if(data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 4, d);
            changeLineChartSingleLowerLimit(0, 4, d);
            // 渲染数据
            dySinglePH.setYAxis(singleChartMax[4], singleChartMin[4], singleChartLabelCount[4]);
            dySinglePH.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 5, d);
            changeLineChartSingleLowerLimit(0, 5, d);
            // 渲染数据
            dySinglePHTemp.setYAxis(singleChartMax[5], singleChartMin[5], singleChartLabelCount[5]);
            dySinglePHTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[3] ++;
            dataValidPH.setText(getString(R.string.data_total_effective)  + validData[3] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }
    }

    // ORP
    public void displayHistoryDataORP(String data) {
        // 如果是ORP：设备类型，传感器类型，ORP

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 3 && type0.equals("0") && type.equals("4")) {

            // 1、显示属性值
            displayAttributeTag(4);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 6, d);
            changeLineChartSingleLowerLimit(0, 6, d);
            // 渲染数据
            dySingleORP.setYAxis(singleChartMax[6], singleChartMin[6], singleChartLabelCount[6]);
            dySingleORP.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[4] ++;
            dataValidORP.setText(getString(R.string.data_total_effective)  + validData[4] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 溶解氧
    public void displayHistoryDataRDO(String data) {
        // 如果是溶解氧：设备类型，传感器类型，RDO，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("5")) {

            // 1、显示属性值
            displayAttributeTag(5);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 7, d);
            changeLineChartSingleLowerLimit(0, 7, d);
            // 渲染数据
            dySingleRDO.setYAxis(singleChartMax[7], singleChartMin[7], singleChartLabelCount[7]);
            dySingleRDO.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 8, d);
            changeLineChartSingleLowerLimit(0, 8, d);
            // 渲染数据
            dySingleRDOTemp.setYAxis(singleChartMax[8], singleChartMin[8], singleChartLabelCount[8]);
            dySingleRDOTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[5] ++;
            dataValidRDO.setText(getString(R.string.data_total_effective)  + validData[5] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 氨氮
    public void displayHistoryDataNHN(String data) {
        // 如果是氨氮：设备类型，传感器类型，NHN，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("6")) {

            // 1、显示属性值
            displayAttributeTag(6);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 9, d);
            changeLineChartSingleLowerLimit(0, 9, d);
            // 渲染数据
            dySingleNHN.setYAxis(singleChartMax[9], singleChartMin[9], singleChartLabelCount[9]);
            dySingleNHN.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 10, d);
            changeLineChartSingleLowerLimit(0, 10, d);
            // 渲染数据
            dySingleNHNTemp.setYAxis(singleChartMax[10], singleChartMin[10], singleChartLabelCount[10]);
            dySingleNHNTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[6] ++;
            dataValidNHN.setText(getString(R.string.data_total_effective)  + validData[6] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 浊度
    public void displayHistoryDataZS(String data) {
        // 如果是浊度：设备类型，传感器类型，ZS，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("7")) {

            // 1、显示属性值
            displayAttributeTag(7);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 11, d);
            changeLineChartSingleLowerLimit(0, 11, d);
            // 渲染数据
            dySingleZS.setYAxis(singleChartMax[11], singleChartMin[11], singleChartLabelCount[11]);
            dySingleZS.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 12, d);
            changeLineChartSingleLowerLimit(0, 12, d);
            // 渲染数据
            dySingleZSTemp.setYAxis(singleChartMax[12], singleChartMin[12], singleChartLabelCount[12]);
            dySingleZSTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[7] ++;
            dataValidZS.setText(getString(R.string.data_total_effective)  + validData[7] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 盐度
    public void displayHistoryDataSAL(String data) {
        // 如果是盐度：设备类型，传感器类型，SAL，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("8")) {

            // 1、显示属性值
            displayAttributeTag(8);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 13, d);
            changeLineChartSingleLowerLimit(0, 13, d);
            // 渲染数据
            dySingleSAL.setYAxis(singleChartMax[13], singleChartMin[13], singleChartLabelCount[13]);
            dySingleSAL.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 14, d);
            changeLineChartSingleLowerLimit(0, 14, d);
            // 渲染数据
            dySingleSALTemp.setYAxis(singleChartMax[14], singleChartMin[14], singleChartLabelCount[14]);
            dySingleSALTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[8] ++;
            dataValidSAL.setText(getString(R.string.data_total_effective)  + validData[8] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 化学需氧量COD
    public void displayHistoryDataCOD(String data) {
        // 如果是COD：设备类型，传感器类型，COD，温度，浊度，BOD

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];
        if (data.split(",").length == 6 && type0.equals("0") && type.equals("9")) {

            // 1、显示属性值
            displayAttributeTag(9);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if(data.length() != digits.length()) {
                return;
            }
            // 数据存到数据数组中
//            historyData.add(data);

            // COD数值
            Double d = Double.parseDouble(data.split(",")[2]);
            changeLineChartSingleUpperLimit(0,15, d);
            changeLineChartSingleLowerLimit(0, 15, d);
            dySingleCOD.setYAxis(singleChartMax[15], singleChartMin[15], singleChartLabelCount[15]);
            dySingleCOD.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            changeLineChartSingleUpperLimit(0,16, d);
            changeLineChartSingleLowerLimit(0, 16, d);
            dySingleCODTemp.setYAxis(singleChartMax[16], singleChartMin[16], singleChartLabelCount[16]);
            dySingleCODTemp.addEntry(d);

            // COD内置浊度
            d = Double.parseDouble(data.split(",")[4]);
            changeLineChartSingleUpperLimit(0,17, d);
            changeLineChartSingleLowerLimit(0, 17, d);
            dySingleCODZS.setYAxis(singleChartMax[17], singleChartMin[17], singleChartLabelCount[17]);
            dySingleCODZS.addEntry(d);

            // COD内置BOD
            d = Double.parseDouble(data.split(",")[5]);
            changeLineChartSingleUpperLimit(0,18, d);
            changeLineChartSingleLowerLimit(0, 18, d);
            dySingleCODBOD.setYAxis(singleChartMax[18], singleChartMin[18], singleChartLabelCount[18]);
            dySingleCODBOD.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[9] ++;
            dataValidCOD.setText(getString(R.string.data_total_effective)  + validData[9] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 余氯
    public void displayHistoryDataRC(String data) {
        // 如果是余氯：设备类型，传感器类型，RC，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("10")) {

            // 1、显示属性值
            displayAttributeTag(10);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 19, d);
            changeLineChartSingleLowerLimit(0, 19, d);
            // 渲染数据
            dySingleRC.setYAxis(singleChartMax[19], singleChartMin[19], singleChartLabelCount[19]);
            dySingleRC.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 20, d);
            changeLineChartSingleLowerLimit(0, 20, d);
            // 渲染数据
            dySingleRCTemp.setYAxis(singleChartMax[20], singleChartMin[20], singleChartLabelCount[20]);
            dySingleRCTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[10] ++;
            dataValidRC.setText(getString(R.string.data_total_effective)  + validData[10] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 叶绿素
    public void displayHistoryDataCH(String data) {
        // 如果是叶绿素：设备类型，传感器类型，CH，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("11")) {

            // 1、显示属性值
            displayAttributeTag(11);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 21, d);
            changeLineChartSingleLowerLimit(0, 21, d);
            // 渲染数据
            dySingleCH.setYAxis(singleChartMax[21], singleChartMin[21], singleChartLabelCount[21]);
            dySingleCH.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 22, d);
            changeLineChartSingleLowerLimit(0, 22, d);
            // 渲染数据
            dySingleCHTemp.setYAxis(singleChartMax[22], singleChartMin[22], singleChartLabelCount[22]);
            dySingleCHTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[11] ++;
            dataValidCH.setText(getString(R.string.data_total_effective)  + validData[11] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 蓝绿藻
    public void displayHistoryDataCY(String data) {
        // 如果是蓝绿藻：设备类型，传感器类型，CY，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("12")) {

            // 1、显示属性值
            displayAttributeTag(12);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 23, d);
            changeLineChartSingleLowerLimit(0, 23, d);
            // 渲染数据
            dySingleCY.setYAxis(singleChartMax[23], singleChartMin[23], singleChartLabelCount[23]);
            dySingleCY.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 24, d);
            changeLineChartSingleLowerLimit(0, 24, d);
            // 渲染数据
            dySingleCYTemp.setYAxis(singleChartMax[24], singleChartMin[24], singleChartLabelCount[24]);
            dySingleCYTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[12] ++;
            dataValidCY.setText(getString(R.string.data_total_effective)  + validData[12] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }
    }

    // 透明度
    public void displayHistoryDataTSS(String data) {
        // 如果是透明度：设备类型，传感器类型，TSS，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("13")) {

            // 1、显示属性值
            displayAttributeTag(13);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 25, d);
            changeLineChartSingleLowerLimit(0, 25, d);
            // 渲染数据
            dySingleTSS.setYAxis(singleChartMax[25], singleChartMin[25], singleChartLabelCount[25]);
            dySingleTSS.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 26, d);
            changeLineChartSingleLowerLimit(0, 26, d);
            // 渲染数据
            dySingleTSSTemp.setYAxis(singleChartMax[26], singleChartMin[26], singleChartLabelCount[26]);
            dySingleTSSTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[13]++;
            dataValidTSS.setText(getString(R.string.data_total_effective)  + validData[13] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 悬浮物
    public void displayHistoryDataTSM(String data) {
        // 如果是悬浮物：设备类型，传感器类型，TSM，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("14")) {

            // 1、显示属性值
            displayAttributeTag(14);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 27, d);
            changeLineChartSingleLowerLimit(0, 27, d);
            // 渲染数据
            dySingleTSM.setYAxis(singleChartMax[27], singleChartMin[27], singleChartLabelCount[27]);
            dySingleTSM.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 28, d);
            changeLineChartSingleLowerLimit(0, 28, d);
            // 渲染数据
            dySingleTSMTemp.setYAxis(singleChartMax[28], singleChartMin[28], singleChartLabelCount[28]);
            dySingleTSMTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[14] ++;
            dataValidTSM.setText(getString(R.string.data_total_effective)  + validData[14] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 水中油
    public void displayHistoryDataOW(String data) {
        // 如果是水中油：设备类型，传感器类型，OW，温度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 4 && type0.equals("0") && type.equals("15")) {

            // 1、显示属性值
            displayAttributeTag(15);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 29, d);
            changeLineChartSingleLowerLimit(0, 29, d);
            // 渲染数据
            dySingleOW.setYAxis(singleChartMax[29], singleChartMin[29], singleChartLabelCount[29]);
            dySingleOW.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 30, d);
            changeLineChartSingleLowerLimit(0, 30, d);
            // 渲染数据
            dySingleOWTemp.setYAxis(singleChartMax[30], singleChartMin[30], singleChartLabelCount[30]);
            dySingleOWTemp.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[15] ++;
            dataValidOW.setText(getString(R.string.data_total_effective)  + validData[15] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 多参数
    public void displayHistoryDataMUL(String data) {
        // 如果是多参数：设备类型，传感器类型，温度，COD，COD内置浊度，电导率/盐度，PH，ORP，溶解氧，NHN，浊度

        for(int i = 0; i < data.split(",").length; i ++) {
            if(data.split(",")[i].length() == 0) {
                return;
            }
        }

        String type0 = data.split(",")[0];
        String type = data.split(",")[1];

        if (data.split(",").length == 11 && type0.equals("1")) {

            // 1、显示属性值
            displayAttributeTag(999);

            // 2、渲染折线图
            String digits = data.replaceAll("[^0-9.,]", "");
            if (data.length() != digits.length()) {
                return;
            }
            // 温度
            Double d = Double.parseDouble(data.split(",")[2]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 0, d);
            changeLineChartSingleLowerLimit(1, 0, d);
            // 渲染数据
            dy0.setYAxis(mutilChartMax[0], mutilChartMin[0], mutilChartLabelCount[0]);
            dy0.addEntry(d);

            // COD
            d = Double.parseDouble(data.split(",")[3]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 1, d);
            changeLineChartSingleLowerLimit(1, 1, d);
            // 渲染数据
            dyCOD.setYAxis(mutilChartMax[1], mutilChartMin[1], mutilChartLabelCount[1]);
            dyCOD.addEntry(d);

            // COD内置浊度
            d = Double.parseDouble(data.split(",")[4]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 2, d);
            changeLineChartSingleLowerLimit(1, 2, d);
            // 渲染数据
            dyCODNT.setYAxis(mutilChartMax[2], mutilChartMin[2], mutilChartLabelCount[2]);
            dyCODNT.addEntry(d);

            // 电导率/盐度
            d = Double.parseDouble(data.split(",")[5]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 3, d);
            changeLineChartSingleLowerLimit(1, 3, d);
            // 渲染数据
            dyEC.setYAxis(mutilChartMax[3], mutilChartMin[3], mutilChartLabelCount[3]);
            dyEC.addEntry(d);

            // PH
            d = Double.parseDouble(data.split(",")[6]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 4, d);
            changeLineChartSingleLowerLimit(1, 4, d);
            // 渲染数据
            dyPH.setYAxis(mutilChartMax[4], mutilChartMin[4], mutilChartLabelCount[4]);
            dyPH.addEntry(d);

            // ORP
            d = Double.parseDouble(data.split(",")[7]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 5, d);
            changeLineChartSingleLowerLimit(1, 5, d);
            // 渲染数据
            dyORP.setYAxis(mutilChartMax[5], mutilChartMin[5], mutilChartLabelCount[5]);
            dyORP.addEntry(d);

            // 溶解氧
            d = Double.parseDouble(data.split(",")[8]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 6, d);
            changeLineChartSingleLowerLimit(1, 6, d);
            // 渲染数据
            dyDO.setYAxis(mutilChartMax[6], mutilChartMin[6], mutilChartLabelCount[6]);
            dyDO.addEntry(d);

            // NHN
            d = Double.parseDouble(data.split(",")[9]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 7, d);
            changeLineChartSingleLowerLimit(1, 7, d);
            // 渲染数据
            dyNH.setYAxis(mutilChartMax[7], mutilChartMin[7], mutilChartLabelCount[7]);
            dyNH.addEntry(d);

            // 浊度
            d = Double.parseDouble(data.split(",")[10]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(1, 8, d);
            changeLineChartSingleLowerLimit(1, 8, d);
            // 渲染数据
            dyNT.setYAxis(mutilChartMax[8], mutilChartMin[8], mutilChartLabelCount[8]);
            dyNT.addEntry(d);

            // 3、数据存到数据数组中
            historyData.add(data);

            // 4、渲染所有记录和有效记录内容
            validData[16] ++;
            dataValidMutil.setText(getString(R.string.data_total_effective)  + validData[16] + getString(R.string.data_number));
            // 数据总数的计数
            dataNumber ++;
            dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
        }

    }

    // 隐藏标签
    public void hideAttributeTag() {
        singleLayout.setVisibility(View.GONE); // 单参数
        singleLayout1.setVisibility(View.GONE); // 单参数第一行
        singleEC1.setVisibility(View.GONE);
        singleEC1Temp.setVisibility(View.GONE);
        singleEC2.setVisibility(View.GONE);
        singleEC2Temp.setVisibility(View.GONE);
        singlePH.setVisibility(View.GONE);
        singlePHTemp.setVisibility(View.GONE);

        singleLayout2.setVisibility(View.GONE); // 单参数第二行
        singleORP.setVisibility(View.GONE);
        singleRDO.setVisibility(View.GONE);
        singleRDOTemp.setVisibility(View.GONE);
        singleNHN.setVisibility(View.GONE);
        singleNHNTemp.setVisibility(View.GONE);
        singleZS.setVisibility(View.GONE);
        singleZSTemp.setVisibility(View.GONE);

        singleLayout3.setVisibility(View.GONE); // 单参数第三行
        singleCOD.setVisibility(View.GONE);
        singleCODTemp.setVisibility(View.GONE);
        singleCODZS.setVisibility(View.GONE);
        singleCODBOD.setVisibility(View.GONE);
        singleCH.setVisibility(View.GONE);
        singleCHTemp.setVisibility(View.GONE);

        singleLayout4.setVisibility(View.GONE); // 单参数第四行
        singleRC.setVisibility(View.GONE);
        singleRCTemp.setVisibility(View.GONE);
        singleCY.setVisibility(View.GONE);
        singleCYTemp.setVisibility(View.GONE);
        singleTSS.setVisibility(View.GONE);
        singleTSSTemp.setVisibility(View.GONE);

        singleLayout5.setVisibility(View.GONE); // 单参数第五行
        singleTSM.setVisibility(View.GONE);
        singleTSMTemp.setVisibility(View.GONE);
        singleOW.setVisibility(View.GONE);
        singleOWTemp.setVisibility(View.GONE);
        singleSAL.setVisibility(View.GONE);
        singleSALTemp.setVisibility(View.GONE);

        mutilLayout.setVisibility(View.GONE); // 多参数
        mutilLayout1.setVisibility(View.GONE); // 多参数第一行
        mutilTemp.setVisibility(View.GONE);
        mutilCOD.setVisibility(View.GONE);
        mutilCODZS.setVisibility(View.GONE);
        mutilECSAL.setVisibility(View.GONE);

        mutilLayout2.setVisibility(View.GONE); // 多参数第二行
        mutilPH.setVisibility(View.GONE);
        mutilORP.setVisibility(View.GONE);
        mutilRDO.setVisibility(View.GONE);
        mutilNHN.setVisibility(View.GONE);
        mutilZS.setVisibility(View.GONE);

        dataValidEC1.setVisibility(View.GONE);
        dataValidEC2.setVisibility(View.GONE);
        dataValidPH.setVisibility(View.GONE);
        dataValidORP.setVisibility(View.GONE);
        dataValidRDO.setVisibility(View.GONE);
        dataValidNHN.setVisibility(View.GONE);
        dataValidZS.setVisibility(View.GONE);
        dataValidSAL.setVisibility(View.GONE);
        dataValidCOD.setVisibility(View.GONE);
        dataValidRC.setVisibility(View.GONE);
        dataValidCH.setVisibility(View.GONE);
        dataValidCY.setVisibility(View.GONE);
        dataValidTSS.setVisibility(View.GONE);
        dataValidTSM.setVisibility(View.GONE);
        dataValidOW.setVisibility(View.GONE);
        dataValidMutil.setVisibility(View.GONE);
    }

    // 隐藏所有的温度标签
    public void hideAllTempTag() {
        singleEC1Temp.setVisibility(View.GONE);
        singleEC2Temp.setVisibility(View.GONE);
        singlePHTemp.setVisibility(View.GONE);
        singleRDOTemp.setVisibility(View.GONE);
        singleNHNTemp.setVisibility(View.GONE);
        singleZSTemp.setVisibility(View.GONE);
        singleSALTemp.setVisibility(View.GONE);
        singleCODTemp.setVisibility(View.GONE);
        singleRCTemp.setVisibility(View.GONE);
        singleCHTemp.setVisibility(View.GONE);
        singleCYTemp.setVisibility(View.GONE);
        singleTSSTemp.setVisibility(View.GONE);
        singleTSMTemp.setVisibility(View.GONE);
        singleOWTemp.setVisibility(View.GONE);

    }

    // 按序号显示属性标签
    public void displayAttributeTag(int n) {
        if(n == 1) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singleEC1.setVisibility(View.VISIBLE);
//            singleEC1Temp.setVisibility(View.VISIBLE);
        }else if(n == 2) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singleEC2.setVisibility(View.VISIBLE);
//            singleEC2Temp.setVisibility(View.VISIBLE);
        }else if(n == 3) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singlePH.setVisibility(View.VISIBLE);
//            singlePHTemp.setVisibility(View.VISIBLE);
        }else if(n == 4) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singleORP.setVisibility(View.VISIBLE);
        }else if(n == 5) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singleRDO.setVisibility(View.VISIBLE);
//            singleRDOTemp.setVisibility(View.VISIBLE);
        }else if(n == 6) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout1.setVisibility(View.VISIBLE); // 单参数第一行
            singleNHN.setVisibility(View.VISIBLE);
//            singleNHNTemp.setVisibility(View.VISIBLE);
        }else if(n == 7) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout2.setVisibility(View.VISIBLE); // 单参数第二行
            singleZS.setVisibility(View.VISIBLE);
//            singleZSTemp.setVisibility(View.VISIBLE);
        }else if(n == 8) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout2.setVisibility(View.VISIBLE); // 单参数第二行
            singleSAL.setVisibility(View.VISIBLE);
//            singleSALTemp.setVisibility(View.VISIBLE);
        }else if(n == 9) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout2.setVisibility(View.VISIBLE); // 单参数第二行
            singleCOD.setVisibility(View.VISIBLE);
//            singleCODTemp.setVisibility(View.VISIBLE);
            singleCODZS.setVisibility(View.VISIBLE);
            singleCODBOD.setVisibility(View.VISIBLE);
        }else if(n == 10) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout2.setVisibility(View.VISIBLE); // 单参数第二行
            singleRC.setVisibility(View.VISIBLE);
//            singleRCTemp.setVisibility(View.VISIBLE);
        }else if(n == 11) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout3.setVisibility(View.VISIBLE); // 单参数第三行
            singleCH.setVisibility(View.VISIBLE);
//            singleCHTemp.setVisibility(View.VISIBLE);
        }else if(n == 12) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout3.setVisibility(View.VISIBLE); // 单参数第三行
            singleCY.setVisibility(View.VISIBLE);
//            singleCYTemp.setVisibility(View.VISIBLE);
        }else if(n == 13) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout3.setVisibility(View.VISIBLE); // 单参数第三行
            singleTSS.setVisibility(View.VISIBLE);
//            singleTSSTemp.setVisibility(View.VISIBLE);
        }else if(n == 14) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout3.setVisibility(View.VISIBLE); // 单参数第三行
            singleTSM.setVisibility(View.VISIBLE);
//            singleTSMTemp.setVisibility(View.VISIBLE);
        }else if(n == 15) {
            singleLayout.setVisibility(View.VISIBLE); // 单参数
            singleLayout3.setVisibility(View.VISIBLE); // 单参数第三行
            singleOW.setVisibility(View.VISIBLE);
//            singleOWTemp.setVisibility(View.VISIBLE);
        }else if(n == 999) {
            mutilLayout.setVisibility(View.VISIBLE); // 多参数
            mutilLayout1.setVisibility(View.VISIBLE); // 多参数第一行
            mutilTemp.setVisibility(View.VISIBLE);
            mutilCOD.setVisibility(View.VISIBLE);
            mutilCODZS.setVisibility(View.VISIBLE);
            mutilECSAL.setVisibility(View.VISIBLE);

            mutilLayout2.setVisibility(View.VISIBLE); // 多参数第二行
            mutilPH.setVisibility(View.VISIBLE);
            mutilORP.setVisibility(View.VISIBLE);
            mutilRDO.setVisibility(View.VISIBLE);
            mutilNHN.setVisibility(View.VISIBLE);
            mutilZS.setVisibility(View.VISIBLE);
        }

    }

    // 隐藏所有的有效数据文字
    public void hideValidDataBox() {
        dataValid.setVisibility(View.GONE);
        dataValidEC1.setVisibility(View.GONE);
        dataValidEC2.setVisibility(View.GONE);
        dataValidPH.setVisibility(View.GONE);
        dataValidORP.setVisibility(View.GONE);
        dataValidRDO.setVisibility(View.GONE);
        dataValidNHN.setVisibility(View.GONE);
        dataValidZS.setVisibility(View.GONE);
        dataValidSAL.setVisibility(View.GONE);
        dataValidCOD.setVisibility(View.GONE);
        dataValidRC.setVisibility(View.GONE);
        dataValidCH.setVisibility(View.GONE);
        dataValidCY.setVisibility(View.GONE);
        dataValidTSS.setVisibility(View.GONE);
        dataValidTSM.setVisibility(View.GONE);
        dataValidOW.setVisibility(View.GONE);
        dataValidMutil.setVisibility(View.GONE);
    }

    // 隐藏所有的折线图
    public void hideLineChart() {
        lineChart.setVisibility(View.GONE);
        // 单参数
        lineChartSingleEC1.setVisibility(View.GONE);
        lineChartSingleEC1Temp.setVisibility(View.GONE);
        lineChartSingleEC2.setVisibility(View.GONE);
        lineChartSingleEC2Temp.setVisibility(View.GONE);
        lineChartSinglePH.setVisibility(View.GONE);
        lineChartSinglePHTemp.setVisibility(View.GONE);
        lineChartSingleORP.setVisibility(View.GONE);
        lineChartSingleRDO.setVisibility(View.GONE);
        lineChartSingleRDOTemp.setVisibility(View.GONE);
        lineChartSingleNHN.setVisibility(View.GONE);
        lineChartSingleNHNTemp.setVisibility(View.GONE);
        lineChartSingleZS.setVisibility(View.GONE);
        lineChartSingleZSTemp.setVisibility(View.GONE);
        lineChartSingleSAL.setVisibility(View.GONE);
        lineChartSingleSALTemp.setVisibility(View.GONE);
        lineChartSingleCOD.setVisibility(View.GONE);
        lineChartSingleCODTemp.setVisibility(View.GONE);
        lineChartSingleCODZS.setVisibility(View.GONE);
        lineChartSingleCODBOD.setVisibility(View.GONE);
        lineChartSingleRC.setVisibility(View.GONE);
        lineChartSingleRCTemp.setVisibility(View.GONE);
        lineChartSingleCH.setVisibility(View.GONE);
        lineChartSingleCHTemp.setVisibility(View.GONE);
        lineChartSingleCY.setVisibility(View.GONE);
        lineChartSingleCYTemp.setVisibility(View.GONE);
        lineChartSingleTSS.setVisibility(View.GONE);
        lineChartSingleTSSTemp.setVisibility(View.GONE);
        lineChartSingleTSM.setVisibility(View.GONE);
        lineChartSingleTSMTemp.setVisibility(View.GONE);
        lineChartSingleOW.setVisibility(View.GONE);
        lineChartSingleOWTemp.setVisibility(View.GONE);

        // 多参数
        lineChart0.setVisibility(View.GONE);
        lineChartCOD.setVisibility(View.GONE); // COD折线图
        lineChartCODNT.setVisibility(View.GONE); // NT(COD)折线图
        lineChartEC.setVisibility(View.GONE); // EC折线图
        lineChartPH.setVisibility(View.GONE); // PH折线图
        lineChartORP.setVisibility(View.GONE); // ORP折线图
        lineChartDO.setVisibility(View.GONE); // DO折线图
        lineChartNH.setVisibility(View.GONE); // NH折线图
        lineChartNT.setVisibility(View.GONE); // NT折线图

        lineChart0.setVisibility(View.GONE); // 温度
    }

    // 所有的属性文字都变成普通的样式
    public void displayAttributeToCommon() {
        singleEC1.setTextColor(getResources().getColor(R.color.textview_common));
        singleEC1Temp.setTextColor(getResources().getColor(R.color.textview_common));
        singleEC2.setTextColor(getResources().getColor(R.color.textview_common));
        singleEC2Temp.setTextColor(getResources().getColor(R.color.textview_common));
        singlePH.setTextColor(getResources().getColor(R.color.textview_common));
        singlePHTemp.setTextColor(getResources().getColor(R.color.textview_common));

        singleORP.setTextColor(getResources().getColor(R.color.textview_common));
        singleRDO.setTextColor(getResources().getColor(R.color.textview_common));
        singleRDOTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleNHN.setTextColor(getResources().getColor(R.color.textview_common));
        singleNHNTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleZS.setTextColor(getResources().getColor(R.color.textview_common));
        singleZSTemp.setTextColor(getResources().getColor(R.color.textview_common));

        singleCOD.setTextColor(getResources().getColor(R.color.textview_common));
        singleCODTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleCODZS.setTextColor(getResources().getColor(R.color.textview_common));
        singleCODBOD.setTextColor(getResources().getColor(R.color.textview_common));
        singleCH.setTextColor(getResources().getColor(R.color.textview_common));
        singleCHTemp.setTextColor(getResources().getColor(R.color.textview_common));

        singleRC.setTextColor(getResources().getColor(R.color.textview_common));
        singleRCTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleCY.setTextColor(getResources().getColor(R.color.textview_common));
        singleCYTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleTSS.setTextColor(getResources().getColor(R.color.textview_common));
        singleTSSTemp.setTextColor(getResources().getColor(R.color.textview_common));

        singleTSM.setTextColor(getResources().getColor(R.color.textview_common));
        singleTSMTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleOW.setTextColor(getResources().getColor(R.color.textview_common));
        singleOWTemp.setTextColor(getResources().getColor(R.color.textview_common));
        singleSAL.setTextColor(getResources().getColor(R.color.textview_common));
        singleSALTemp.setTextColor(getResources().getColor(R.color.textview_common));

        mutilTemp.setTextColor(getResources().getColor(R.color.textview_common));
        mutilCOD.setTextColor(getResources().getColor(R.color.textview_common));
        mutilCODZS.setTextColor(getResources().getColor(R.color.textview_common));
        mutilECSAL.setTextColor(getResources().getColor(R.color.textview_common));

        mutilPH.setTextColor(getResources().getColor(R.color.textview_common));
        mutilORP.setTextColor(getResources().getColor(R.color.textview_common));
        mutilRDO.setTextColor(getResources().getColor(R.color.textview_common));
        mutilNHN.setTextColor(getResources().getColor(R.color.textview_common));
        mutilZS.setTextColor(getResources().getColor(R.color.textview_common));
    }

    // 对属性文字进行高亮显示
    public void displayAttributeToHighLight(TextView t) {
        t.setTextColor(getResources().getColor(R.color.textview_click));
    }

    // 改变单参数折线图的上限
    public void changeLineChartSingleUpperLimit(int type, int i, double d) {
        // 单参数
        if(type == 0) {
            if(d > singleChartMax[i]) {
                if(d > 10000) {
                    singleChartMax[i] = 20000;
                }else if(d > 5000) {
                    singleChartMax[i] = 10000;
                }else if(d > 3000) {
                    singleChartMax[i] = 5000;
                }else if(d > 2000) {
                    singleChartMax[i] = 3000;
                }else if(d > 1000) {
                    singleChartMax[i] = 2000;
                }else if(d > 500) {
                    singleChartMax[i] = 1000;
                }else if(d > 300) {
                    singleChartMax[i] = 500;
                }else if(d > 200) {
                    singleChartMax[i] = 300;
                }else if(d > 100) {
                    singleChartMax[i] = 200;
                }else if(d > 0) {
                    singleChartMax[i] = 100;
                }
            }
        }

        // 多参数
        if(type == 1) {
            if(d > mutilChartMax[i]) {
                if(d > 10000) {
                    mutilChartMax[i] = 20000;
                }else if(d > 5000) {
                    mutilChartMax[i] = 10000;
                }else if(d > 3000) {
                    mutilChartMax[i] = 5000;
                }else if(d > 2000) {
                    mutilChartMax[i] = 3000;
                }else if(d > 1000) {
                    mutilChartMax[i] = 2000;
                }else if(d > 500) {
                    mutilChartMax[i] = 1000;
                }else if(d > 300) {
                    mutilChartMax[i] = 500;
                }else if(d > 200) {
                    mutilChartMax[i] = 300;
                }else if(d > 100) {
                    mutilChartMax[i] = 200;
                }else if(d > 0) {
                    mutilChartMax[i] = 100;
                }
            }
        }

    }

    // 改变单参数折线图下限
    public void changeLineChartSingleLowerLimit(int type, int i, double n) {
        // 单参数
        if(type == 0) {
            if(n < singleChartMin[i]) {
                if(n < -70) {
                    singleChartMin[i] = -100;
                }else if(n < -60) {
                    singleChartMin[i] = -70;
                }else if(n < -50) {
                    singleChartMin[i] = -60;
                }else if(n < -40) {
                    singleChartMin[i] = -50;
                }else if(n < -30) {
                    singleChartMin[i] = -40;
                }else if(n < -20) {
                    singleChartMin[i] = -30;
                }else if(n < -10) {
                    singleChartMin[i] = -20;
                }else if(n < 0) {
                    singleChartMin[i] = -10;
                }else if(n < 10) {
                    singleChartMin[i] = 0;
                }
            }
        }

        // 多参数
        if(type == 1) {
            if(n < mutilChartMin[i]) {
                if(n < -70) {
                    mutilChartMin[i] = -100;
                }else if(n < -60) {
                    mutilChartMin[i] = -70;
                }else if(n < -50) {
                    mutilChartMin[i] = -60;
                }else if(n < -40) {
                    mutilChartMin[i] = -50;
                }else if(n < -30) {
                    mutilChartMin[i] = -40;
                }else if(n < -20) {
                    mutilChartMin[i] = -30;
                }else if(n < -10) {
                    mutilChartMin[i] = -20;
                }else if(n < 0) {
                    mutilChartMin[i] = -10;
                }else if(n < 10) {
                    mutilChartMin[i] = 0;
                }
            }
        }
    }

    // 初始化折线图
    public void initDyLineChart() {
        // 单参数数据上下限：未连接，电导率1，电导率2，PH，ORP，溶解氧，氨氮，浊度，盐度，化学需氧量COD，余氯，叶绿素，蓝绿藻，
        // 透明度，悬浮物，水中油,浊度(COD),BOD

        int[] colors = {Color.RED, Color.LTGRAY, Color.YELLOW, Color.GRAY, Color.GREEN, Color.DKGRAY, Color.CYAN, Color.BLUE, Color.BLACK};

        dySingleEC1 = new DyLineChartUtils(lineChartSingleEC1, "", colors[0], this);
        dySingleEC1.setYAxis(singleChartMax[0], singleChartMin[0], singleChartLabelCount[0]);

        dySingleEC1Temp = new DyLineChartUtils(lineChartSingleEC1Temp, "", colors[1], this);
        dySingleEC1Temp.setYAxis(singleChartMax[1], singleChartMin[1], singleChartLabelCount[1]);

        dySingleEC2 = new DyLineChartUtils(lineChartSingleEC2, "", colors[2], this);
        dySingleEC2.setYAxis(singleChartMax[2], singleChartMin[2], singleChartLabelCount[2]);

        dySingleEC2Temp = new DyLineChartUtils(lineChartSingleEC2Temp, "", colors[3], this);
        dySingleEC2Temp.setYAxis(singleChartMax[3], singleChartMin[3], singleChartLabelCount[3]);

        dySinglePH = new DyLineChartUtils(lineChartSinglePH, "", colors[4], this);
        dySinglePH.setYAxis(singleChartMax[4], singleChartMin[4], singleChartLabelCount[4]);

        dySinglePHTemp = new DyLineChartUtils(lineChartSinglePHTemp, "", colors[5], this);
        dySinglePHTemp.setYAxis(singleChartMax[5], singleChartMin[5], singleChartLabelCount[5]);

        dySingleORP = new DyLineChartUtils(lineChartSingleORP, "", colors[6], this);
        dySingleORP.setYAxis(singleChartMax[6], singleChartMin[6], singleChartLabelCount[6]);

        dySingleRDO = new DyLineChartUtils(lineChartSingleRDO, "", colors[7], this);
        dySingleRDO.setYAxis(singleChartMax[7], singleChartMin[7], singleChartLabelCount[7]);

        dySingleRDOTemp = new DyLineChartUtils(lineChartSingleRDOTemp, "", colors[8], this);
        dySingleRDOTemp.setYAxis(singleChartMax[8], singleChartMin[8], singleChartLabelCount[8]);

        dySingleNHN = new DyLineChartUtils(lineChartSingleNHN, "", colors[0], this);
        dySingleNHN.setYAxis(singleChartMax[9], singleChartMin[9], singleChartLabelCount[9]);

        dySingleNHNTemp = new DyLineChartUtils(lineChartSingleNHNTemp, "", colors[1], this);
        dySingleNHNTemp.setYAxis(singleChartMax[10], singleChartMin[10], singleChartLabelCount[10]);

        dySingleZS = new DyLineChartUtils(lineChartSingleZS, "", colors[2], this);
        dySingleZS.setYAxis(singleChartMax[11], singleChartMin[11], singleChartLabelCount[11]);

        dySingleZSTemp = new DyLineChartUtils(lineChartSingleZSTemp, "", colors[3], this);
        dySingleZSTemp.setYAxis(singleChartMax[12], singleChartMin[12], singleChartLabelCount[12]);

        dySingleSAL = new DyLineChartUtils(lineChartSingleSAL, "" , colors[4], this);
        dySingleSAL.setYAxis(singleChartMax[13], singleChartMin[13], singleChartLabelCount[13]);

        dySingleSALTemp = new DyLineChartUtils(lineChartSingleSALTemp, "" , colors[5], this);
        dySingleSALTemp.setYAxis(singleChartMax[14], singleChartMin[14], singleChartLabelCount[14]);

        dySingleCOD = new DyLineChartUtils(lineChartSingleCOD, "", colors[6], this);
        dySingleCOD.setYAxis(singleChartMax[15], singleChartMin[15], singleChartLabelCount[15]);

        dySingleCODTemp = new DyLineChartUtils(lineChartSingleCODTemp, "", colors[7], this);
        dySingleCODTemp.setYAxis(singleChartMax[16], singleChartMin[16], singleChartLabelCount[16]);

        dySingleCODZS = new DyLineChartUtils(lineChartSingleCODZS, "", colors[8], this);
        dySingleCODZS.setYAxis(singleChartMax[17], singleChartMin[17], singleChartLabelCount[17]);

        dySingleCODBOD = new DyLineChartUtils(lineChartSingleCODBOD, "", colors[0], this);
        dySingleCODBOD.setYAxis(singleChartMax[18], singleChartMin[18], singleChartLabelCount[18]);

        dySingleRC = new DyLineChartUtils(lineChartSingleRC, "", colors[1], this);
        dySingleRC.setYAxis(singleChartMax[19], singleChartMin[19], singleChartLabelCount[19]);

        dySingleRCTemp = new DyLineChartUtils(lineChartSingleRCTemp, "", colors[2], this);
        dySingleRCTemp.setYAxis(singleChartMax[20],singleChartMin[20], singleChartLabelCount[20]);

        dySingleCH = new DyLineChartUtils(lineChartSingleCH, "", colors[3], this);
        dySingleCH.setYAxis(singleChartMax[21], singleChartMin[21], singleChartLabelCount[21]);

        dySingleCHTemp = new DyLineChartUtils(lineChartSingleCHTemp, "", colors[4], this);
        dySingleCHTemp.setYAxis(singleChartMax[22], singleChartMin[22], singleChartLabelCount[22]);

        dySingleCY = new DyLineChartUtils(lineChartSingleCY, "", colors[5], this);
        dySingleCY.setYAxis(singleChartMax[23], singleChartMin[23], singleChartLabelCount[23]);

        dySingleCYTemp = new DyLineChartUtils(lineChartSingleCYTemp, "", colors[6], this);
        dySingleCYTemp.setYAxis(singleChartMax[24], singleChartMin[24], singleChartLabelCount[24]);

        dySingleTSS = new DyLineChartUtils(lineChartSingleTSS, "", colors[7], this);
        dySingleTSS.setYAxis(singleChartMax[25], singleChartMin[25], singleChartLabelCount[25]);

        dySingleTSSTemp = new DyLineChartUtils(lineChartSingleTSSTemp, "", colors[8], this);
        dySingleTSSTemp.setYAxis(singleChartMax[26], singleChartMin[26], singleChartLabelCount[26]);

        dySingleTSM = new DyLineChartUtils(lineChartSingleTSM, "", colors[8], this);
        dySingleTSM.setYAxis(singleChartMax[27], singleChartMin[27], singleChartLabelCount[27]);

        dySingleTSMTemp = new DyLineChartUtils(lineChartSingleTSMTemp, "", colors[0], this);
        dySingleTSMTemp.setYAxis(singleChartMax[28], singleChartMin[28], singleChartLabelCount[28]);

        dySingleOW = new DyLineChartUtils(lineChartSingleOW, "", colors[1], this);
        dySingleOW.setYAxis(singleChartMax[29], singleChartMin[29], singleChartLabelCount[29]);

        dySingleOWTemp = new DyLineChartUtils(lineChartSingleOWTemp, "", colors[2], this);
        dySingleOWTemp.setYAxis(singleChartMax[30], singleChartMin[30], singleChartLabelCount[30]);



        // 单参数
//        for(int i = 0; i < dySingles.length; i ++) {
//            dySingles[i] = new DyLineChartUtils(lcSingles[i], "", colors[i % colors.length],this);
//            dySingles[i].setYAxis(singleChartMax[i], singleChartMin[i], singleChartLabelCount[i]);
//            System.out.println("单参数折线图初始化" + i);
//        }

        // 多参数
//        for(int i = 0; i < dys.length; i ++) {
//            dys[i] = new DyLineChartUtils(lcMutils[i], "", colors[i % colors.length],this);
//            dys[i].setYAxis(mutilChartMax[i], mutilChartMin[i], mutilChartLabelCount[i]);
//            System.out.println("多参数折线图初始化" + i);
//        }

        dy0 = new DyLineChartUtils(lineChart0, "", colors[3], this);
        dy0.setYAxis(mutilChartMax[0], mutilChartMin[0], mutilChartLabelCount[0]);

        dyCOD = new DyLineChartUtils(lineChartCOD, "", colors[4], this);
        dyCOD.setYAxis(mutilChartMax[1], mutilChartMin[1], mutilChartLabelCount[1]);

        dyCODNT = new DyLineChartUtils(lineChartCODNT, "", colors[5], this);
        dyCODNT.setYAxis(mutilChartMax[2], mutilChartMin[2], mutilChartLabelCount[2]);

        dyEC = new DyLineChartUtils(lineChartEC, "", colors[6], this);
        dyEC.setYAxis(mutilChartMax[3], mutilChartMin[3], mutilChartLabelCount[3]);

        dyPH = new DyLineChartUtils(lineChartPH, "", colors[7], this);
        dyPH.setYAxis(mutilChartMax[4], mutilChartMin[4], mutilChartLabelCount[4]);

        dyORP = new DyLineChartUtils(lineChartORP, "", colors[8], this);
        dyORP.setYAxis(mutilChartMax[5], mutilChartMin[5], mutilChartLabelCount[5]);

        dyDO = new DyLineChartUtils(lineChartDO, "", colors[0], this);
        dyDO.setYAxis(mutilChartMax[6], mutilChartMin[6], mutilChartLabelCount[6]);

        dyNH = new DyLineChartUtils(lineChartNH, "", colors[1], this);
        dyNH.setYAxis(mutilChartMax[7], mutilChartMin[7], mutilChartLabelCount[7]);

        dyNT = new DyLineChartUtils(lineChartNT, "", colors[2], this);
        dyNT.setYAxis(mutilChartMax[8], mutilChartMin[8], mutilChartLabelCount[8]);


    }

    // 读取历史数据
    public void getHistoryData(String hex) {
        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                // 记录每次收到的数据转换成需要的每条数据
//                String[] data = {""};
                // 记录一半数据的内容
                final String[] incompleteData = {""};

                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        HexUtil.hexStringToBytes(hex),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristicRead.getService().getUuid().toString(),
                                        characteristicRead.getUuid().toString(),
                                        new BleNotifyCallback() {

                                            @Override
                                            public void onNotifySuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //                                                                    System.out.println("notify success通知成功");

                                                        System.out.println("notify success通知成功");
//                                                                        dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
//                                                                        dataValid.setText(getString(R.string.data_total_effective)  + dataValidNumber + getString(R.string.data_number));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        System.out.println("实时数据指令写入失败：" + exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                    @Override
                                                    public void run() {

//                                                        System.out.println("历史数据函数");
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                                        String dateString = formatter.format(date);
//                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                        String s1 = HexUtil.byteToString(data);
                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                        // System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
                                                        System.out.println("时间：" + dateString + "，数值：" + s1);
                                                        // System.out.println("时间：" + dateString + "，非回调值：" + HexUtil.formatHexString(characteristicRead.getValue()));
//                                                        SensorData s = new SensorData();
//                                                        String[] str = s1.split(",");

                                                        // 首先获取现在连接的是什么类型的传感器，不同的传感器解析不同的数据，进行配对处理
//                                                                        int type = ((MyApplication) getApplication()).getBasicDeviceType(); // 传感器类型
//                                                                        int type0 = ((MyApplication) getApplication()).getBasicType(); // 设备类型
                                                        // 首先获取的数据包含多个[],可能只包含其中一个[或]
                                                        char[] ch = s1.toCharArray();
                                                        String data = ""; // 数据存储
                                                        boolean isSave = false; // 是否开始存储数据
                                                        int realLocation = 0;
                                                        List<String> historyData = new ArrayList<String>();

                                                        // 分析数据，全存到historyData中
                                                        for(int i = 0; i < ch.length; i ++) {
                                                            if(ch[i] == ']') {
                                                                // 开始结束数据
                                                                if(!isSave) { // 表示上次有未完成的数据
                                                                    String supplementData = incompleteData[0] + s1.substring(0, i);
                                                                    historyData.add(supplementData);
                                                                }else{
                                                                    historyData.add(data);
                                                                    data = "";
                                                                }
                                                                isSave = false;
                                                            }
                                                            if(isSave) {
                                                                data = data + ch[i];
                                                            }
                                                            if(ch[i] == '[') {
                                                                // 开始记录数据
                                                                isSave = true;
                                                                realLocation = i;
                                                            }
                                                        }
                                                        // 判断是否有不完全的数据
                                                        if(isSave) {
                                                            incompleteData[0] = s1.substring(realLocation + 1, s1.length());
                                                        }
//                                                        for(int x = 1 ; x < 10; x ++) {
//                                                            for(int i = 1; i <= 16; i ++) {
//                                                                if(i == 4) {
//                                                                    historyData.add("0," + i + ",43.5,");
//                                                                }else if(i == 9) {
//                                                                    historyData.add("0," + i + ",3.5,24.5,123,45.6,");
//                                                                }else if(i == 16) {
//                                                                    // 设备类型，传感器类型，温度，COD，COD内置浊度，电导率/盐度，PH，ORP，溶解氧，NHN，浊度
//                                                                    historyData.add("1,0,24.5,17.5,123,45.6,7.41,13.4,45.6,146,23,");
//                                                                }else {
//                                                                    historyData.add("0," + i + ",6.86,24.5,");
//                                                                }
//                                                            }
//                                                        }

                                                        // 开始分发数据
                                                        for(int i = 0; i < historyData.size(); i ++) {
//                                                            s1:0,8,18.6,数值
//                                                            System.out.println("解析后的数据：" + historyData.get(i));
                                                            analysisHistoryData(historyData.get(i), historyData.get(i).split(","));

                                                        }
                                                        historyData.clear();

                                                    }

                                                });
                                            }
                                        });

                                // 获取数据和存储数据

                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("写入指令失败：" + exception);
                            }
                        });

            }


        },1000);
    }

    public void analysisHistoryData(String s1, String[] str) {
        int type0 = -1;
        int type1 = -1;
        if(str.length >= 2) {
            String digits = str[0].replaceAll("[^0-9]", "");
            String digits1 = str[1].replaceAll("[^0-9]", "");

            if (str[0].length() != 0 && str[1].length() != 0 && str[0].length() == digits.length() && str[1].length() == digits1.length()) {
                type0 = Integer.parseInt(str[0]);
                type1 = Integer.parseInt(str[1]);
            }
        }

        // 电导率1
        if(type0 == 0 && type1 == 1) {
            System.out.println("此时连接的是电导率1传感器");
            displayHistoryDataEC1(s1);
        }
        // 电导率2
        if(type0 == 0 && type1 == 2) {
            System.out.println("此时连接的是电导率2传感器");
            displayHistoryDataEC2(s1);
        }
        // PH
        if(type0 == 0 && type1 == 3) {
            System.out.println("此时连接的是PH传感器");
            displayHistoryDataPH(s1);

        }
        // ORP 氧化还原电位
        if(type0 == 0 && type1 == 4) {
            System.out.println("此时连接的是ORP传感器");
            displayHistoryDataORP(s1);

        }
        // RDO 溶解氧
        if(type0 == 0 && type1 == 5) {
            System.out.println("此时连接的是RDO传感器");
            displayHistoryDataRDO(s1);

        }
        // NHN 氨氮
        if(type0 == 0 && type1 == 6) {
            System.out.println("此时连接的是NHN传感器");
            displayHistoryDataNHN(s1);

        }
        // ZS 浊度
        if(type0 == 0 && type1 == 7) {
            System.out.println("此时连接的是浊度传感器");
            displayHistoryDataZS(s1);

        }
        // SAL 盐度
        if(type0 == 0 && type1 == 8) {
            System.out.println("此时连接的是盐度传感器");
            displayHistoryDataSAL(s1);

        }
        // COD 化学需氧量
        if(type0 == 0 && type1 == 9) {
            System.out.println("此时连接的是COD传感器");
            displayHistoryDataCOD(s1);

        }
        // RC 余氯
        if(type0 == 0 && type1 == 10) {
            System.out.println("此时连接的是余氯传感器");
            displayHistoryDataRC(s1);

        }
        // 叶绿素 CH
        if(type0 == 0 && type1 == 11) {
            System.out.println("此时连接的是叶绿素传感器");
            displayHistoryDataCH(s1);

        }
        // 蓝绿藻 CY
        if(type0 == 0 && type1 == 12) {
            System.out.println("此时连接的是蓝绿藻传感器");
            displayHistoryDataCY(s1);

        }
        // 透明度 TSS
        if(type0 == 0 && type1 == 13) {
            System.out.println("此时连接的是透明度传感器");
            displayHistoryDataTSS(s1);

        }
        // 悬浮物 TSM
        if(type0 == 0 && type1 == 14) {
            System.out.println("此时连接的是悬浮物传感器");
            displayHistoryDataTSM(s1);

        }
        // 水中油 OW
        if(type0 == 0 && type1 == 15) {
            System.out.println("此时连接的是水中油传感器");
            displayHistoryDataOW(s1);

        }
        // 多参数MUL
        if(type0 == 1) {
            System.out.println("此时连接的是多参数传感器");
            displayHistoryDataMUL(s1);

        }
    }

    public void getHistoryDataByF6(){
        final String hex = "f6";
        final boolean[] isNum = {false};
        final String[] historyDataNumber = {""};
        final int[] basicNum = {65536};
        final int[] lengthNum = {0};
        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {

                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        HexUtil.hexStringToBytes(hex),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                isF6Success = true;

                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristicRead.getService().getUuid().toString(),
                                        characteristicRead.getUuid().toString(),
                                        new BleNotifyCallback() {

                                            @Override
                                            public void onNotifySuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //                                                                    System.out.println("notify success通知成功");

                                                        System.out.println("读取f6历史数据数量长度");
//                                                                        dataTotal.setText(getString(R.string.data_total) + dataNumber + getString(R.string.data_number));
//                                                                        dataValid.setText(getString(R.string.data_total_effective)  + dataValidNumber + getString(R.string.data_number));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        System.out.println("f6指令写入失败：" + exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        System.out.println("历史数据数量函数");
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
                                                        System.out.println("未转换的数值：" + characteristicRead.getValue());
                                                        System.out.println("未转换的回调数值：" + HexUtil.byteToString(data));
//                                                        String content = "";
                                                        handler.removeCallbacks(runnable);
//                                                            System.out.println(Byte.MAX_VALUE + "," + Byte.MIN_VALUE);
                                                        // 解码转长度
//                                                            for(int b : characteristicRead.getValue()) {
                                                        for(int b : data) {
//                                                                String s = b.toString();
                                                            System.out.println("未转换的数组：" + b + ",字符：");
                                                            if(b == 93) {
                                                                isNum[0] = false;
                                                            }
                                                            if(isNum[0]) {
                                                                if(b < 0) {
                                                                    b = 256 + b;
                                                                }
                                                                historyDataNumber[0] = historyDataNumber[0] + b;
                                                                lengthNum[0] = lengthNum[0] + b * basicNum[0]; // 长度
                                                                basicNum[0] = basicNum[0] / 256;
                                                            }
                                                            if(b == 91) {
                                                                isNum[0] = true;
                                                                basicNum[0] = 0;
                                                                historyDataNumber[0] = "";
                                                                basicNum[0] = 65536;
                                                                lengthNum[0] = 0;
                                                            }
                                                            if(b == 44) {
                                                                System.out.println("数据出错了，中间有逗号");
                                                                isNum[0] = false;
                                                                basicNum[0] = 0;
                                                                historyDataNumber[0] = "";
                                                                basicNum[0] = 65536;
                                                                lengthNum[0] = 0;
                                                                break;
                                                            }

                                                        }
                                                        System.out.println("字符串:" + historyDataNumber[0] + "，数：" + lengthNum[0]);

//                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                        String s1 = HexUtil.byteToString(data);
                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                        System.out.println("回调值：" + HexUtil.byteToString(data));
                                                        System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
//                                                            SensorData s = new SensorData();
//                                                            String[] str = s1.split(",");
                                                        String time = "";
                                                        int n = lengthNum[0];
                                                        if(n > 180000) {
                                                            time = time + (int)(n/180000) + getString(R.string.hour);
                                                            n = n % 180000;
                                                        }
                                                        if(n > 3000) {
                                                            time = time + (int)(n / 3000) + getString(R.string.minute);
                                                            n = n % 3000;
                                                        }
                                                        if(n >= 50){
                                                            int i = n / 50;
                                                            time = time + i;
                                                            n = n % 50;
                                                        }
                                                        if(n >= 0) {
                                                            if(time.length() == 0) {
                                                                time = "0";
                                                            }
                                                            n = n * 2;
                                                            if(n < 10) {
                                                                time = time + ".0" + n + getString(R.string.seconds);
                                                            }else {

                                                                time = time + "." + n + getString(R.string.seconds);
                                                            }
                                                        }
                                                        System.out.println(lengthNum[0] + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
                                                        if(lengthNum[0] >= 1) {
                                                            System.out.println("duiduiudiduiduiduddududuiduiduiduiduduu");
                                                            new AlertDialog.Builder(historyLayout.getContext()).setMessage(getString(R.string.history_data_number_about) + lengthNum[0] + getString(R.string.estimated_read_time) + time + getString(R.string.read_now)).setNegativeButton(getString(R.string.ensure),
                                                                    new DialogInterface.OnClickListener(){

                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            progressDialogReal = new ProgressDialog(historyLayout.getContext());
                                                                            progressDialogReal.setIcon(R.mipmap.ic_launcher);
                                                                            progressDialogReal.setTitle(getString(R.string.history_data_read));
                                                                            progressDialogReal.setMessage(getString(R.string.start_read));
                                                                            progressDialogReal.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                                                                            progressDialogReal.setCancelable(true);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                                                                            progressDialogReal.show();
                                                                            new Handler().postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    progressDialogReal.setMessage(getString(R.string.reading));
                                                                                    System.out.println("开始读取历史数据");
                                                                                }
                                                                            },500);
                                                                            new Handler().postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    progressDialogReal.dismiss();
                                                                                    System.out.println("开始读取历史数据");
                                                                                    // 获取所有的历史数据
                                                                                    final String hex = "fb";

                                                                                    getHistoryData(hex);
                                                                                }
                                                                            },1500);
                                                                        }
                                                                    }).setPositiveButton(getString(R.string.cancel),null).show();
                                                        }
//                                                        else if(lengthNum[0] > 0) {
//                                                            System.out.println("#################################数据量小，直接读取历史数据###############################");
//                                                            // 获取所有的历史数据
//                                                            final String hex = "fb";
//
//                                                            getHistoryData(hex);
//                                                        }

                                                    }

                                                });
                                            }
                                        });

                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("写入指令失败：" + exception);
                            }
                        });

            }


        },1000);
    }

    private void openAssignFolder(String figurepath) {
//        File sdDir;
//        boolean sdCardExist = Environment.getExternalStorageState().equals(
//                Environment.MEDIA_MOUNTED);
//        if (sdCardExist){
//            if (Build.VERSION.SDK_INT >= 29){
//                sdDir = getExternalFilesDir(null);
//            }else {
//                sdDir = Environment.getExternalStorageDirectory();
//            }
//        } else {
//            sdDir = Environment.getRootDirectory();
//        }
//        figurepath = sdDir.getAbsolutePath();
//        figurepath = "/M2102J2SC/蓝牙";
        System.out.println(figurepath);

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 跳转到最近
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setDataAndType(Uri.parse(figurepath), "*/*");
        startActivityForResult(intent,0);
    }

    public void initMutilSensorNameSet() {
        // 判断多参数是否进行了更改配置
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        // 如果进行过配置
        if(pref.getString("mutilIsSet", "").equals("true")) {
            String[] name = new String[]{getString(R.string.data_EC), getString(R.string.data_PH), getString(R.string.data_ORP_0),
                    getString(R.string.data_DO), getString(R.string.data_NH4), getString(R.string.data_ZS),
                    getString(R.string.data_salinity), getString(R.string.data_residual_chlorine), getString(R.string.data_Chlorophyl),
                    getString(R.string.data_blue_green_algae), getString(R.string.data_Transparency), getString(R.string.data_Suspended_Solids),
                    getString(R.string.data_oil_in_water), getString(R.string.ununited)};

            if(pref.getString("mutilParameter1", "").equals("1")) {
                mutilCOD.setText(R.string.ununited);
                mutilCODZS.setText(R.string.ununited);
            } else {
                mutilCOD.setText(R.string.data_COD_0);
                mutilCODZS.setText(R.string.data_COD_ZS);
            }
            mutilECSAL.setText(name[Integer.parseInt(pref.getString("mutilParameter2", ""))]);
            mutilPH.setText(name[Integer.parseInt(pref.getString("mutilParameter3", ""))]);
            mutilORP.setText(name[Integer.parseInt(pref.getString("mutilParameter4", ""))]);
            mutilRDO.setText(name[Integer.parseInt(pref.getString("mutilParameter5", ""))]);
            mutilNHN.setText(name[Integer.parseInt(pref.getString("mutilParameter6", ""))]);
            mutilZS.setText(name[Integer.parseInt(pref.getString("mutilParameter7", ""))]);


        }



    }


}
