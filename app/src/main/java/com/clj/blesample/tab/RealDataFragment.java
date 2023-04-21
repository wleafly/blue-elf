package com.clj.blesample.tab;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProvider;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Base64;
import android.util.Log;
import android.view.DragAndDropPermissions;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.activity.DeviceOrderSet2Activity;
import com.clj.blesample.activity.TimeOrderSetActivity;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.operation.OperationActivity;
import com.clj.blesample.operation.ServiceListFragment;
import com.clj.blesample.utils.DyLineChartUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.MyLog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.onlynight.waveview.WaveView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class RealDataFragment extends Fragment {

    MainActivity act = (MainActivity) getActivity();

    private View view;
    private View v;

    private static final int REQUEST_CODE_OPEN_GPS = 1;//请求代码打开GPS
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2; //请求代码权限位置
    private BleDevice bleDevice;
    private BleManager bleManager;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;
    private int chipType = 0; // 1:安信可  2:BT02-E104

    // 获取全局类中的变量
    private Boolean globalDeviceIsConnect; // 设备是否连接
    private String globalDeviceName; // 全局的设备名称
    private int globalDeviceAddress; // 全局的设备地址
    private int globalDeviceType; // 全局的传感器类型
    private int globalType; // 全局设备类型
    private int globalIntervalTime; // 全局的间隔时间
    private int globalTestTime; // 全局的测试时间

    private DynamicLineChartManager dynamicLineChartManager2;
    private List<Float> list = new ArrayList<>(); //数据集合
    private List<String> names = new ArrayList<>(); //折线名字集合
    private List<Integer> colour = new ArrayList<>();//折线颜色集合

    private Boolean bo = true; // 是否读取到间隔时间和测试时间
    private Drawable drawable;
    private Drawable drawableForbidden;
    private Drawable drawableSensorTypeInit;
    private Drawable drawableSensorType;

    private int chartMax = 100;
    private int chartMin = 0;
    private int chartLabelCount = 8;
    private int chartUpperLimit = 0;
    private int chartLowLimit = 0;
    private int chartUpperLimit0 = 40;
    private int chartLowLimit0 = 10;
    private int chartMax0 = 40;
    private int chartMin0 = 10;
    private int[] lineChartMax = {40, 100, 100, 100, 14, 100, 20, 100, 100};
    private int[] lineChartMin = {10, 0, 0, 0, 0, 0, 0, 0, 0};
    private int[] lineChartLabelCount = {8, 8, 8, 8, 8, 8, 8, 8, 8};

    private LineChart lineChart; // 单参数的数值
    private LineChart lineChart0; // 单参数的温度
    private LineChart lineChartDemo; // 样例
    private DyLineChartUtils dy; // 单参数的数值
    private DyLineChartUtils dy0; // 单参数的温度
    private DyLineChartUtils dyDemo; // 样例

    // 多参数数值
    private DyLineChartUtils dyTEMP;
    private DyLineChartUtils dyCOD;
    private DyLineChartUtils dyCODNT;
    private DyLineChartUtils dyEC;
    private DyLineChartUtils dyPH;
    private DyLineChartUtils dyORP;
    private DyLineChartUtils dyDO;
    private DyLineChartUtils dyNH;
    private DyLineChartUtils dyNT;

    private DyLineChartUtils[] dys = {dyTEMP, dyCOD, dyCODNT, dyEC, dyPH, dyORP, dyDO, dyNH, dyNT};

    private int chartSpecialCODMax = 100;
    private int chartSpecialCODMin = 0;
    private int chartSpecialBODMax = 100;
    private int chartSpecialBODMin = 0;

    // 8个数据的上下限为主
    private double[] controlUpperAndLowerData = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 基本参数, type = 0
    private int CUALDSign = 0; // 基本参数对应的序号
    private double[] controlUpperAndLowerBOD = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // COD中的bod, type = 1
    private int CUALBSign = 0; // bod参数对应的序号
    private double[] controlUpperAndLowerZS = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // COD中的ZS, type = 2
    private int CUALZSign = 0; // bod参数对应的序号
    private double[] controlUpperAndLowerMutilCOD = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数COD, type = 3
    private int CUALMCODSign = 0; // 多参数COD对应的序号
    private double[] controlUpperAndLowerMutilCODNT = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数CODNT, type = 4
    private int CUALMCODNTSign = 0; // 多参数CODNT对应的序号
    private double[] controlUpperAndLowerMutilEC = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数EC, type = 5
    private int CUALMECSign = 0; // 多参数EC对应的序号
    private double[] controlUpperAndLowerMutilPH = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数PH, type = 6
    private int CUALMPHSign = 0; // 多参数PH对应的序号
    private double[] controlUpperAndLowerMutilORP = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数ORP, type = 7
    private int CUALMORPSign = 0; // 多参数ORP对应的序号
    private double[] controlUpperAndLowerMutilDO = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数DO, type = 8
    private int CUALMDOSign = 0; // 多参数DO对应的序号
    private double[] controlUpperAndLowerMutilNH = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数NH, type = 9
    private int CUALMNHSign = 0; // 多参数NH对应的序号
    private double[] controlUpperAndLowerMutilNT = {0, 0, 0, 0, 0, 0, 0, 0, 0}; // 多参数NT, type = 10
    private int CUALMNTSign = 0; // 多参数NT对应的序号

    private View real_data_view;
    private ImageView sensorType; // 传感器类型图片
    private TextView deviceName;  // 设备名称
    private TextView deviceAddress; // 设备地址
    private TextView deviceType; // 设备类型
    private Button btn; // 刷新按钮
    private TextView text1; // 测试数据
    private TextView text2; // 测试温度
    private TextView text3; // 间隔时间
    private TextView text4; // 测试时间
    private View basicData1; // 单参数数据框:数值，温度，时间，时间
    private View basicData2; // 多参数温度时间框
    private View proData; // 多参数属性数据框
    private View electricLayout; // 电量框(球形)
    private View electricLayout2; // 电量框(电池形状)
    private TextView linechartName; // 折线图名字
    private TextView electricData; // 电量数据(球形)
    private TextView electricData2; // 电量数据(电池形状)
    private LinearLayout electricBox; // 电量比例框(电池形状)

    private LineChart lineChartTEMP; // 温度折线图
    private LineChart lineChartCOD; // COD折线图
    private LineChart lineChartCODNT; // NT(COD)折线图
    private LineChart lineChartEC; // EC折线图
    private LineChart lineChartPH; // PH折线图
    private LineChart lineChartORP; // ORP折线图
    private LineChart lineChartDO; // DO折线图
    private LineChart lineChartNH; // NH折线图
    private LineChart lineChartNT; // NT折线图

    private LineChart[] lineCharts;

    private TextView pro01; // 多参数:温度
    private TextView pro02; // 多参数:COD
    private TextView pro03; // 多参数:COD 内置浊度
    private TextView pro04; // 多参数:电导率/盐度
    private TextView pro05; // 多参数:PH
    private TextView pro06; // 多参数:ORP
    private TextView pro07; // 多参数:溶解氧
    private TextView pro08; // 多参数:氨氮
    private TextView pro09; // 多参数:浊度
    private TextView pro10; // 多参数:间隔时间
    private TextView pro11; // 多参数:测试时间

    // 经纬度数值显示
    private View lngAndLat; // 经纬度view
    private TextView lngAndLatValue; // 经纬度数值

    private ProgressDialog progressDialogReal;
    private Boolean typeOrderIsSend = false;
    private Boolean realDataOrderIsSend = false;

    private Runnable runnable;
    private Handler handler;

    private Boolean isVisible;
    private int phaseRead = 0;
    private Boolean isGetData = false; // 是否正确开始读取实时数据
    private Boolean isRealData = false; // 是都是实时数据
    private String incompleteData = ""; // 不完全的实时数据

    private WaveView waveView;

    private Drawable drawableBlue;
    private Drawable drawableOrange;
    private Drawable drawableRed;

    // 重新规划的上下限问题
    // 单参数数据上下限：电导率1，温度，电导率2，温度，  PH，温度，ORP，溶解氧，温度，  氨氮，温度，浊度，温度，盐度，温度，
    // 化学需氧量COD，温度，浊度(COD),BOD,余氯，温度，  叶绿素，温度，蓝绿藻，温度，透明度，温度，  悬浮物，温度，水中油,温度
    private int[] singleChartMax = {100, 50, 100, 50, 14, 50, 100, 20, 50, 100, 50, 100, 50, 70, 50,
            100, 50, 100, 500, 10, 50, 400, 50, 300, 50, 100, 50, 100, 50, 40, 50};
    private int[] singleChartMin = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int[] singleChartLabelCount = {8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8};

    // 多参数数据上下限：温度，COD，浊度(COD),电导率/盐度,PH,ORP,溶解氧,NHN,浊度
    private int[] mutilChartMax = {50, 100, 100, 100, 14, 100, 20, 100, 100};
    private int[] mutilChartMin = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private int[] mutilChartLabelCount = {8, 8, 8, 8, 8, 8, 8, 8, 8};

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

    private View singleLayoutEC1;
    private TextView dataEC1;
    private TextView dataTempEC1;
    private TextView intervalTimeEC1;
    private TextView testTimeEC1;

    private View singleLayoutEC2;
    private TextView dataEC2;
    private TextView dataTempEC2;
    private TextView intervalTimeEC2;
    private TextView testTimeEC2;

    private View singleLayoutPH;
    private TextView dataPH;
    private TextView dataTempPH;
    private TextView intervalTimePH;
    private TextView testTimePH;

    private View singleLayoutORP;
    private TextView dataORP;
    private TextView intervalTimeORP;
    private TextView testTimeORP;

    private View singleLayoutRDO;
    private TextView dataRDO;
    private TextView dataTempRDO;
    private TextView intervalTimeRDO;
    private TextView testTimeRDO;

    private View singleLayoutNHN;
    private TextView dataNHN;
    private TextView dataTempNHN;
    private TextView intervalTimeNHN;
    private TextView testTimeNHN;

    private View singleLayoutZS;
    private TextView dataZS;
    private TextView dataTempZS;
    private TextView intervalTimeZS;
    private TextView testTimeZS;

    private View singleLayoutSAL;
    private TextView dataSAL;
    private TextView dataTempSAL;
    private TextView intervalTimeSAL;
    private TextView testTimeSAL;

    private View singleLayoutCOD;
    private TextView dataCOD;
    private TextView dataTempCOD;
    private TextView dataCODZS;
    private TextView dataCODBOD;
    private TextView intervalTimeCOD;
    private TextView testTimeCOD;

    private View singleLayoutRC;
    private TextView dataRC;
    private TextView dataTempRC;
    private TextView intervalTimeRC;
    private TextView testTimeRC;

    private View singleLayoutCH;
    private TextView dataCH;
    private TextView dataTempCH;
    private TextView intervalTimeCH;
    private TextView testTimeCH;

    private View singleLayoutCY;
    private TextView dataCY;
    private TextView dataTempCY;
    private TextView intervalTimeCY;
    private TextView testTimeCY;

    private View singleLayoutTSS;
    private TextView dataTSS;
    private TextView dataTempTSS;
    private TextView intervalTimeTSS;
    private TextView testTimeTSS;

    private View singleLayoutTSM;
    private TextView dataTSM;
    private TextView dataTempTSM;
    private TextView intervalTimeTSM;
    private TextView testTimeTSM;

    private View singleLayoutOW;
    private TextView dataOW;
    private TextView dataTempOW;
    private TextView intervalTimeOW;
    private TextView testTimeOW;

    private LineChart[] lcSingles = {lineChartSingleEC1, lineChartSingleEC1Temp, lineChartSingleEC2, lineChartSingleEC2Temp, lineChartSinglePH,
            lineChartSinglePHTemp, lineChartSingleORP, lineChartSingleRDO, lineChartSingleRDOTemp, lineChartSingleNHN,
            lineChartSingleNHNTemp, lineChartSingleZS, lineChartSingleZSTemp, lineChartSingleSAL, lineChartSingleSALTemp,
            lineChartSingleCOD, lineChartSingleCODTemp, lineChartSingleCODZS, lineChartSingleCODBOD, lineChartSingleRC,
            lineChartSingleRCTemp, lineChartSingleCH, lineChartSingleCHTemp, lineChartSingleCY, lineChartSingleCYTemp,
            lineChartSingleTSS, lineChartSingleTSSTemp, lineChartSingleTSM, lineChartSingleTSMTemp, lineChartSingleOW,
            lineChartSingleOWTemp};


    private LineChart[] lcMutils = {lineChart0, lineChartCOD, lineChartCODNT, lineChartEC, lineChartPH, lineChartORP, lineChartDO, lineChartNH, lineChartNT};


//    private DyLineChartUtils[] dys = {dy0, dyCOD, dyCODNT, dyEC, dyPH, dyORP, dyDO, dyNH, dyNT};


    // 实时数据下载序号标记：电导率us,电导率ms,ph,orp,溶解氧,氨氮,浊度,盐度,COD,余氯,叶绿素,蓝绿藻,透明度,悬浮物,水中油,多参数
    private int DDM1 = 1, DDM2 = 1, PHG = 1, ORP = 1, RDO = 1, NHN = 1, ZS = 1, DDMS = 1, COD = 1, CL = 1, CHLO = 1, BGA = 1, TPS = 1, TSS = 1, OIL = 1, MUTIL = 1;
    // 多参数配置名称和单位
    private int[] mutilSetUnit = {9, 7, 2, 3, 4, 5, 6, 7}; // 单位
    private String[] mutilSetName = {"", "", "", "", "", "", "", ""};
    // 刷新次数
    private int freshNum = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.real_time_data, null);
            v = view;


            System.out.println("RealDataFragment中的onCreateView()方法");
        }
        System.out.println("RealDataFragment中的onCreateView()方法");
        // 获取实时数据页面的控件
        real_data_view = v.findViewById(R.id.real_data_view);
//        showLineChart();
        lineChart = v.findViewById(R.id.dy_line_chart);
        lineChart0 = v.findViewById(R.id.dy_line_chart0);
        lineChartDemo = v.findViewById(R.id.dy_line_chart_demo);
        lineChartTEMP = v.findViewById(R.id.dy_line_chart_temp);
        lineChartCOD = v.findViewById(R.id.dy_line_chart_cod);
        lineChartCODNT = v.findViewById(R.id.dy_line_chart_cod_nt);
        lineChartEC = v.findViewById(R.id.dy_line_chart_ec);
        lineChartPH = v.findViewById(R.id.dy_line_chart_ph);
        lineChartORP = v.findViewById(R.id.dy_line_chart_orp);
        lineChartDO = v.findViewById(R.id.dy_line_chart_do);
        lineChartNH = v.findViewById(R.id.dy_line_chart_nh);
        lineChartNT = v.findViewById(R.id.dy_line_chart_nt);

        // 重新规划的单参数折线图
        lineChartSingleEC1 = v.findViewById(R.id.dy_line_chart_single_EC1);
        lineChartSingleEC1Temp = v.findViewById(R.id.dy_line_chart_single_EC1_temp);
        lineChartSingleEC2 = v.findViewById(R.id.dy_line_chart_single_EC2);
        lineChartSingleEC2Temp = v.findViewById(R.id.dy_line_chart_single_EC2_temp);
        lineChartSinglePH = v.findViewById(R.id.dy_line_chart_single_PH);
        lineChartSinglePHTemp = v.findViewById(R.id.dy_line_chart_single_PH_temp);
        lineChartSingleORP = v.findViewById(R.id.dy_line_chart_single_ORP);
        lineChartSingleRDO = v.findViewById(R.id.dy_line_chart_single_RDO);
        lineChartSingleRDOTemp = v.findViewById(R.id.dy_line_chart_single_RDO_temp);
        lineChartSingleNHN = v.findViewById(R.id.dy_line_chart_single_NHN);
        lineChartSingleNHNTemp = v.findViewById(R.id.dy_line_chart_single_NHN_temp);
        lineChartSingleZS = v.findViewById(R.id.dy_line_chart_single_ZS);
        lineChartSingleZSTemp = v.findViewById(R.id.dy_line_chart_single_ZS_temp);
        lineChartSingleSAL = v.findViewById(R.id.dy_line_chart_single_SAL);
        lineChartSingleSALTemp = v.findViewById(R.id.dy_line_chart_single_SAL_temp);
        lineChartSingleCOD = v.findViewById(R.id.dy_line_chart_single_COD);
        lineChartSingleCODTemp = v.findViewById(R.id.dy_line_chart_single_COD_temp);
        lineChartSingleCODZS = v.findViewById(R.id.dy_line_chart_single_COD_ZS);
        lineChartSingleCODBOD = v.findViewById(R.id.dy_line_chart_single_COD_BOD);
        lineChartSingleRC = v.findViewById(R.id.dy_line_chart_single_RC);
        lineChartSingleRCTemp = v.findViewById(R.id.dy_line_chart_single_RC_temp);
        lineChartSingleCH = v.findViewById(R.id.dy_line_chart_single_CH);
        lineChartSingleCHTemp = v.findViewById(R.id.dy_line_chart_single_CH_temp);
        lineChartSingleCY = v.findViewById(R.id.dy_line_chart_single_CY);
        lineChartSingleCYTemp = v.findViewById(R.id.dy_line_chart_single_CY_temp);
        lineChartSingleTSS = v.findViewById(R.id.dy_line_chart_single_TSS);
        lineChartSingleTSSTemp = v.findViewById(R.id.dy_line_chart_single_TSS_temp);
        lineChartSingleTSM = v.findViewById(R.id.dy_line_chart_single_TSM);
        lineChartSingleTSMTemp = v.findViewById(R.id.dy_line_chart_single_TSM_temp);
        lineChartSingleOW = v.findViewById(R.id.dy_line_chart_single_OW);
        lineChartSingleOWTemp = v.findViewById(R.id.dy_line_chart_single_OW_temp);

        singleLayoutEC1 = v.findViewById(R.id.single_layout_EC1);
        dataEC1 = v.findViewById(R.id.data_EC1);
        dataTempEC1 = v.findViewById(R.id.data_temp_EC1);
        intervalTimeEC1 = v.findViewById(R.id.interval_time_EC1);
        testTimeEC1 = v.findViewById(R.id.test_time_EC1);

        singleLayoutEC2 = v.findViewById(R.id.single_layout_EC2);
        dataEC2 = v.findViewById(R.id.data_EC2);
        dataTempEC2 = v.findViewById(R.id.data_temp_EC2);
        intervalTimeEC2 = v.findViewById(R.id.interval_time_EC2);
        testTimeEC2 = v.findViewById(R.id.test_time_EC2);

        singleLayoutPH = v.findViewById(R.id.single_layout_PH);
        dataPH = v.findViewById(R.id.data_PH);
        dataTempPH = v.findViewById(R.id.data_temp_PH);
        intervalTimePH = v.findViewById(R.id.interval_time_PH);
        testTimePH = v.findViewById(R.id.test_time_PH);

        singleLayoutORP = v.findViewById(R.id.special_ORP);
        dataORP = v.findViewById(R.id.orp_data);
        intervalTimeORP = v.findViewById(R.id.orp_interval);
        testTimeORP = v.findViewById(R.id.orp_test);

        singleLayoutRDO = v.findViewById(R.id.single_layout_RDO);
        dataRDO = v.findViewById(R.id.data_RDO);
        dataTempRDO = v.findViewById(R.id.data_temp_RDO);
        intervalTimeRDO = v.findViewById(R.id.interval_time_RDO);
        testTimeRDO = v.findViewById(R.id.test_time_RDO);

        singleLayoutNHN = v.findViewById(R.id.single_layout_NHN);
        dataNHN = v.findViewById(R.id.data_NHN);
        dataTempNHN = v.findViewById(R.id.data_temp_NHN);
        intervalTimeNHN = v.findViewById(R.id.interval_time_NHN);
        testTimeNHN = v.findViewById(R.id.test_time_NHN);

        singleLayoutZS = v.findViewById(R.id.single_layout_ZS);
        dataZS = v.findViewById(R.id.data_ZS);
        dataTempZS = v.findViewById(R.id.data_temp_ZS);
        intervalTimeZS = v.findViewById(R.id.interval_time_ZS);
        testTimeZS = v.findViewById(R.id.test_time_ZS);

        singleLayoutSAL = v.findViewById(R.id.single_layout_SAL);
        dataSAL = v.findViewById(R.id.data_SAL);
        dataTempSAL = v.findViewById(R.id.data_temp_SAL);
        intervalTimeSAL = v.findViewById(R.id.interval_time_SAL);
        testTimeSAL = v.findViewById(R.id.test_time_SAL);

        singleLayoutCOD = v.findViewById(R.id.special_data);
        dataCOD = v.findViewById(R.id.special1);
        dataTempCOD = v.findViewById(R.id.special_temp);
        dataCODZS = v.findViewById(R.id.special2);
        dataCODBOD = v.findViewById(R.id.special_BOD);
        intervalTimeCOD = v.findViewById(R.id.special_time1);
        testTimeCOD = v.findViewById(R.id.special_time2);

        singleLayoutRC = v.findViewById(R.id.single_layout_RC);
        dataRC = v.findViewById(R.id.data_RC);
        dataTempRC = v.findViewById(R.id.data_temp_RC);
        intervalTimeRC = v.findViewById(R.id.interval_time_RC);
        testTimeRC = v.findViewById(R.id.test_time_RC);

        singleLayoutCH = v.findViewById(R.id.single_layout_CH);
        dataCH = v.findViewById(R.id.data_CH);
        dataTempCH = v.findViewById(R.id.data_temp_CH);
        intervalTimeCH = v.findViewById(R.id.interval_time_CH);
        testTimeCH = v.findViewById(R.id.test_time_CH);

        singleLayoutCY = v.findViewById(R.id.single_layout_CY);
        dataCY = v.findViewById(R.id.data_CY);
        dataTempCY = v.findViewById(R.id.data_temp_CY);
        intervalTimeCY = v.findViewById(R.id.interval_time_CY);
        testTimeCY = v.findViewById(R.id.test_time_CY);

        singleLayoutTSS = v.findViewById(R.id.single_layout_TSS);
        dataTSS = v.findViewById(R.id.data_TSS);
        dataTempTSS = v.findViewById(R.id.data_temp_TSS);
        intervalTimeTSS = v.findViewById(R.id.interval_time_TSS);
        testTimeTSS = v.findViewById(R.id.test_time_TSS);

        singleLayoutTSM = v.findViewById(R.id.single_layout_TSM);
        dataTSM = v.findViewById(R.id.data_TSM);
        dataTempTSM = v.findViewById(R.id.data_temp_TSM);
        intervalTimeTSM = v.findViewById(R.id.interval_time_TSM);
        testTimeTSM = v.findViewById(R.id.test_time_TSM);

        singleLayoutOW = v.findViewById(R.id.single_layout_OW);
        dataOW = v.findViewById(R.id.data_OW);
        dataTempOW = v.findViewById(R.id.data_temp_OW);
        intervalTimeOW = v.findViewById(R.id.interval_time_OW);
        testTimeOW = v.findViewById(R.id.test_time_OW);


        linechartName = v.findViewById(R.id.linechart_name);
//        lineChartSpecialCOD = v.findViewById(R.id.dy_line_chart_special_COD_ZS);
//        lineChartSpecialCODBOD = v.findViewById(R.id.dy_line_chart_special_COD_BOD);

        lineCharts = new LineChart[]{lineChartTEMP, lineChartCOD, lineChartCODNT, lineChartEC, lineChartPH, lineChartORP, lineChartDO, lineChartNH, lineChartNT};

        text1 = v.findViewById(R.id.text1); // 测试数据
        text2 = v.findViewById(R.id.text2); // 测试温度
        text3 = v.findViewById(R.id.text3); // 间隔时间
        text4 = v.findViewById(R.id.text4); // 测试时间

        // 电量(球形)
        electricLayout = v.findViewById(R.id.electric_layout); // 电量框
        waveView = v.findViewById(R.id.wave_view); // 电量图
        electricData = v.findViewById(R.id.electric_data); // 电量数值

        // 电量(电池形状)
        electricLayout2 = v.findViewById(R.id.electric_layout_2);
        electricBox = v.findViewById(R.id.electric_box);
        electricData2 = v.findViewById(R.id.electric_data_2);

        // 折线图显示函数s
        initDyLineChart();
        initLineChartDemo();


        // 经纬度部分
        lngAndLat = v.findViewById(R.id.lng_and_lat);
        lngAndLatValue = v.findViewById(R.id.lng_and_lat_value);


        // http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=11418.29079&y=3546.3569




        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();

        // 在这里渲染整个页面的值
        System.out.println("RealDataFragment中的onResume()方法");

        Location location = getCurrentLngAndLat();
        try {
            System.out.println("原：" + String.valueOf(location.getLongitude()) + "  ;  " + String.valueOf(location.getLatitude()));
            httpRequest(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()));

        } catch (NullPointerException e) {
            e.printStackTrace();
            lngAndLat.setVisibility(View.GONE);
            System.out.println("空指针异常");
            // 就调用另一个函数进行经纬度查询
            getCurrentLngAndLat2();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            lngAndLat.setVisibility(View.GONE);
            System.out.println("空指针异常");
        }

        deviceName = v.findViewById(R.id.device_name);  // 设备名称
        deviceAddress = v.findViewById(R.id.device_address); // 设备地址
        deviceType = v.findViewById(R.id.device_type); // 设备类型
        btn = v.findViewById(R.id.btn); // 刷新按钮
        sensorType = v.findViewById(R.id.sensor_type); // 传感器类型图片
        basicData1 = v.findViewById(R.id.basic_data1);
        basicData2 = v.findViewById(R.id.basic_data2);
        proData = v.findViewById(R.id.pro_data);

        pro01 = v.findViewById(R.id.pro1);
        pro02 = v.findViewById(R.id.pro2);
        pro03 = v.findViewById(R.id.pro3);
        pro04 = v.findViewById(R.id.pro4);
        pro05 = v.findViewById(R.id.pro5);
        pro06 = v.findViewById(R.id.pro6);
        pro07 = v.findViewById(R.id.pro7);
        pro08 = v.findViewById(R.id.pro8);
        pro09 = v.findViewById(R.id.pro9);
        pro10 = v.findViewById(R.id.pro10);
        pro11 = v.findViewById(R.id.pro11);


        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication) getActivity().getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication) getActivity().getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication) getActivity().getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication) getActivity().getApplication()).getBasicDeviceType();
        globalType = ((MyApplication) getActivity().getApplication()).getBasicType();
        globalIntervalTime = ((MyApplication) getActivity().getApplication()).getIntervalTime();
        globalTestTime = ((MyApplication) getActivity().getApplication()).getTestTime();

        System.out.println("globalDeviceIsConnect的值是：" + globalDeviceIsConnect);

        // 初始化
        if (!globalDeviceIsConnect) {
            basicData2.setVisibility(View.GONE);
            proData.setVisibility(View.GONE);
        }

//
//      deviceType.setText("传感器类型：" + globalDeviceType);

        if (globalDeviceName == null) {
            deviceName.setText(getString(R.string.name) + getString(R.string.device_ununited));
        } else {
            deviceName.setText(getString(R.string.name) + globalDeviceName);
        }
        if (globalDeviceAddress == 0) {
            deviceAddress.setText(getString(R.string.device_address) + getString(R.string.data_not_obtained));
        } else {
            deviceAddress.setText(getString(R.string.device_address) + globalDeviceAddress);
        }
        if (globalType == 0) {
            deviceType.setText(getString(R.string.device_type) + getDeviceType(globalDeviceType) + getString(R.string.sensor));
        } else if (globalType == 1) {
            deviceType.setText(getString(R.string.device_type) + getDeviceType(999) + getString(R.string.sensor));
        } else if (globalDeviceType == -1) {
            deviceType.setText(getString(R.string.device_type) + getString(R.string.data_not_obtained));
        }

        // 传感器类型图片初始
        drawableSensorType = getResources().getDrawable(R.mipmap.type_0);

        // 按钮可点击样式
        drawable = getResources().getDrawable(R.drawable.button_style_real_time_data);
        // 按钮不可点击样式
        drawableForbidden = getResources().getDrawable(R.drawable.button_style_real_time_data_forbidden);

        drawableBlue = getResources().getDrawable(R.drawable.layout_border_style_right);
        drawableOrange = getResources().getDrawable(R.drawable.layout_border_style_right_orange);
        drawableRed = getResources().getDrawable(R.drawable.layout_border_style_right_red);


        electricBox.setBackground(null);

        // 页面滑动和图表滑动干扰的问题
        ScrollView scrollView = v.findViewById(R.id.scrollview);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            float ratio = 1.8f; // 水平和竖直方向滑动的灵敏度,偏大是水平方向灵敏
            float x0 = 0f;
            float y0 = 0f;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x0 = event.getX();
                        y0 = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getX() - x0);
                        float dy = Math.abs(event.getY() - y0);
                        x0 = event.getX();
                        y0 = event.getY();
                        scrollView.requestDisallowInterceptTouchEvent(dx * ratio > dy);
                        break;
                }
                return false;
            }
        };

        // 间隔时间
        text3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.select_skip_order_time))
                        .setNegativeButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), TimeOrderSetActivity.class);
                                startActivity(intent);
                            }
                        }).setPositiveButton(getString(R.string.cancel), null)
                        .setCancelable(true)
                        .show();
            }
        });

        // 测试时间
        text4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.select_skip_order_time))
                        .setNegativeButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), TimeOrderSetActivity.class);
                                startActivity(intent);
                            }
                        }).setPositiveButton(getString(R.string.cancel), null)
                        .setCancelable(true)
                        .show();
            }
        });

        pro10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!globalDeviceIsConnect) {
                    return;
                }
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.select_skip_order_time))
                        .setNegativeButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), TimeOrderSetActivity.class);
                                startActivity(intent);
                            }
                        }).setPositiveButton(getString(R.string.cancel), null)
                        .setCancelable(true)
                        .show();
            }
        });

        pro11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!globalDeviceIsConnect) {
                    return;
                }
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.select_skip_order_time))
                        .setNegativeButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getActivity(), TimeOrderSetActivity.class);
                                startActivity(intent);
                            }
                        }).setPositiveButton(getString(R.string.cancel), null)
                        .setCancelable(true)
                        .show();
            }
        });

        // 温度
        pro01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro01.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro01.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChart0.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_temperature) + ")");
            }
        });

        // COD
        pro02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro02.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro02.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartCOD.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[0] + ")");
            }
        });

        // COD->NT
        pro03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro03.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro03.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartCODNT.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[1] + ")");
            }
        });

        // EC
        pro04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro04.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro04.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartEC.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[2] + ")");
            }
        });

        // PH
        pro05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro05.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro05.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartPH.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[3] + ")");
            }
        });

        // ORP
        pro06.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro06.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro06.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartORP.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[4] + ")");
            }
        });

        // DO
        pro07.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro07.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro07.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartDO.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[5] + ")");
            }
        });

        // NH
        pro08.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro08.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro08.setTextColor(Color.BLUE);
                // 显示折线图
                hideLineChart();
                lineChartNH.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[6] + ")");
            }
        });

        // NT
        pro09.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示数据框样式
                displayAttributeToCommon();
                pro09.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                pro09.setTextColor(Color.BLUE);

                // 显示折线图
                hideLineChart();
                lineChartNT.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[7] + ")");
            }
        });

        // 点击事件
        dataEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleEC1.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC1) + ")");

                displayAttributeToCommon();
                dataEC1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataEC1.setTextColor(Color.BLUE);
            }
        });
        // 电导率1温度
        dataTempEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleEC1Temp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC1_Temp) + ")");

                displayAttributeToCommon();
                dataTempEC1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempEC1.setTextColor(Color.BLUE);
            }
        });

        // 电导率2
        dataEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleEC2.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC2) + ")");

                displayAttributeToCommon();
                dataEC2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataEC2.setTextColor(Color.BLUE);
            }
        });

        // 电导率2温度
        dataTempEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleEC2Temp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC2_Temp) + ")");

                displayAttributeToCommon();
                dataTempEC2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempEC2.setTextColor(Color.BLUE);
            }
        });

        // PH
        dataPH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSinglePH.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_PH) + ")");

                displayAttributeToCommon();
                dataPH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataPH.setTextColor(Color.BLUE);
            }
        });

        // PH温度
        dataTempPH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSinglePHTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_PH) + ")");

                displayAttributeToCommon();
                dataTempPH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempPH.setTextColor(Color.BLUE);
            }
        });

        // ORP
        dataORP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleORP.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_ORP_0) + ")");

                displayAttributeToCommon();
                dataORP.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataORP.setTextColor(Color.BLUE);
            }
        });

        // RDO
        dataRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleRDO.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_DO) + ")");

                displayAttributeToCommon();
                dataRDO.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataRDO.setTextColor(Color.BLUE);
            }
        });

        // 溶解氧温度
        dataTempRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleRDOTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_DO_Temp) + ")");

                displayAttributeToCommon();
                dataTempRDO.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempRDO.setTextColor(Color.BLUE);
            }
        });

        // 氨氮
        dataNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleNHN.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_NH) + ")");

                displayAttributeToCommon();
                dataNHN.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataNHN.setTextColor(Color.BLUE);
            }
        });

        // 氨氮温度
        dataTempNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleNHNTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_NH_Temp) + ")");

                displayAttributeToCommon();
                dataTempNHN.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempNHN.setTextColor(Color.BLUE);
            }
        });

        // 浊度
        dataZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleZS.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_ZS) + ")");

                displayAttributeToCommon();
                dataZS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataZS.setTextColor(Color.BLUE);
            }
        });

        // 浊度温度
        dataTempZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleZSTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_ZS_Temp) + ")");

                displayAttributeToCommon();
                dataTempZS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempZS.setTextColor(Color.BLUE);
            }
        });

        // 盐度
        dataSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleSAL.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_salinity) + ")");

                displayAttributeToCommon();
                dataSAL.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataSAL.setTextColor(Color.BLUE);
            }
        });

        // 盐度温度
        dataTempSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleSALTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_salinity_Temp) + ")");

                displayAttributeToCommon();
                dataTempSAL.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempSAL.setTextColor(Color.BLUE);
            }
        });

        // COD
        dataCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCOD.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_COD) + ")");

                displayAttributeToCommon();
                dataCOD.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataCOD.setTextColor(Color.BLUE);
            }
        });

        // COD 浊度
        dataCODZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCODZS.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_COD_ZS) + ")");

                displayAttributeToCommon();
                dataCODZS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataCODZS.setTextColor(Color.BLUE);
            }
        });

        // COD BOD
        dataCODBOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCODBOD.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(BOD)");

                displayAttributeToCommon();
                dataCODBOD.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataCODBOD.setTextColor(Color.BLUE);
            }
        });

        // COD 温度
        dataTempCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCODTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_COD_Temp) + ")");

                displayAttributeToCommon();
                dataTempCOD.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempCOD.setTextColor(Color.BLUE);
            }
        });

        // RC
        dataRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleRC.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_residual_chlorine) + ")");

                displayAttributeToCommon();
                dataRC.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataRC.setTextColor(Color.BLUE);
            }
        });

        // RC 温度
        dataTempRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleRCTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_residual_chlorine_Temp) + ")");

                displayAttributeToCommon();
                dataTempRC.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempRC.setTextColor(Color.BLUE);
            }
        });

        // 叶绿素
        dataCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCH.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Chlorophyl) + ")");

                displayAttributeToCommon();
                dataCH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataCH.setTextColor(Color.BLUE);
            }
        });

        // 叶绿素温度
        dataTempCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCHTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Chlorophyl_Temp) + ")");

                displayAttributeToCommon();
                dataTempCH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempCH.setTextColor(Color.BLUE);
            }
        });

        // 蓝绿藻
        dataCY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCY.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_blue_green_algae) + ")");

                displayAttributeToCommon();
                dataCY.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataCY.setTextColor(Color.BLUE);
            }
        });

        // 蓝绿藻温度
        dataTempCY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleCYTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_blue_green_algae_Temp) + ")");

                displayAttributeToCommon();
                dataTempCY.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempCY.setTextColor(Color.BLUE);
            }
        });

        // 透明度
        dataTSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleTSS.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Transparency) + ")");

                displayAttributeToCommon();
                dataTSS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTSS.setTextColor(Color.BLUE);
            }
        });

        // 透明度 温度
        dataTempTSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleTSSTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Transparency_Temp) + ")");

                displayAttributeToCommon();
                dataTempTSS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempTSS.setTextColor(Color.BLUE);
            }
        });

        // 悬浮物
        dataTSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleTSM.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Suspended_Solids) + ")");

                displayAttributeToCommon();
                dataTSM.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTSM.setTextColor(Color.BLUE);
            }
        });

        // 悬浮物 温度
        dataTempTSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleTSMTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Suspended_Solids_Temp) + ")");

                displayAttributeToCommon();
                dataTempTSM.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempTSM.setTextColor(Color.BLUE);
            }
        });

        // 水中油OW
        dataOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleOW.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_oil_in_water) + ")");

                displayAttributeToCommon();
                dataOW.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataOW.setTextColor(Color.BLUE);
            }
        });

        // 水中油OW 温度
        dataTempOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLineChart();
                lineChartSingleOWTemp.setVisibility(View.VISIBLE);
                linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_oil_in_water_Temp) + ")");

                displayAttributeToCommon();
                dataTempOW.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
                dataTempOW.setTextColor(Color.BLUE);
            }
        });

        // 电导率1
        intervalTimeEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // 电导率1
        testTimeEC1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // 电导率2
        intervalTimeEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeEC2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // PH
        intervalTimePH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimePH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // ORP
        intervalTimeORP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeORP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // RDO
        intervalTimeRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // NHN
        intervalTimeNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeNHN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // ZS
        intervalTimeZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeZS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // SAL
        intervalTimeSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeSAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // COD
        intervalTimeCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeCOD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // RC
        intervalTimeRC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeRDO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // CH
        intervalTimeCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // CY
        intervalTimeCY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeCY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // TSS
        intervalTimeTSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeTSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // TSM
        intervalTimeTSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeTSM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        // OW
        intervalTimeOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });
        testTimeOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpTempModify();
            }
        });


        // 如果没有连接设备，就只能显示默认值了
        if (!globalDeviceIsConnect) {
//            new AlertDialog.Builder(real_data_view.getContext()).setMessage("请先连接设备").setPositiveButton("确定",null).show();

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn.setBackground(drawableForbidden);
                    btn.setEnabled(false);
                    if (dy != null) {
                        // 清空lineChart
                        dy.destroyChart();
                    }

                    new AlertDialog.Builder(real_data_view.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            btn.setBackground(drawable);
                            btn.setEnabled(true);
                        }
                    }).setCancelable(false).show();
                }
            });

            text1.setEnabled(false);
            text2.setEnabled(false);
            text3.setEnabled(false);
            text4.setEnabled(false);
            pro10.setEnabled(false);
            pro11.setEnabled(false);

        } else {

            System.out.println("自动刷新---------------------------------------------------------------------------");

            String number = "----";
            int bend = number.length();
            String str = number + "\nnull";
            SpannableStringBuilder style = new SpannableStringBuilder(str);
            style.setSpan(new AbsoluteSizeSpan(70), 0, bend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text1.setText(style);
            text2.setText(style);
            text3.setText(style);
            text4.setText(style);

            text1.setEnabled(true);
            text2.setEnabled(true);
            text3.setEnabled(true);
            text4.setEnabled(true);
            pro10.setEnabled(true);
            pro11.setEnabled(true);


            // 操作Operation来获取设备的实时数据等情况
            // 获得这个设备的特性
            bleDevice = ((MyApplication) getActivity().getApplication()).getBasicBleDevice();
            bleManager = ((MyApplication) getActivity().getApplication()).getBasicBleManager();
            gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
            if (gatt != null) {
                for (BluetoothGattService s : gatt.getServices()) {
                    service = s;
                    if ("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                        // 安信可芯片
                        System.out.println("安信可芯片");
                        BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
                        int ii = 0;
                        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                            System.out.println("特征值值内容：" + c.getUuid().toString());
                            cs[ii++] = c;
                        }
                        characteristicRead = cs[1];
                        characteristic = cs[0];
                        chipType = 1;
                        break;
                    }
                    chipType = 2;
                }
            }
            if (chipType == 2) {
                // BT02-E104芯片
                for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                    // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
                    System.out.println("BT02-E104芯片");
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

            System.out.println("service相关的值是：" + service.getUuid());
//            BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
//            int ii = 0;
//            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
//                // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
////                String uid = c.getUuid().toString().split("-")[0];
//                System.out.println("特征值值内容：" + c.getUuid().toString());
////            System.out.println("将特征属性加入到mResultAdapter:" + characteristic.getProperties());
//                cs[ii ++] = c;
////                if ("0000fff1".equals(uid)) {
//////                if ("5833ff02".equals(uid)) {
////                    characteristicRead = c;
////                } else if ("0000fff2".equals(uid)) {
//////                } else if ("5833ff03".equals(uid)) {
////                    characteristic = c;
////                }
//            }
//            characteristicRead = cs[1];
//            characteristic = cs[0];

            System.out.println("characteristic的特征值是：" + characteristic.getUuid());
//            System.out.println("是否一样：" + (characteristicRead == characteristic));
            // 获取到所有应该获取的值了，可以将未存储的数据存储到全局中去
            ((MyApplication) getActivity().getApplication()).setBasicCharacteristic(characteristic);
            ((MyApplication) getActivity().getApplication()).setBasicCharacteristicRead(characteristicRead);
            ((MyApplication) getActivity().getApplication()).setBasicBluetoothGatt(gatt);
            ((MyApplication) getActivity().getApplication()).setBasicService(service);

            // 接下来获取实时数据的数值
//            System.out.println("接下来要写入fa的数据了");
            freshNum = 1; // 初始化的第一次刷新
            // 给progressDialog加一个定时关闭的功能
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //要做的事情
                    if (progressDialogReal.isShowing()) {
                        btn.setBackground(drawable);
                        btn.setEnabled(true);
                        progressDialogReal.dismiss();
//                        new AlertDialog.Builder(getContext()).setMessage(getString(R.string.real_data_gain_fail)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                handler.removeCallbacks(runnable);
//                            }
//                        }).setCancelable(false).show();
                        handler.removeCallbacks(runnable);

                        // 如果没有数据
                        System.out.println("isGetData:" + isGetData);
                        if (!isGetData) {
                            if(freshNum % 2 == 1) {
                                // 没有数据就读取fa指令
                                System.out.println("没有数据，读取电量，通过fa指令");
                                judgeSurplusElectric();
                                freshNum ++;
                                System.out.println("没有数据，读取电量，通过fa指令:" + freshNum);
                            } else {
                                System.out.println("没有数据，再次刷新");
                                refreshData();
                                freshNum ++;
                                System.out.println("没有数据，再次刷新" + freshNum);
                            }
                        }
                    }
                }
            };


            ((MyApplication) getActivity().getApplication()).setBasicRealDataIsWorking(true);

//            BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());
//
//            getDeviceAddress();
//            if(text4.getText().equals(style)) {
//                System.out.println("变化--------------------------------");
//                refreshData();
//            }

            if (((MyApplication) getActivity().getApplication()).getIntervalTime() > 0) {
                String i = Integer.toString(((MyApplication) getActivity().getApplication()).getIntervalTime());
                String t = Integer.toString(((MyApplication) getActivity().getApplication()).getTestTime());
                // 如果是多参数
                if (((MyApplication) getActivity().getApplication()).getBasicType() == 1) {
                    displayTime(i, t, pro10, pro11);
                }
                // 如果是单参数
                if (((MyApplication) getActivity().getApplication()).getBasicType() == 0
                        && ((MyApplication) getActivity().getApplication()).getBasicDeviceType() == 9) {
                    displayTime(i, t, intervalTimeORP, testTimeORP);
                } else {
                    displayTime(i, t, intervalTimeEC1, testTimeEC1);
                    displayTime(i, t, intervalTimeEC2, testTimeEC2);
                    displayTime(i, t, intervalTimePH, testTimePH);
                    displayTime(i, t, intervalTimeRDO, testTimeRDO);
                    displayTime(i, t, intervalTimeNHN, testTimeNHN);
                    displayTime(i, t, intervalTimeZS, testTimeZS);
                    displayTime(i, t, intervalTimeSAL, testTimeSAL);
                    displayTime(i, t, intervalTimeCOD, testTimeCOD);
                    displayTime(i, t, intervalTimeRC, testTimeRC);
                    displayTime(i, t, intervalTimeCH, testTimeCH);
                    displayTime(i, t, intervalTimeCY, testTimeCY);
                    displayTime(i, t, intervalTimeTSS, testTimeTSS);
                    displayTime(i, t, intervalTimeTSM, testTimeTSM);
                    displayTime(i, t, intervalTimeOW, testTimeOW);
                }
            }
        }

        // 电量图标
        waveView.start();
    }

    // 获取设备类型
    public String getDeviceType(int a) {
        if (a == 0) {
            chartMax = 100;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_0);
            return getString(R.string.ununited);
        } else if (a == 1) {
            chartMax = 100;
            chartLabelCount = 8;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_1);
            return getString(R.string.data_EC);
        } else if (a == 2) {
            chartMax = 100;
            chartLabelCount = 8;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_2);
            return getString(R.string.data_EC);
        } else if (a == 3) {
            chartMax = 14;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_3);
            return getString(R.string.data_PH);
        } else if (a == 4) {
            chartMax = 100;
            chartMin = 0;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_4);
            return getString(R.string.data_ORP);
        } else if (a == 5) {
            chartMax = 20;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_5);
            return getString(R.string.data_DO);
        } else if (a == 6) {
            chartMax = 100;
            chartLabelCount = 8;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_6);
            return getString(R.string.data_NH);
        } else if (a == 7) {
            chartMax = 100;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_7);
            return getString(R.string.data_ZS);
        } else if (a == 8) {
            chartMax = 70;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_8);
            return getString(R.string.data_salinity);
        } else if (a == 9) {
            chartMax = 100;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_9);
            return getString(R.string.data_COD);
        } else if (a == 10) {
            chartMax = 2;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_10);
            return getString(R.string.data_residual_chlorine);
        } else if (a == 11) {
            chartMax = 400;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_11);
            return getString(R.string.data_Chlorophyl);
        } else if (a == 12) {
            chartMax = 300;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_12);
            return getString(R.string.data_blue_green_algae);
        } else if (a == 13) {
            chartMax = 100;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_13);
            return getString(R.string.data_Transparency);
        } else if (a == 14) {
            chartMax = 100;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_14);
            return getString(R.string.data_Suspended_Solids);
        } else if (a == 15) {
            chartMax = 40;
            chartLabelCount = 10;
            drawableSensorType = getResources().getDrawable(R.mipmap.type_15);
            return getString(R.string.data_oil_in_water);
        } else if (a == 999) {
            drawableSensorType = getResources().getDrawable(R.mipmap.type_999);
            return getString(R.string.data_multiparameter);
        }
        chartMax = 100;
        chartLabelCount = 10;
        drawableSensorType = getResources().getDrawable(R.mipmap.type_0);
        return "----";
    }

    // 获取设备单位
    public String getDeviceUnit(int a) {
        if (a == 0) {
            return "";
        } else if (a == 1) {
            return "uS/cm";
        } else if (a == 2) {
            return "mS/cm";
        } else if (a == 3) {
            return "PH";
        } else if (a == 4) {
            return "mv";
        } else if (a == 5) {
            return "mg/L";
        } else if (a == 6) {
            return "mg/L";
        } else if (a == 7) {
            return "NTU";
        } else if (a == 8) {
            return "PSU";
        } else if (a == 9) {
            return "mg/L";
        } else if (a == 10) {
            return "mg/L";
        } else if (a == 11) {
            return "ug/L";
        } else if (a == 12) {
            return "kcells/ml";
        } else if (a == 13) {
            return "mm";
        } else if (a == 14) {
            return "mg/L";
        } else if (a == 15) {
            return "mg/L";
        }
        return "----";
    }


    @Override
    public void onPause() {
        super.onPause();
        System.out.println("RealDataFragment调用onPause()方法");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("RealDataFragment调用onStop()方法");
        System.out.println(getFragmentManager().getFragments().size());
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    /**
     * 通过工具类，动态添加数据，不断使x轴偏移
     * //                lineChart.fitScreen();
     * //                lineChart.clear();
     * //                lineChart0.fitScreen();
     * //                lineChart0.clear();
     * //                lineChartSpecialCOD.fitScreen();
     * //                lineChartSpecialCOD.clear();
     */

    // 首先获取设备的地址和类型
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getDeviceAddress() {

        // 初始化实时数据存储的序号
        initRealDataDownloadNum();

        // 初始化多参数配置传感器类型
        initMutilSensorSet();

        // 如果此时存储的设备地址是0，表示没有设备地址的存入
//        if(globalDeviceAddress == 0 || globalIntervalTime == -1) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        String dateString = formatter.format(date);
        String hex = "f900";
        System.out.println(dateString + "  现在发送的指令是" + hex);
        isGetData = false;
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

                                System.out.println("读取设备地址和类型指令写入成功");
                                new Handler().postDelayed(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    @Override
                                    public void run() {
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
                                                                System.out.println("读取设备地址成功");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("读取设备地址失败：" + exception.toString());
                                                                new AlertDialog.Builder(real_data_view.getContext()).setMessage(getString(R.string.address_gain_fail)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        btn.setBackground(drawable);
                                                                        btn.setEnabled(true);
                                                                    }
                                                                }).setCancelable(false).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCharacteristicChanged(final byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("开始读取地址和设备类型信息...");
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                    @Override
                                                                    public void run() {
                                                                        Date date = new Date();
                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                                                        String dateString = formatter.format(date);
//                                                                            System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                            for(byte b : characteristicRead.getValue()) {
//                                                                                System.out.println(b);
//                                                                            }
//                                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                        String s1 = HexUtil.byteToString(data);
                                                                        String s2 = "时间:" + dateString + ",接收到的f900实时数据:" + s1 + "长度：" + s1.split(",").length;
                                                                        System.out.println("接收：" + s2);

                                                                        System.out.println("原始数据" + s1);
                                                                        int length = s1.split(",").length;
                                                                        System.out.println("原始数据长度" + s1.split(",").length);
                                                                        System.out.println(s1.charAt(s1.length() - 1));

                                                                        // 判断若是只有一部分内容
                                                                        if (s1.charAt(0) == '{' && s1.charAt(s1.length() - 1) != '}' && incompleteData.length() == 0) {
                                                                            // 不完全数据的前半部分
                                                                            incompleteData = s1;
                                                                        }
                                                                        if (s1.charAt(0) != '{' && s1.charAt(s1.length() - 1) != '}' && incompleteData.length() > 0) {
                                                                            // 不完全数据的中间部分
                                                                            incompleteData = incompleteData + s1;
                                                                        }
                                                                        if (s1.charAt(0) != '{' && s1.charAt(s1.length() - 1) == '}' && incompleteData.length() > 0) {
                                                                            // 不完全数据的后半部分
                                                                            s1 = incompleteData + s1;
                                                                            incompleteData = "";
                                                                        }

                                                                        // 对组合后的数据进行解析
                                                                        if (s1.charAt(0) == '{' && s1.charAt(s1.length() - 1) == '}') {
                                                                            // 表明是实时数据
                                                                            System.out.println("此为实时数据");
                                                                            s1 = s1.replaceAll("[{}]", "");
                                                                            isRealData = true;
                                                                        } else {
                                                                            isRealData = false;
                                                                        }


                                                                        ((MyApplication) getActivity().getApplication()).setBasicHistoryDataSign(true);

                                                                        int address = -1; // 设备地址
                                                                        int type0 = ((MyApplication) getActivity().getApplication()).getBasicType(); // 设备类型（多参数、单参数）
                                                                        int type = ((MyApplication) getActivity().getApplication()).getBasicDeviceType(); // 传感器类型
                                                                        boolean isDismissDialog = false;

                                                                        // 按照传感器类型类分别渲染实时数据页面
                                                                        // 如果是电导率1
                                                                        if (isGetData && isRealData && type0 == 0 && type == 1) {
                                                                            System.out.println("电导率1传感器---》电导率1数据");
                                                                            displayEC1(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是电导率2
                                                                        if (isGetData && isRealData && type0 == 0 && type == 2) {
                                                                            System.out.println("电导率2传感器---》电导率2数据");
                                                                            displayEC2(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是PH
                                                                        if (isGetData && isRealData && type0 == 0 && type == 3) {
                                                                            System.out.println("PH传感器---》PH数据");
                                                                            displayPH(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是ORP
                                                                        if (isGetData && isRealData && type0 == 0 && type == 4) {
                                                                            System.out.println("ORP传感器---》ORP数据");
                                                                            displayORP(s1);
                                                                            if (s1.split(",").length == 4) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }

                                                                        }

                                                                        // 如果是溶解氧
                                                                        if (isGetData && isRealData && type0 == 0 && type == 5) {
                                                                            System.out.println("溶解氧传感器---》溶解氧数据");
                                                                            displayRDO(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是氨氮
                                                                        if (isGetData && isRealData && type0 == 0 && type == 6) {
                                                                            System.out.println("氨氮传感器---》氨氮数据");
                                                                            displayNHN(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是浊度
                                                                        if (isGetData && isRealData && type0 == 0 && type == 7) {
                                                                            System.out.println("浊度传感器---》浊度数据");
                                                                            displayZS(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是盐度
                                                                        if (isGetData && isRealData && type0 == 0 && type == 8) {
                                                                            System.out.println("盐度传感器---》盐度数据");
                                                                            displaySAL(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是化学需氧量COD
                                                                        if (isGetData && isRealData && type0 == 0 && type == 9) {
                                                                            System.out.println("COD传感器---》COD数据");
                                                                            displayCOD(s1);
                                                                            if (s1.split(",").length == 7) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是余氯
                                                                        if (isGetData && isRealData && type0 == 0 && type == 10) {
                                                                            System.out.println("余氯传感器---》余氯数据");
                                                                            displayRC(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是叶绿素
                                                                        if (isGetData && isRealData && type0 == 0 && type == 11) {
                                                                            System.out.println("叶绿素传感器---》叶绿素数据");
                                                                            displayCH(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是蓝绿藻
                                                                        if (isGetData && isRealData && type0 == 0 && type == 12) {
                                                                            System.out.println("蓝绿藻传感器---》蓝绿藻数据");
                                                                            displayCY(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是透明度
                                                                        if (isGetData && isRealData && type0 == 0 && type == 13) {
                                                                            System.out.println("透明度传感器---》透明度数据");
                                                                            displayTSS(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是悬浮物
                                                                        if (isGetData && isRealData && type0 == 0 && type == 14) {
                                                                            System.out.println("悬浮物传感器---》悬浮物数据");
                                                                            displayTSM(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是水中油
                                                                        if (isGetData && isRealData && type0 == 0 && type == 15) {
                                                                            System.out.println("水中油传感器---》水中油数据");
                                                                            displayOW(s1);
                                                                            if (s1.split(",").length == 5) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }

                                                                        // 如果是多参数
                                                                        if (isGetData && isRealData && type0 == 1) {
                                                                            System.out.println("多参数传感器---》多参数数据");
                                                                            displayMUL(s1);
                                                                            if (s1.split(",").length == 12) {
                                                                                isDismissDialog = true;
                                                                            } else {
                                                                                isDismissDialog = false;
                                                                            }
                                                                        }
                                                                        if (isDismissDialog) {
                                                                            new Handler().postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    progressDialogReal.hide();
                                                                                    progressDialogReal.dismiss();
                                                                                    handler.removeCallbacks(runnable);
                                                                                }
                                                                            }, 0);
                                                                        }

                                                                        // 如果获取的数据是这种类型：[12,1,16,]
                                                                        if (s1.split(",").length == 4 && s1.charAt(0) == '[' && s1.charAt(s1.length() - 1) == ']') {
                                                                            System.out.println("地址和类型获取成功：" + s1);
                                                                            String digits = s1.replaceAll("[^0-9,/\\[\\]]", "");
                                                                            if (digits.length() != s1.length()) {
                                                                                System.out.println(s1);
                                                                                System.out.println(digits);
                                                                                progressDialogReal.setMessage(getString(R.string.data_format_issues));
                                                                                new Handler().postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        progressDialogReal.dismiss();
                                                                                        BleManager.getInstance().stopNotify(bleDevice, characteristicRead.getService().getUuid().toString(), characteristicRead.getUuid().toString());

                                                                                    }
                                                                                }, 1200);
                                                                            }
                                                                            s1 = s1.replaceAll("[\\[\\]]", "");
                                                                            address = Integer.parseInt(s1.split(",")[0]); // 设备地址
                                                                            type0 = Integer.parseInt(s1.split(",")[1]); // 设备类型（多参数、单参数）
                                                                            type = Integer.parseInt(s1.split(",")[2]); // 传感器类型

                                                                            // 这里设置地址显示
                                                                            deviceAddress.setText(getString(R.string.device_address) + address);
                                                                            progressDialogReal.setMessage(getString(R.string.address_gain_success));

                                                                            if (type0 == 1) {
                                                                                // 如果是多参数
                                                                                deviceType.setText(getString(R.string.device_type) + getDeviceType(999) + getString(R.string.sensor));
                                                                                sensorType.setBackground(drawableSensorType);

                                                                                // 存储多参数
                                                                                MyLog.downloadRealDataMain(0, 999, "", ((MyApplication) getActivity().getApplication()).getMutilSensor());


                                                                            } else if (type0 == 0) {
                                                                                // 如果是单参数
                                                                                deviceType.setText(getString(R.string.device_type) + getDeviceType(type) + getString(R.string.sensor));
                                                                                sensorType.setBackground(drawableSensorType);

                                                                                // 存储多参数
                                                                                MyLog.downloadRealDataMain(0, type, "", ((MyApplication) getActivity().getApplication()).getMutilSensor());
                                                                            }

                                                                            ((MyApplication) getActivity().getApplication()).setBasicDeviceAddress(address);
                                                                            ((MyApplication) getActivity().getApplication()).setBasicDeviceType(type);
                                                                            ((MyApplication) getActivity().getApplication()).setBasicType(type0);
                                                                            progressDialogReal.setMessage(getString(R.string.type_gain_success));
                                                                            globalDeviceType = type;

                                                                            // 标志着地址和类型读取完
                                                                            isGetData = true;
                                                                        }
//


                                                                    }
                                                                }, 100);


                                                            }
                                                        });
                                                    }
                                                });


                                    }
                                }, 1000);


                            }

                            @Override
                            public void onWriteFailure(BleException exception) {

                            }
                        });
            }
        }, 500);
//        }else {
//            deviceAddress.setText(getString(R.string.device_address) + ((MyApplication) getActivity().getApplication()).getBasicDeviceAddress());
//            getRealData();
//        }

    }

    // 判断电量并弹出馈电提示
    public void judgeSurplusElectric() {

        progressDialogReal = new ProgressDialog(getActivity());
        progressDialogReal.setIcon(R.mipmap.ic_launcher);
        progressDialogReal.setTitle(getString(R.string.init_data));
        progressDialogReal.setMessage(getString(R.string.speed_gain_data));
        progressDialogReal.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
        progressDialogReal.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
        progressDialogReal.setButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialogReal.dismiss();
            }
        });
        progressDialogReal.show();
        handler.postDelayed(runnable, 50000);//每20秒执行一次runnable.

        // 下面是获取设备电量
        String hex = "fa";
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

                                System.out.println("fa指令读取设备地址和类型指令写入成功");
                                new Handler().postDelayed(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                    @Override
                                    public void run() {
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
                                                                System.out.println("fa指令发送成功");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("fa指令失败：" + exception.toString());
                                                                new AlertDialog.Builder(real_data_view.getContext()).setMessage(getString(R.string.address_gain_fail)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        btn.setBackground(drawable);
                                                                        btn.setEnabled(true);
                                                                    }
                                                                }).setCancelable(false).show();
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCharacteristicChanged(final byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("fa指令开始读取地址和设备类型信息...");
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                    @Override
                                                                    public void run() {
                                                                        Date date = new Date();
                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                                                        String dateString = formatter.format(date);
                                                                        String s1 = HexUtil.byteToString(data);
                                                                        String s2 = "时间:" + dateString + ",接收到的fa实时数据:" + s1 + "长度：" + s1.split(",").length;
                                                                        System.out.println("接收：" + s2);
                                                                        // {0,0,2.250}

                                                                        System.out.println("原始数据" + s1);
                                                                        int length = s1.split(",").length; // 参数数量
                                                                        System.out.println("原始数据长度" + s1.split(",").length);
                                                                        System.out.println(s1.charAt(s1.length() - 1));

                                                                        // 获取电量
                                                                        if(length >= 3) {
                                                                            double e = Double.parseDouble(s1.split(",")[2]);
                                                                            if(e < 2.8) {
                                                                                // 电量不足，开始提示
                                                                                progressDialogReal.setMessage(getString(R.string.battery_is_low));
                                                                                // 指令程序关闭
//                                                                                BleManager.getInstance().stopNotify(bleDevice, characteristic.getService().getUuid().toString(), characteristic.getUuid().toString());// 标志着地址和类型读取完
//                                                                                handler.postDelayed(runnable, 10000000);//每20秒执行一次runnable.
//                                                                                freshNum = -1;
                                                                                handler.removeCallbacks(runnable);
                                                                                isGetData = true;

                                                                            }
                                                                        }




                                                                    }
                                                                }, 100);


                                                            }
                                                        });
                                                    }
                                                });


                                    }
                                }, 1000);


                            }

                            @Override
                            public void onWriteFailure(BleException exception) {

                            }
                        });
            }
        }, 500);
    }

    // 刷新首页数据
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void refreshData() {
        // 刷新按钮，点击事件
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {

        // 先初始化折线图
//        destoryWay1(0);
//        destoryWay1(1);

        btn.setBackground(drawableForbidden);
        if (!((MyApplication) getActivity().getApplication()).getBasicDeviceIsConnect()) {
            new AlertDialog.Builder(real_data_view.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    btn.setBackground(drawable);
                    btn.setEnabled(true);
                }
            }).setCancelable(false).show();
            return;
        }

        progressDialogReal = new ProgressDialog(getActivity());
        progressDialogReal.setIcon(R.mipmap.ic_launcher);
        progressDialogReal.setTitle(getString(R.string.init_data));
        progressDialogReal.setMessage(getString(R.string.speed_gain_data));
        progressDialogReal.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
        progressDialogReal.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
        progressDialogReal.setButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialogReal.dismiss();
            }
        });
        progressDialogReal.show();
        handler.postDelayed(runnable, 40000);//每20秒执行一次runnable.

        // 从获取地址和类型开始
        getDeviceAddress();
//                }
//            });
    }

    // 页面显示和隐藏对应不同的功能
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;

        if (!isVisible && isResumed()) {
            System.out.println("real data 隐藏的代码块");
        } else if (isVisible && isResumed()) {
            System.out.println("real data 显示的代码块");
            if (globalDeviceIsConnect) {
                // fragment显示，就调用数据刷新函数
                refreshData();
            }
        }
    }

    // 电量显示：多参数和单参数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayElectricData(String e) {
//        electricLayout.setVisibility(View.VISIBLE);
        electricLayout2.setVisibility(View.VISIBLE);
//        String e = "";
//        // 单参数情况
//        if(n == 0) {
//            e = str.split(",")[2];
//        }
//        // 多参数情况
//        if(n == 1) {
//            e = str.split(",")[9];
//        }
//        // 特殊情况:COD
//        if(n == 9) {
//            e = str.split(",")[3];
//        }


        String electric = String.valueOf(getElectricQuantity(e));
        electricData.setText(electric + "%");
        electricData2.setText(electric + "% ");
        float electricF = 0f;
        if (electric.length() > 2) {
            electricF = 1.0f;
        } else if (electric.length() == 1) {
            electricF = Float.parseFloat("0.0" + electric);
        } else if (electric.length() == 2) {
            electricF = Float.parseFloat("0." + electric);
        }
        waveView.setWaveHeightPercent(electricF);

        int electricInt = Integer.parseInt(electric);
        ViewGroup.LayoutParams lp;
        lp = electricBox.getLayoutParams();
//        System.out.println("ectric:" + electric);
//        System.out.println("electricInt:" + electricInt);
//        System.out.println("lp.width:" + lp.width);
        int width = ((MyApplication) getActivity().getApplication()).getBasicDeviceBox();
        if (width == 0) {
            // 说明，电量池还没有被使用过
            width = lp.width;
            ((MyApplication) getActivity().getApplication()).setBasicDeviceBox(width);
        }
//        System.out.println("width:" + width);
        if (electricInt >= 80) {
            // 变蓝色
            electricBox.setBackground(drawableBlue);
            lp.width = width;
        } else if (electricInt > 70 && electricInt < 80) {
            if (electricBox.getBackground() != drawableBlue && electricBox.getBackground() != drawableOrange && electricBox.getBackground() != drawableRed) {
                // 变蓝色
                electricBox.setBackground(drawableBlue);
                lp.width = width;
            }
        } else if (electricInt >= 50 && electricInt <= 70) {
            // 变蓝色
            electricBox.setBackground(drawableBlue);
            lp.width = (int) (width * 70 / 100);
        } else if (electricInt > 40 && electricInt < 50) {
            if (electricBox.getBackground() != drawableBlue && electricBox.getBackground() != drawableOrange && electricBox.getBackground() != drawableRed) {
                // 变蓝色
                electricBox.setBackground(drawableBlue);
                lp.width = (int) (width * 70 / 100);
            }
        } else if (electricInt >= 20 && electricInt <= 40) {
            // 变橙色
            electricBox.setBackground(drawableOrange);
            lp.width = (int) (width * 40 / 100);
        } else if (electricInt > 10 && electricInt < 20) {
            if (electricBox.getBackground() != drawableBlue && electricBox.getBackground() != drawableOrange && electricBox.getBackground() != drawableRed) {
                // 变橙色
                electricBox.setBackground(drawableOrange);
                lp.width = (int) (width * 40 / 100);
            }
        } else if (electricInt <= 10) {
            // 变红色
            electricBox.setBackground(drawableRed);
            lp.width = (int) (width * 10 / 100);
        } else if (electricInt <= 0) {
            lp.width = 0;
        }
        electricBox.setLayoutParams(lp);
    }

    // 数值显示：
    // 单参数
    public void displayData(String d, String t, TextView data, TextView temp) {

        int bend1 = d.length();
        int bend2 = t.length();
        String strN1 = d + "\n"
                + getDeviceUnit(((MyApplication) getActivity().getApplication()).getBasicDeviceType());
        String strN2 = t + "℃" + "\n" + getString(R.string.data_temperature);
        SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
        SpannableStringBuilder style2 = new SpannableStringBuilder(strN2);
        style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style2.setSpan(new AbsoluteSizeSpan(50), 0, bend2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        data.setText(style1);
        temp.setText(style2);
    }

    // ORP
    public void displayData(String d, TextView data) {
        int bend1 = d.length();
        String strN1 = d + "\n"
                + getDeviceUnit(4);
        SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
        style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        data.setText(style1);
    }

    // COD
    public void displayData(String c, String t, String z, String b, TextView data, TextView temp, TextView zs, TextView bod) {
        int bend1 = c.length();
        int bend2 = t.length();
        int bend3 = z.length();
        int bend4 = b.length();
        String strN1 = c + "\n" + getDeviceUnit(9);
        String strN2 = t + "℃" + "\n" + getString(R.string.data_temperature);
        String strN3 = z + "\n" + getDeviceUnit(7);
        String strN4 = b + "\nmg/L";
        SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
        SpannableStringBuilder style2 = new SpannableStringBuilder(strN2);
        SpannableStringBuilder style3 = new SpannableStringBuilder(strN3);
        SpannableStringBuilder style4 = new SpannableStringBuilder(strN4);
        style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style2.setSpan(new AbsoluteSizeSpan(50), 0, bend2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style3.setSpan(new AbsoluteSizeSpan(50), 0, bend3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style4.setSpan(new AbsoluteSizeSpan(50), 0, bend4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        data.setText(style1);
        temp.setText(style2);
        zs.setText(style3);
        bod.setText(style4);
    }

    // 多参数
    public void displayData(String str) {
        String n1 = str.split(",")[0];
        int bend1 = n1.length();
        String strN1 = n1 + "℃" + "\n" + getString(R.string.data_temperature);
        SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
        style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        pro01.setText(style1);

        // int[] unit = {9,7,2,3,4,5,6,7}; // 单位
        // String[] name = new String[]{getString(R.string.data_COD_0), getString(R.string.data_COD_ZS), getString(R.string.data_EC_salinity), getString(R.string.data_PH), getString(R.string.data_ORP_0), getString(R.string.data_DO), getString(R.string.data_NH4), getString(R.string.data_ZS)};
        TextView[] text = {pro02, pro03, pro04, pro05, pro06, pro07, pro08, pro09};

        for (int i = 0; i < 8; i++) {
            String s = str.split(",")[i + 1];
            int bend = s.length();
            String t = s + getDeviceUnit(mutilSetUnit[i]) + "\n" + mutilSetName[i];
            if(mutilSetUnit[i] == 0) {
                t = "---" + getDeviceUnit(mutilSetUnit[i]) + "\n" + mutilSetName[i];
                bend = 3;
            }
            SpannableStringBuilder style = new SpannableStringBuilder(t);
            style.setSpan(new AbsoluteSizeSpan(50), 0, bend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text[i].setText(style);
        }

    }

    // 时间显示：
    public void displayTime(String i, String t, TextView interval, TextView test) {
        int bend1 = i.length();
        int bend2 = t.length();
        String strN1 = i + "min" + "\n" + getString(R.string.interval_time);
        String strN2 = t + "min" + "\n" + getString(R.string.test_time);
        SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
        SpannableStringBuilder style2 = new SpannableStringBuilder(strN2);
        style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style2.setSpan(new AbsoluteSizeSpan(50), 0, bend2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ((MyApplication) getActivity().getApplication()).setBasicIntervalTime(i);
        ((MyApplication) getActivity().getApplication()).setBasicTestTime(t);
        interval.setText(style1);
        test.setText(style2);
        globalIntervalTime = Integer.parseInt(i);
        globalTestTime = Integer.parseInt(t);
        ((MyApplication) getActivity().getApplication()).setIntervalTime(Integer.parseInt(i));
        ((MyApplication) getActivity().getApplication()).setTestTime(Integer.parseInt(t));
    }

    // 折线图上限的问题:多参数
    public void changeLineChartUpperBound(int i, Double n) {
        // n:实时数据，i:对应哪一个数据
        // 温度
        if (i == 0 && n > lineChartMax[i]) {
            if (n > 500) {
                lineChartMax[i] = 1000;
            } else if (n > 200) {
                lineChartMax[i] = 500;
            } else if (n > 90) {
                lineChartMax[i] = 100;
            } else if (n > 800) {
                lineChartMax[i] = 90;
            } else if (n > 70) {
                lineChartMax[i] = 80;
            } else if (n > 60) {
                lineChartMax[i] = 70;
            } else if (n > 50) {
                lineChartMax[i] = 60;
            } else if (n > 40) {
                lineChartMax[i] = 50;
            }
        }

        // 数值
        if (i > 0 && n > lineChartMax[i]) {
            if (n > 10000) {
                lineChartMax[i] = 20000;
            } else if (n > 5000) {
                lineChartMax[i] = 10000;
            } else if (n > 3000) {
                lineChartMax[i] = 5000;
            } else if (n > 2000) {
                lineChartMax[i] = 3000;
            } else if (n > 1000) {
                lineChartMax[i] = 2000;
            } else if (n > 500) {
                lineChartMax[i] = 1000;
            } else if (n > 300) {
                lineChartMax[i] = 500;
            } else if (n > 200) {
                lineChartMax[i] = 300;
            } else if (n > 100) {
                lineChartMax[i] = 200;
            } else if (n > 0) {
                lineChartMax[i] = 100;
            }
        }
    }

    // 折线图上限的问题：单参数
    public void changeLineChartUpperLimit(int i, Double d) {
        // i:0数值 1:温度
        if (i == 0 && d > chartMax) {
            if (d > 10000) {
                chartMax = 20000;
            } else if (d > 5000) {
                chartMax = 10000;
            } else if (d > 3000) {
                chartMax = 5000;
            } else if (d > 2000) {
                chartMax = 3000;
            } else if (d > 1000) {
                chartMax = 2000;
            } else if (d > 500) {
                chartMax = 1000;
            } else if (d > 300) {
                chartMax = 500;
            } else if (d > 200) {
                chartMax = 300;
            } else if (d > 100) {
                chartMax = 200;
            } else if (d > 0) {
                chartMax = 100;
            }
        }

        // 数值
        if (i == 1 && d > chartMax0) {
            if (d > 500) {
                chartMax0 = 1000;
            } else if (d > 200) {
                chartMax0 = 500;
            } else if (d > 90) {
                chartMax0 = 100;
            } else if (d > 800) {
                chartMax0 = 90;
            } else if (d > 70) {
                chartMax0 = 80;
            } else if (d > 60) {
                chartMax0 = 70;
            } else if (d > 50) {
                chartMax0 = 60;
            } else if (d > 40) {
                chartMax0 = 50;
            }
        }

        // 特殊情况10：单只COD
        if (i == 10 && d > chartSpecialCODMax) {
            if (d > 10000) {
                chartSpecialCODMax = 20000;
            } else if (d > 5000) {
                chartSpecialCODMax = 10000;
            } else if (d > 3000) {
                chartSpecialCODMax = 5000;
            } else if (d > 2000) {
                chartSpecialCODMax = 3000;
            } else if (d > 1000) {
                chartSpecialCODMax = 2000;
            } else if (d > 500) {
                chartSpecialCODMax = 1000;
            } else if (d > 300) {
                chartSpecialCODMax = 500;
            } else if (d > 200) {
                chartSpecialCODMax = 300;
            } else if (d > 100) {
                chartSpecialCODMax = 200;
            } else if (d > 0) {
                chartSpecialCODMax = 100;
            }
        }
        // 特殊情况16：COD中的BOD
        if (i == 16 && d > chartSpecialBODMax) {
            if (d > 10000) {
                chartSpecialBODMax = 20000;
            } else if (d > 5000) {
                chartSpecialBODMax = 10000;
            } else if (d > 3000) {
                chartSpecialBODMax = 5000;
            } else if (d > 2000) {
                chartSpecialBODMax = 3000;
            } else if (d > 1000) {
                chartSpecialBODMax = 2000;
            } else if (d > 500) {
                chartSpecialBODMax = 1000;
            } else if (d > 300) {
                chartSpecialBODMax = 500;
            } else if (d > 200) {
                chartSpecialBODMax = 300;
            } else if (d > 100) {
                chartSpecialBODMax = 200;
            } else if (d > 0) {
                chartSpecialBODMax = 100;
            }
        }
    }

    // 折线图下限的问题:多参数
    public void changeLineChartLowBound(int i, Double n) {

        if (i == 0 && n < lineChartMin[i]) {
            if (n < -70) {
                lineChartMin[i] = -100;
            } else if (n < -60) {
                lineChartMin[i] = -70;
            } else if (n < -50) {
                lineChartMin[i] = -60;
            } else if (n < -40) {
                lineChartMin[i] = -50;
            } else if (n < -30) {
                lineChartMin[i] = -40;
            } else if (n < -20) {
                lineChartMin[i] = -30;
            } else if (n < -10) {
                lineChartMin[i] = -20;
            } else if (n < 0) {
                lineChartMin[i] = -10;
            } else if (n < 10) {
                lineChartMin[i] = 0;
            }
        }

        if (i > 0 && n < lineChartMin[i]) {
            if (n < -10000) {
                lineChartMin[i] = -20000;
            } else if (n < -5000) {
                lineChartMin[i] = -10000;
            } else if (n < -3000) {
                lineChartMin[i] = -5000;
            } else if (n < -2000) {
                lineChartMin[i] = -3000;
            } else if (n < -1000) {
                lineChartMin[i] = -2000;
            } else if (n < -500) {
                lineChartMin[i] = -100;
            } else if (n < -300) {
                lineChartMin[i] = -500;
            } else if (n < -200) {
                lineChartMin[i] = -300;
            } else if (n < -100) {
                lineChartMin[i] = -200;
            } else if (n < 0) {
                lineChartMin[i] = -100;
            }
        }
    }

    // 折线图下限的问题：单参数
    public void changeLineChartLowLimit(int i, Double d) {
        // 温度
        if (i == 0 && d < chartMin) {
            if (d < -10000) {
                chartMin = -20000;
            } else if (d < -5000) {
                chartMin = -10000;
            } else if (d < -3000) {
                chartMin = -5000;
            } else if (d < -2000) {
                chartMin = -3000;
            } else if (d < -1000) {
                chartMin = -2000;
            } else if (d < -500) {
                chartMin = -100;
            } else if (d < -300) {
                chartMin = -500;
            } else if (d < -200) {
                chartMin = -300;
            } else if (d < -100) {
                chartMin = -200;
            } else if (d < 0) {
                chartMin = -100;
            }
        }

        // 数值
        if (i == 1 && d < chartMin0) {
            if (d < -70) {
                chartMin0 = -100;
            } else if (d < -60) {
                chartMin0 = -70;
            } else if (d < -50) {
                chartMin0 = -60;
            } else if (d < -40) {
                chartMin0 = -50;
            } else if (d < -30) {
                chartMin0 = -40;
            } else if (d < -20) {
                chartMin0 = -30;
            } else if (d < -10) {
                chartMin0 = -20;
            } else if (d < 0) {
                chartMin0 = -10;
            } else if (d < 10) {
                chartMin0 = 0;
            }
        }

        // 特殊情况10：单只COD
        if (i == 10 && d < chartSpecialCODMin) {
            if (d < -10000) {
                chartSpecialCODMin = -20000;
            } else if (d < -5000) {
                chartSpecialCODMin = -10000;
            } else if (d < -3000) {
                chartSpecialCODMin = -5000;
            } else if (d < -2000) {
                chartSpecialCODMin = -3000;
            } else if (d < -1000) {
                chartSpecialCODMin = -2000;
            } else if (d < -500) {
                chartSpecialCODMin = -100;
            } else if (d < -300) {
                chartSpecialCODMin = -500;
            } else if (d < -200) {
                chartSpecialCODMin = -300;
            } else if (d < -100) {
                chartSpecialCODMin = -200;
            } else if (d < 0) {
                chartSpecialCODMin = -100;
            }
        }

        // 特殊情况16：单只COD中的BOD
        if (i == 16 && d < chartSpecialBODMin) {
            if (d < -10000) {
                chartSpecialBODMin = -20000;
            } else if (d < -5000) {
                chartSpecialBODMin = -10000;
            } else if (d < -3000) {
                chartSpecialBODMin = -5000;
            } else if (d < -2000) {
                chartSpecialBODMin = -3000;
            } else if (d < -1000) {
                chartSpecialBODMin = -2000;
            } else if (d < -500) {
                chartSpecialBODMin = -100;
            } else if (d < -300) {
                chartSpecialBODMin = -500;
            } else if (d < -200) {
                chartSpecialBODMin = -300;
            } else if (d < -100) {
                chartSpecialBODMin = -200;
            } else if (d < 0) {
                chartSpecialBODMin = -100;
            }
        }
    }

    // 折线图点数的问题:多参数
    public void changeLineChartLabelBound(int i, Double n) {

    }

    // 电量百分比计算
    public int getElectricQuantity(String s) {
        Double[] standards = {4.182, 4.164, 4.146, 4.128, 4.11, 4.092, 4.074, 4.056, 4.038,
                4.02, 4.002, 3.984, 3.966, 3.948, 3.93, 3.912, 3.894, 3.876, 3.858,
                3.84, 3.822, 3.804, 3.786, 3.768, 3.75, 3.732, 3.714, 3.696, 3.678,
                3.66, 3.642, 3.624, 3.606, 3.588, 3.57, 3.552, 3.534, 3.516, 3.498,
                3.48, 3.462, 3.444, 3.426, 3.408, 3.39, 3.372, 3.354, 3.336, 3.318,
                3.3, 3.282, 3.264, 3.246, 3.228, 3.21, 3.192, 3.174, 3.156, 3.138,
                3.12, 3.102, 3.084, 3.066, 3.048, 3.03, 3.012, 2.994, 2.976, 2.958,
                2.94, 2.922, 2.904, 2.886, 2.868, 2.85, 2.832, 2.814, 2.796, 2.778,
                2.76, 2.742, 2.724, 2.706, 2.688, 2.67, 2.652, 2.634, 2.616, 2.598,
                2.58, 2.562, 2.544, 2.526, 2.508, 2.49, 2.472, 2.454, 2.436, 2.418, 2.35};
        Double d = Double.parseDouble(s);
        for (int i = 0; i <= 99; i++) {
            if (d > standards[i]) {
                return 100 - i;
            }
        }
        return 0;
    }

    // 初始化折线图内容
    public void initLineChart() {
        chartMax = 100;
        chartMin = 0;
        chartLabelCount = 8;
        chartUpperLimit = 0;
        chartLowLimit = 0;
        chartUpperLimit0 = 40;
        chartLowLimit0 = 10;
        chartMax0 = 40;
        chartMin0 = 10;
        lineChartMax = new int[]{40, 100, 100, 100, 14, 100, 20, 100, 100};
        lineChartMin = new int[]{10, 0, 0, 0, 0, 0, 0, 0, 0};
        lineChartLabelCount = new int[]{8, 8, 8, 8, 8, 8, 8, 8, 8};
        chartSpecialCODMax = 100;
        chartSpecialCODMin = 0;
    }

    // 改变单参数折线图的上限
    public void changeLineChartSingleUpperLimit(int type, int i, double d) {
        // 单参数
        if (type == 0) {
            if (d > singleChartMax[i]) {
                if (d > 10000) {
                    singleChartMax[i] = 20000;
                } else if (d > 5000) {
                    singleChartMax[i] = 10000;
                } else if (d > 3000) {
                    singleChartMax[i] = 5000;
                } else if (d > 2000) {
                    singleChartMax[i] = 3000;
                } else if (d > 1000) {
                    singleChartMax[i] = 2000;
                } else if (d > 500) {
                    singleChartMax[i] = 1000;
                } else if (d > 300) {
                    singleChartMax[i] = 500;
                } else if (d > 200) {
                    singleChartMax[i] = 300;
                } else if (d > 100) {
                    singleChartMax[i] = 200;
                } else if (d > 0) {
                    singleChartMax[i] = 100;
                }
            }
        }

        // 多参数
        if (type == 1) {
            if (d > mutilChartMax[i]) {
                if (d > 10000) {
                    mutilChartMax[i] = 20000;
                } else if (d > 5000) {
                    mutilChartMax[i] = 10000;
                } else if (d > 3000) {
                    mutilChartMax[i] = 5000;
                } else if (d > 2000) {
                    mutilChartMax[i] = 3000;
                } else if (d > 1000) {
                    mutilChartMax[i] = 2000;
                } else if (d > 500) {
                    mutilChartMax[i] = 1000;
                } else if (d > 300) {
                    mutilChartMax[i] = 500;
                } else if (d > 200) {
                    mutilChartMax[i] = 300;
                } else if (d > 100) {
                    mutilChartMax[i] = 200;
                } else if (d > 0) {
                    mutilChartMax[i] = 100;
                }
            }
        }

    }

    // 改变单参数折线图的上限2,取8个数据中的值
    public void changeLineChartSingleUpperLimitInEight(int type, int i, double d) {
        // 单参数
        if (type == 0) {
//            if(d > singleChartMax[i]) {
            if (true) {
                if (d > 10000) {
                    singleChartMax[i] = 20000;
                } else if (d > 5000) {
                    singleChartMax[i] = 10000;
                } else if (d > 3000) {
                    singleChartMax[i] = 5000;
                } else if (d > 2000) {
                    singleChartMax[i] = 3000;
                } else if (d > 1000) {
                    singleChartMax[i] = 2000;
                } else if (d > 500) {
                    singleChartMax[i] = 1000;
                } else if (d > 300) {
                    singleChartMax[i] = 500;
                } else if (d > 200) {
                    singleChartMax[i] = 300;
                } else if (d > 100) {
                    singleChartMax[i] = 200;
                } else if (d > 0) {
                    singleChartMax[i] = 100;
                }
            }
        }

        // 多参数
        if (type == 1) {
//            if(d > mutilChartMax[i]) {
            if (d > 10000) {
                mutilChartMax[i] = 20000;
            } else if (d > 5000) {
                mutilChartMax[i] = 10000;
            } else if (d > 3000) {
                mutilChartMax[i] = 5000;
            } else if (d > 2000) {
                mutilChartMax[i] = 3000;
            } else if (d > 1000) {
                mutilChartMax[i] = 2000;
            } else if (d > 500) {
                mutilChartMax[i] = 1000;
            } else if (d > 300) {
                mutilChartMax[i] = 500;
            } else if (d > 200) {
                mutilChartMax[i] = 300;
            } else if (d > 100) {
                mutilChartMax[i] = 200;
            } else if (d > 0) {
                mutilChartMax[i] = 100;
            }
//            }
        }

    }

    // 改变单参数折线图下限
    public void changeLineChartSingleLowerLimit(int type, int i, double n) {
        // 单参数
        if (type == 0) {
            if (n < singleChartMin[i]) {
                if (n < -50000) {
                    singleChartMin[i] = -100000;
                } else if (n < -20000) {
                    singleChartMin[i] = -50000;
                } else if (n < -10000) {
                    singleChartMin[i] = -20000;
                } else if (n < -5000) {
                    singleChartMin[i] = -10000;
                } else if (n < -2000) {
                    singleChartMin[i] = -5000;
                } else if (n < -1000) {
                    singleChartMin[i] = -2000;
                } else if (n < -500) {
                    singleChartMin[i] = -1000;
                } else if (n < -200) {
                    singleChartMin[i] = -500;
                } else if (n < -100) {
                    singleChartMin[i] = -200;
                } else if (n < -60) {
                    singleChartMin[i] = -70;
                } else if (n < -50) {
                    singleChartMin[i] = -60;
                } else if (n < -40) {
                    singleChartMin[i] = -50;
                } else if (n < -30) {
                    singleChartMin[i] = -40;
                } else if (n < -20) {
                    singleChartMin[i] = -30;
                } else if (n < -10) {
                    singleChartMin[i] = -20;
                } else if (n < 0) {
                    singleChartMin[i] = -10;
                } else if (n < 10) {
                    singleChartMin[i] = 0;
                }
            }
        }

        // 多参数
        if (type == 1) {
            if (n < mutilChartMin[i]) {
                if (n < -50000) {
                    singleChartMin[i] = -100000;
                } else if (n < -20000) {
                    singleChartMin[i] = -50000;
                } else if (n < -10000) {
                    singleChartMin[i] = -20000;
                } else if (n < -5000) {
                    singleChartMin[i] = -10000;
                } else if (n < -2000) {
                    singleChartMin[i] = -5000;
                } else if (n < -1000) {
                    singleChartMin[i] = -2000;
                } else if (n < -500) {
                    singleChartMin[i] = -1000;
                } else if (n < -200) {
                    singleChartMin[i] = -500;
                } else if (n < -100) {
                    singleChartMin[i] = -200;
                } else if (n < -60) {
                    singleChartMin[i] = -70;
                } else if (n < -50) {
                    mutilChartMin[i] = -60;
                } else if (n < -40) {
                    mutilChartMin[i] = -50;
                } else if (n < -30) {
                    mutilChartMin[i] = -40;
                } else if (n < -20) {
                    mutilChartMin[i] = -30;
                } else if (n < -10) {
                    mutilChartMin[i] = -20;
                } else if (n < 0) {
                    mutilChartMin[i] = -10;
                } else if (n < 10) {
                    mutilChartMin[i] = 0;
                }
            }
        }
    }

    // 改变单参数折线图下限2,取8个数据中的值
    public void changeLineChartSingleLowerLimitInEight(int type, int i, double n) {
        // 单参数
        if (type == 0) {
//            if(n < singleChartMin[i]) {
            if (true) {
                if (n < -50000) {
                    singleChartMin[i] = -100000;
                } else if (n < -20000) {
                    singleChartMin[i] = -50000;
                } else if (n < -10000) {
                    singleChartMin[i] = -20000;
                } else if (n < -5000) {
                    singleChartMin[i] = -10000;
                } else if (n < -2000) {
                    singleChartMin[i] = -5000;
                } else if (n < -1000) {
                    singleChartMin[i] = -2000;
                } else if (n < -500) {
                    singleChartMin[i] = -1000;
                } else if (n < -200) {
                    singleChartMin[i] = -500;
                } else if (n < -100) {
                    singleChartMin[i] = -200;
                } else if (n < -60) {
                    singleChartMin[i] = -70;
                } else if (n < -50) {
                    singleChartMin[i] = -60;
                } else if (n < -40) {
                    singleChartMin[i] = -50;
                } else if (n < -30) {
                    singleChartMin[i] = -40;
                } else if (n < -20) {
                    singleChartMin[i] = -30;
                } else if (n < -10) {
                    singleChartMin[i] = -20;
                } else if (n < 0) {
                    singleChartMin[i] = -10;
                } else if (n < 10) {
                    singleChartMin[i] = 0;
                }
            }
        }

        // 多参数
        if (type == 1) {
            if (n < mutilChartMin[i]) {
                if (n < -50000) {
                    singleChartMin[i] = -100000;
                } else if (n < -20000) {
                    singleChartMin[i] = -50000;
                } else if (n < -10000) {
                    singleChartMin[i] = -20000;
                } else if (n < -5000) {
                    singleChartMin[i] = -10000;
                } else if (n < -2000) {
                    singleChartMin[i] = -5000;
                } else if (n < -1000) {
                    singleChartMin[i] = -2000;
                } else if (n < -500) {
                    singleChartMin[i] = -1000;
                } else if (n < -200) {
                    singleChartMin[i] = -500;
                } else if (n < -100) {
                    singleChartMin[i] = -200;
                } else if (n < -60) {
                    singleChartMin[i] = -70;
                } else if (n < -50) {
                    mutilChartMin[i] = -60;
                } else if (n < -40) {
                    mutilChartMin[i] = -50;
                } else if (n < -30) {
                    mutilChartMin[i] = -40;
                } else if (n < -20) {
                    mutilChartMin[i] = -30;
                } else if (n < -10) {
                    mutilChartMin[i] = -20;
                } else if (n < 0) {
                    mutilChartMin[i] = -10;
                } else if (n < 10) {
                    mutilChartMin[i] = 0;
                }
            }
        }
    }

    // 电导率1页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayEC1(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeEC1, testTimeEC1);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutEC1.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleEC1.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC1) + ")");
            displayAttributeToCommon();
            dataEC1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataEC1.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(1);
            displayData(data.split(",")[0], data.split(",")[1], dataEC1, dataTempEC1);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutEC1.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 0, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 0, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleEC1.setYAxis(singleChartMax[0], singleChartMin[0], singleChartLabelCount[0]);
            dySingleEC1.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 1, d);
            changeLineChartSingleLowerLimit(0, 1, d);
            // 渲染数据
            dySingleEC1Temp.setYAxis(singleChartMax[1], singleChartMin[1], singleChartLabelCount[1]);
            dySingleEC1Temp.addEntry(d);

            // 存储数据
            downloadRealData(1, data);

        }
    }

    // 电导率2页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayEC2(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(2);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeEC2, testTimeEC2);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutEC2.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleEC2.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_EC2) + ")");
            displayAttributeToCommon();
            dataEC2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataEC2.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(2);
            displayData(data.split(",")[0], data.split(",")[1], dataEC2, dataTempEC2);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutEC2.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 2, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 2, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleEC2.setYAxis(singleChartMax[2], singleChartMin[2], singleChartLabelCount[2]);
            dySingleEC2.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 3, d);
            changeLineChartSingleLowerLimit(0, 3, d);
            // 渲染数据
            dySingleEC2Temp.setYAxis(singleChartMax[3], singleChartMin[3], singleChartLabelCount[3]);
            dySingleEC2Temp.addEntry(d);

            // 存储数据
            downloadRealData(2, data);
        }
    }

    // PH页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayPH(String data) {
        // 首先判断数据长度
        // 带时间长度的数据:数值，温度，电量，间隔时间，测试时间
        // 不带时间长度的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(3);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimePH, testTimePH);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutPH.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSinglePH.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_PH) + ")");

            displayAttributeToCommon();
            dataPH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataPH.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(3);
            displayData(data.split(",")[0], data.split(",")[1], dataPH, dataTempPH);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutPH.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 4, d);
            changeLineChartSingleLowerLimit(0, 4, d);
            // 渲染数据
            dySinglePH.setYAxis(singleChartMax[4], singleChartMin[4], singleChartLabelCount[4]);
            dySinglePH.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 5, d);
            changeLineChartSingleLowerLimit(0, 5, d);
            // 渲染数据
            dySinglePHTemp.setYAxis(singleChartMax[5], singleChartMin[5], singleChartLabelCount[5]);
            dySinglePHTemp.addEntry(d);

            // 存储数据
            downloadRealData(3, data);
        }
    }

    // ORP页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayORP(String data) {
        // 带时间的ORP数据：数值，电量，间隔时间，测试时间
        // 不带时间的ORP数据：数值，电量

        // 带时间参数的先获取时间
        if (data.split(",").length == 4) {
            initCUAL();
//            displayPropertiesBox(4);
            displayTime(data.split(",")[2], data.split(",")[3], intervalTimeORP, testTimeORP);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutORP.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_ORP) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleORP.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataORP.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataORP.setTextColor(Color.BLUE);
        }

        // 满足格式的渲染数据
        if (data.split(",").length == 4 || data.split(",").length == 2) {
            // 将数据显示在基础数据输入框中
//            displayPropertiesBox(4);
            displayData(data.split(",")[0], dataORP);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutORP.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);

            String n1 = data.split(",")[0];
            int bend1 = n1.length();
            String strN1 = n1 + "\n"
                    + getDeviceUnit(((MyApplication) getActivity().getApplication()).getBasicDeviceType());
            SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
            style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            dataORP.setText(style1);

            // 电量显示
            displayElectricData(data.split(",")[1]);

            // 数据的折线图渲染
//            Double d = Double.parseDouble(data.split(",")[0]);
//            changeLineChartUpperLimit(0, d);
//            changeLineChartLowLimit(0, d);
//            if(dy == null) {
//                initWay1(0);
//            }
//            dy.setYAxis(chartMax, chartMin, chartLabelCount);
//            dy.addEntry(d);

            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 6, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 6, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleORP.setYAxis(singleChartMax[6], singleChartMin[6], singleChartLabelCount[6]);
            dySingleORP.addEntry(d);

            // 存储数据
            downloadRealData(4, data);
        }
    }

    // 溶解氧页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayRDO(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(5);

            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeRDO, testTimeRDO);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutRDO.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_DO) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleRDO.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataRDO.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataRDO.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(5);
            displayData(data.split(",")[0], data.split(",")[1], dataRDO, dataTempRDO);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutRDO.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 7, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 7, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleRDO.setYAxis(singleChartMax[7], singleChartMin[7], singleChartLabelCount[7]);
            dySingleRDO.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 8, d);
            changeLineChartSingleLowerLimit(0, 8, d);
            // 渲染数据
            dySingleRDOTemp.setYAxis(singleChartMax[8], singleChartMin[8], singleChartLabelCount[8]);
            dySingleRDOTemp.addEntry(d);

            // 存储数据
            downloadRealData(5, data);
        }
    }

    // 氨氮页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayNHN(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(6);
            // 初始化数量为8个
            initCUAL();

            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeNHN, testTimeNHN);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutNHN.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleNHN.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_NH) + ")");
            displayAttributeToCommon();
            dataNHN.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataNHN.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(6);
            displayData(data.split(",")[0], data.split(",")[1], dataNHN, dataTempNHN);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutNHN.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(0, 9, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimitInEight(0, 9, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleNHN.setYAxis(singleChartMax[9], singleChartMin[9], singleChartLabelCount[9]);
            dySingleNHN.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 10, d);
            changeLineChartSingleLowerLimit(0, 10, d);
            // 渲染数据
            dySingleNHNTemp.setYAxis(singleChartMax[10], singleChartMin[10], singleChartLabelCount[10]);
            dySingleNHNTemp.addEntry(d);

            // 存储数据
            downloadRealData(6, data);
        }
    }

    // 浊度页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayZS(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(7);
            // 初始化数量为8个
            initCUAL();

            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeZS, testTimeZS);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutZS.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleZS.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_ZS) + ")");
            displayAttributeToCommon();
            dataZS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataZS.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(7);
            displayData(data.split(",")[0], data.split(",")[1], dataZS, dataTempZS);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutZS.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(0, 11, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimitInEight(0, 11, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleZS.setYAxis(singleChartMax[11], singleChartMin[11], singleChartLabelCount[11]);
            dySingleZS.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 12, d);
            changeLineChartSingleLowerLimit(0, 12, d);
            // 渲染数据
            dySingleZSTemp.setYAxis(singleChartMax[12], singleChartMin[12], singleChartLabelCount[12]);
            dySingleZSTemp.addEntry(d);

            // 存储数据
            downloadRealData(7, data);
        }
    }

    // 盐度页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displaySAL(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量
        System.out.println(data.split(",").length);
        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(8);
            // 初始化数量为8个
            initCUAL();
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeSAL, testTimeSAL);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutSAL.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleSAL.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_salinity) + ")");
            displayAttributeToCommon();
            dataSAL.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataSAL.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(8);
            displayData(data.split(",")[0], data.split(",")[1], dataSAL, dataTempSAL);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutSAL.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(0, 13, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimitInEight(0, 13, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleSAL.setYAxis(singleChartMax[13], singleChartMin[13], singleChartLabelCount[13]);
            dySingleSAL.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 14, d);
            changeLineChartSingleLowerLimit(0, 14, d);
            // 渲染数据
            dySingleSALTemp.setYAxis(singleChartMax[14], singleChartMin[14], singleChartLabelCount[14]);
            dySingleSALTemp.addEntry(d);

            // 存储数据
            downloadRealData(8, data);

        }
    }

    // COD页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayCOD(String data) {
        // 带时间的COD数据：数值，温度，浊度，BOD，电量，间隔时间，测试时间
        // 不带时间的COD数据：数值，温度，浊度，BOD，电量

        // 带时间参数的先获取时间
        if (data.split(",").length == 7) {
            initCUAL();
//            displayPropertiesBox(9);

            displayTime(data.split(",")[5], data.split(",")[6], intervalTimeCOD, testTimeCOD);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutCOD.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleCOD.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_COD_0) + ")");

            // 数据框样式
            displayAttributeToCommon();
            dataCOD.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataCOD.setTextColor(Color.BLUE);
        }

        // 满足格式的渲染数据
        if (data.split(",").length == 7 || data.split(",").length == 5) {
//            displayPropertiesBox(9);
            // 将数据显示在基础数据输入框中
//            String n1 = data.split(",")[0]; // COD
//            String n2 = data.split(",")[1]; // 温度
//            String n3 = data.split(",")[2]; // 浊度
//            String n4 = data.split(",")[3]; // BOD
//            int bend1 = n1.length();
//            int bend2 = n2.length();
//            int bend3 = n3.length();
//            int bend4 = n4.length();
//            String strN1 = n1 + "\n"
//                    + getDeviceUnit(((MyApplication) getActivity().getApplication()).getBasicDeviceType());
//            String strN2 = n2 + "℃" + "\n" + getString(R.string.data_temperature);
//            String strN3 = n3 + "\n" + getDeviceUnit(7);
//            String strN4 = n4 + "\n" + "mg/L";
//            SpannableStringBuilder style1 = new SpannableStringBuilder(strN1);
//            SpannableStringBuilder style2 = new SpannableStringBuilder(strN2);
//            SpannableStringBuilder style3 = new SpannableStringBuilder(strN3);
//            SpannableStringBuilder style4 = new SpannableStringBuilder(strN4);
//            style1.setSpan(new AbsoluteSizeSpan(50), 0, bend1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            style2.setSpan(new AbsoluteSizeSpan(50), 0, bend2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            style3.setSpan(new AbsoluteSizeSpan(50), 0, bend3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            style4.setSpan(new AbsoluteSizeSpan(50), 0, bend4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            dataCOD.setText(style1);
//            dataTempCOD.setText(style2);
//            dataCODZS.setText(style3);
//            dataCODBOD.setText(style4);

            displayData(data.split(",")[0], data.split(",")[1], data.split(",")[2], data.split(",")[3], dataCOD, dataTempCOD, dataCODZS, dataCODBOD);

            // 电量显示
            displayElectricData(data.split(",")[4]);


            // COD数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            changeLineChartSingleUpperLimit(0, 15, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 15, getMin(controlUpperAndLowerData));
            dySingleCOD.setYAxis(singleChartMax[15], singleChartMin[15], singleChartLabelCount[15]);
            dySingleCOD.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            changeLineChartSingleUpperLimit(0, 16, d);
            changeLineChartSingleLowerLimit(0, 16, d);
            dySingleCODTemp.setYAxis(singleChartMax[16], singleChartMin[16], singleChartLabelCount[16]);
            dySingleCODTemp.addEntry(d);

            // COD内置浊度
            d = Double.parseDouble(data.split(",")[2]);
            updateCUAL(d, 2);
            changeLineChartSingleUpperLimit(0, 17, getMax(controlUpperAndLowerZS));
            changeLineChartSingleLowerLimit(0, 17, getMin(controlUpperAndLowerZS));
            dySingleCODZS.setYAxis(singleChartMax[17], singleChartMin[17], singleChartLabelCount[17]);
            dySingleCODZS.addEntry(d);

            // COD内置BOD
            d = Double.parseDouble(data.split(",")[3]);
            updateCUAL(d, 1);
            changeLineChartSingleUpperLimit(0, 18, getMax(controlUpperAndLowerBOD));
            changeLineChartSingleLowerLimit(0, 18, getMin(controlUpperAndLowerBOD));
            dySingleCODBOD.setYAxis(singleChartMax[18], singleChartMin[18], singleChartLabelCount[18]);
            dySingleCODBOD.addEntry(d);

            // 存储数据
            downloadRealData(9, data);

        }
    }

    // 余氯页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayRC(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(10);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeRC, testTimeRC);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutRC.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_residual_chlorine) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleRC.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataRC.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataRC.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(10);
            displayData(data.split(",")[0], data.split(",")[1], dataRC, dataTempRC);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutRC.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 19, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 19, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleRC.setYAxis(singleChartMax[19], singleChartMin[19], singleChartLabelCount[19]);
            dySingleRC.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 20, d);
            changeLineChartSingleLowerLimit(0, 20, d);
            // 渲染数据
            dySingleRCTemp.setYAxis(singleChartMax[20], singleChartMin[20], singleChartLabelCount[20]);
            dySingleRCTemp.addEntry(d);

            // 存储数据
            downloadRealData(10, data);
        }
    }

    // 叶绿素页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayCH(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(11);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeCH, testTimeCH);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutCH.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Chlorophyl) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleCH.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataCH.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataCH.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(11);
            displayData(data.split(",")[0], data.split(",")[1], dataCH, dataTempCH);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutCH.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 21, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 21, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleCH.setYAxis(singleChartMax[21], singleChartMin[21], singleChartLabelCount[21]);
            dySingleCH.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 22, d);
            changeLineChartSingleLowerLimit(0, 22, d);
            // 渲染数据
            dySingleCHTemp.setYAxis(singleChartMax[22], singleChartMin[22], singleChartLabelCount[22]);
            dySingleCHTemp.addEntry(d);

            // 存储数据
            downloadRealData(11, data);
        }
    }

    // 蓝绿藻页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayCY(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(12);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeCY, testTimeCY);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutCY.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_blue_green_algae) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleCY.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataCY.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataCY.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(12);
            displayData(data.split(",")[0], data.split(",")[1], dataCY, dataTempCY);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutCY.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 23, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 23, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleCY.setYAxis(singleChartMax[23], singleChartMin[23], singleChartLabelCount[23]);
            dySingleCY.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 24, d);
            changeLineChartSingleLowerLimit(0, 24, d);
            // 渲染数据
            dySingleCYTemp.setYAxis(singleChartMax[24], singleChartMin[24], singleChartLabelCount[24]);
            dySingleCYTemp.addEntry(d);

            // 存储数据
            downloadRealData(12, data);
        }
    }

    // 透明度页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayTSS(String data) {
        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(13);

            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeTSS, testTimeTSS);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutTSS.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleTSS.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Transparency) + ")");
            displayAttributeToCommon();
            dataTSS.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataTSS.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(13);
            displayData(data.split(",")[0], data.split(",")[1], dataTSS, dataTempTSS);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutTSS.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 25, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 25, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleTSS.setYAxis(singleChartMax[25], singleChartMin[25], singleChartLabelCount[25]);
            dySingleTSS.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 26, d);
            changeLineChartSingleLowerLimit(0, 26, d);
            // 渲染数据
            dySingleTSSTemp.setYAxis(singleChartMax[26], singleChartMin[26], singleChartLabelCount[26]);
            dySingleTSSTemp.addEntry(d);

            // 存储数据
            downloadRealData(13, data);
        }
    }

    // 悬浮物页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayTSM(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(14);
            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeTSM, testTimeTSM);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutTSM.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_Suspended_Solids) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleTSM.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataTSM.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataTSM.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(14);
            displayData(data.split(",")[0], data.split(",")[1], dataTSM, dataTempTSM);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutTSM.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 27, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 27, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleTSM.setYAxis(singleChartMax[27], singleChartMin[27], singleChartLabelCount[27]);
            dySingleTSM.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 28, d);
            changeLineChartSingleLowerLimit(0, 28, d);
            // 渲染数据
            dySingleTSMTemp.setYAxis(singleChartMax[28], singleChartMin[28], singleChartLabelCount[28]);
            dySingleTSMTemp.addEntry(d);

            // 存储数据
            downloadRealData(14, data);
        }
    }

    // 水中油页面渲染
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayOW(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 5) {
            initCUAL();
            // 将时间数据显示在时间框中
//            displayTimeData(0, data);
//            displayPropertiesBox(15);

            displayTime(data.split(",")[3], data.split(",")[4], intervalTimeOW, testTimeOW);
            // 隐藏所有的数据框
            hideDataView();
            singleLayoutOW.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + getString(R.string.data_oil_in_water) + ")");
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartSingleOW.setVisibility(View.VISIBLE);
            displayAttributeToCommon();
            dataOW.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            dataOW.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 3 || data.split(",").length == 5) {
            // 将数据显示在基础数据输入框中
//            displayProData(0, data);
//            displayPropertiesBox(15);
            displayData(data.split(",")[0], data.split(",")[1], dataOW, dataTempOW);

            // 隐藏所有的数据框
            hideDataView();
            singleLayoutOW.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[2]);

            // 数据的折线图渲染
//            displayLineChartData(0, data);
            // 获取数值
            Double d = Double.parseDouble(data.split(",")[0]);
            updateCUAL(d, 0);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 29, getMax(controlUpperAndLowerData));
            changeLineChartSingleLowerLimit(0, 29, getMin(controlUpperAndLowerData));
            // 渲染数据
            dySingleOW.setYAxis(singleChartMax[29], singleChartMin[29], singleChartLabelCount[29]);
            dySingleOW.addEntry(d);

            // 温度
            d = Double.parseDouble(data.split(",")[1]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimit(0, 30, d);
            changeLineChartSingleLowerLimit(0, 30, d);
            // 渲染数据
            dySingleOWTemp.setYAxis(singleChartMax[30], singleChartMin[30], singleChartLabelCount[30]);
            dySingleOWTemp.addEntry(d);

            // 存储数据
            downloadRealData(15, data);
        }
    }

    // 多参数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void displayMUL(String data) {
        // 带时间的数据：数值，温度，电量，间隔时间，测试时间
        // 不带时间的数据：数值，温度，电量

        // 带时间参数先显示时间
        if (data.split(",").length == 12) {
            initCUAL();
//            if(dy0.getLineDataNum() > 0) {
//                resetLineChart(lineChart0);
//                resetLineChart(lineChartCOD);
//                resetLineChart(lineChartCODNT);
//                resetLineChart(lineChartEC);
//                resetLineChart(lineChartPH);
//                dyPH.destroyChart();
//                resetLineChart(lineChartORP);
//                resetLineChart(lineChartDO);
//                resetLineChart(lineChartNH);
//                resetLineChart(lineChartNT);
//            }
            // 将时间数据显示在时间框中
//            displayTimeData(1, data);
//            displayPropertiesBox(999);
            displayTime(data.split(",")[10], data.split(",")[11], pro10, pro11);
            // 隐藏所有的数据框
            hideDataView();
            basicData2.setVisibility(View.VISIBLE);
            proData.setVisibility(View.VISIBLE);
//            displayPropertiesBox(1);
            // 隐藏所有的折线图
            hideLineChart();
            lineChartCOD.setVisibility(View.VISIBLE);
            linechartName.setText(getString(R.string.real_data) + "(" + mutilSetName[0] + ")");
            displayAttributeToCommon();
            pro02.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
            pro02.setTextColor(Color.BLUE);
        }

        // 显示数值框和折线图数据框
        if (data.split(",").length == 12 || data.split(",").length == 10) {
            // 将数据显示在基础数据输入框中
//            displayProData(1, data);
//            displayPropertiesBox(999);
            displayData(data);

            hideDataView();
            basicData2.setVisibility(View.VISIBLE);
            proData.setVisibility(View.VISIBLE);

            // 电量显示
            displayElectricData(data.split(",")[9]);

            // 数据的折线图渲染
//            displayLineChartData(1, data);
            // 温度
            Double d = Double.parseDouble(data.split(",")[0]);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 0, d);
            changeLineChartSingleLowerLimitInEight(1, 0, d);
            // 渲染数据
            dy0.setYAxis(mutilChartMax[0], mutilChartMin[0], mutilChartLabelCount[0]);
            dy0.addEntry(d);

            // COD
            d = Double.parseDouble(data.split(",")[1]);
            updateCUAL(d, 3);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 1, getMax(controlUpperAndLowerMutilCOD));
            changeLineChartSingleLowerLimitInEight(1, 1, getMin(controlUpperAndLowerMutilCOD));
            // 渲染数据
            dyCOD.setYAxis(mutilChartMax[1], mutilChartMin[1], mutilChartLabelCount[1]);
            dyCOD.addEntry(d);

            // COD内置浊度
            d = Double.parseDouble(data.split(",")[2]);
            updateCUAL(d, 4);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 2, getMax(controlUpperAndLowerMutilCODNT));
            changeLineChartSingleLowerLimitInEight(1, 2, getMin(controlUpperAndLowerMutilCODNT));
            // 渲染数据
            dyCODNT.setYAxis(mutilChartMax[2], mutilChartMin[2], mutilChartLabelCount[2]);
            dyCODNT.addEntry(d);

            // 电导率/盐度
            d = Double.parseDouble(data.split(",")[3]);
            updateCUAL(d, 5);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 3, getMax(controlUpperAndLowerMutilEC));
            changeLineChartSingleLowerLimitInEight(1, 3, getMin(controlUpperAndLowerMutilEC));
            // 渲染数据
            dyEC.setYAxis(mutilChartMax[3], mutilChartMin[3], mutilChartLabelCount[3]);
            dyEC.addEntry(d);

            // PH
            d = Double.parseDouble(data.split(",")[4]);
            updateCUAL(d, 6);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 4, getMax(controlUpperAndLowerMutilPH));
            changeLineChartSingleLowerLimitInEight(1, 4, getMin(controlUpperAndLowerMutilPH));
            // 渲染数据
            dyPH.setYAxis(mutilChartMax[4], mutilChartMin[4], mutilChartLabelCount[4]);
            dyPH.addEntry(d);

            // ORP
            d = Double.parseDouble(data.split(",")[5]);
            updateCUAL(d, 7);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 5, getMax(controlUpperAndLowerMutilORP));
            changeLineChartSingleLowerLimitInEight(1, 5, getMin(controlUpperAndLowerMutilORP));
            // 渲染数据
            dyORP.setYAxis(mutilChartMax[5], mutilChartMin[5], mutilChartLabelCount[5]);
            dyORP.addEntry(d);

            // 溶解氧
            d = Double.parseDouble(data.split(",")[6]);
            updateCUAL(d, 8);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 6, getMax(controlUpperAndLowerMutilDO));
            changeLineChartSingleLowerLimitInEight(1, 6, getMin(controlUpperAndLowerMutilDO));
            // 渲染数据
            dyDO.setYAxis(mutilChartMax[6], mutilChartMin[6], mutilChartLabelCount[6]);
            dyDO.addEntry(d);

            // NHN
            d = Double.parseDouble(data.split(",")[7]);
            updateCUAL(d, 9);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 7, getMax(controlUpperAndLowerMutilNH));
            changeLineChartSingleLowerLimitInEight(1, 7, getMin(controlUpperAndLowerMutilNH));
            // 渲染数据
            dyNH.setYAxis(mutilChartMax[7], mutilChartMin[7], mutilChartLabelCount[7]);
            dyNH.addEntry(d);

            // 浊度
            d = Double.parseDouble(data.split(",")[8]);
            updateCUAL(d, 10);
            // 折线图上下限进行判断
            changeLineChartSingleUpperLimitInEight(1, 8, getMax(controlUpperAndLowerMutilNT));
            changeLineChartSingleLowerLimitInEight(1, 8, getMin(controlUpperAndLowerMutilNT));
            // 渲染数据
            dyNT.setYAxis(mutilChartMax[8], mutilChartMin[8], mutilChartLabelCount[8]);
            dyNT.addEntry(d);

            // 存储数据
            downloadRealData(999, data);
        }
    }

    /**
     * 初始化折线图
     */
    public void initDyLineChart() {
        System.out.println("初始化折线图");
        // 单参数数据上下限：未连接，电导率1，电导率2，PH，ORP，溶解氧，氨氮，浊度，盐度，化学需氧量COD，余氯，叶绿素，蓝绿藻，
        // 透明度，悬浮物，水中油,浊度(COD),BOD

        int[] colors = {Color.RED, Color.LTGRAY, Color.YELLOW, Color.GRAY, Color.GREEN, Color.DKGRAY, Color.CYAN, Color.BLUE, Color.BLACK};

//        for(int i = 0; i < dySingles.length; i ++) {
//            dySingles[i] = new DyLineChartUtils(lcSingles[i], "", colors[i % colors.length], getContext());
//            dySingles[i].setYAxis(singleChartMax[i], singleChartMin[i], singleChartLabelCount[i]);
//            System.out.println("单参数折线图初始化" + i);
//        }

        dySingleEC1 = new DyLineChartUtils(lineChartSingleEC1, "123", colors[0], getContext());
        dySingleEC1.setYAxis(singleChartMax[0], singleChartMin[0], singleChartLabelCount[0]);

        dySingleEC1Temp = new DyLineChartUtils(lineChartSingleEC1Temp, "", colors[1], getContext());
        dySingleEC1Temp.setYAxis(singleChartMax[1], singleChartMin[1], singleChartLabelCount[1]);

        dySingleEC2 = new DyLineChartUtils(lineChartSingleEC2, "", colors[2], getContext());
        dySingleEC2.setYAxis(singleChartMax[2], singleChartMin[2], singleChartLabelCount[2]);

        dySingleEC2Temp = new DyLineChartUtils(lineChartSingleEC2Temp, "", colors[3], getContext());
        dySingleEC2Temp.setYAxis(singleChartMax[3], singleChartMin[3], singleChartLabelCount[3]);

        dySinglePH = new DyLineChartUtils(lineChartSinglePH, "", colors[4], getContext());
        dySinglePH.setYAxis(singleChartMax[4], singleChartMin[4], singleChartLabelCount[4]);

        dySinglePHTemp = new DyLineChartUtils(lineChartSinglePHTemp, "", colors[5], getContext());
        dySinglePHTemp.setYAxis(singleChartMax[5], singleChartMin[5], singleChartLabelCount[5]);

        dySingleORP = new DyLineChartUtils(lineChartSingleORP, "", colors[6], getContext());
        dySingleORP.setYAxis(singleChartMax[6], singleChartMin[6], singleChartLabelCount[6]);

        dySingleRDO = new DyLineChartUtils(lineChartSingleRDO, "", colors[7], getContext());
        dySingleRDO.setYAxis(singleChartMax[7], singleChartMin[7], singleChartLabelCount[7]);

        dySingleRDOTemp = new DyLineChartUtils(lineChartSingleRDOTemp, "", colors[8], getContext());
        dySingleRDOTemp.setYAxis(singleChartMax[8], singleChartMin[8], singleChartLabelCount[8]);

        dySingleNHN = new DyLineChartUtils(lineChartSingleNHN, "", colors[0], getContext());
        dySingleNHN.setYAxis(singleChartMax[9], singleChartMin[9], singleChartLabelCount[9]);

        dySingleNHNTemp = new DyLineChartUtils(lineChartSingleNHNTemp, "", colors[1], getContext());
        dySingleNHNTemp.setYAxis(singleChartMax[10], singleChartMin[10], singleChartLabelCount[10]);

        dySingleZS = new DyLineChartUtils(lineChartSingleZS, "", colors[2], getContext());
        dySingleZS.setYAxis(singleChartMax[11], singleChartMin[11], singleChartLabelCount[11]);

        dySingleZSTemp = new DyLineChartUtils(lineChartSingleZSTemp, "", colors[3], getContext());
        dySingleZSTemp.setYAxis(singleChartMax[12], singleChartMin[12], singleChartLabelCount[12]);

        dySingleSAL = new DyLineChartUtils(lineChartSingleSAL, "", colors[4], getContext());
        dySingleSAL.setYAxis(singleChartMax[13], singleChartMin[13], singleChartLabelCount[13]);

        dySingleSALTemp = new DyLineChartUtils(lineChartSingleSALTemp, "", colors[5], getContext());
        dySingleSALTemp.setYAxis(singleChartMax[14], singleChartMin[14], singleChartLabelCount[14]);

        dySingleCOD = new DyLineChartUtils(lineChartSingleCOD, "", colors[6], getContext());
        dySingleCOD.setYAxis(singleChartMax[15], singleChartMin[15], singleChartLabelCount[15]);

        dySingleCODTemp = new DyLineChartUtils(lineChartSingleCODTemp, "", colors[7], getContext());
        dySingleCODTemp.setYAxis(singleChartMax[16], singleChartMin[16], singleChartLabelCount[16]);

        dySingleCODZS = new DyLineChartUtils(lineChartSingleCODZS, "", colors[8], getContext());
        dySingleCODZS.setYAxis(singleChartMax[17], singleChartMin[17], singleChartLabelCount[17]);

        dySingleCODBOD = new DyLineChartUtils(lineChartSingleCODBOD, "", colors[0], getContext());
        dySingleCODBOD.setYAxis(singleChartMax[18], singleChartMin[18], singleChartLabelCount[18]);

        dySingleRC = new DyLineChartUtils(lineChartSingleRC, "", colors[1], getContext());
        dySingleRC.setYAxis(singleChartMax[19], singleChartMin[19], singleChartLabelCount[19]);

        dySingleRCTemp = new DyLineChartUtils(lineChartSingleRCTemp, "", colors[2], getContext());
        dySingleRCTemp.setYAxis(singleChartMax[20], singleChartMin[20], singleChartLabelCount[20]);

        dySingleCH = new DyLineChartUtils(lineChartSingleCH, "", colors[3], getContext());
        dySingleCH.setYAxis(singleChartMax[21], singleChartMin[21], singleChartLabelCount[21]);

        dySingleCHTemp = new DyLineChartUtils(lineChartSingleCHTemp, "", colors[4], getContext());
        dySingleCHTemp.setYAxis(singleChartMax[22], singleChartMin[22], singleChartLabelCount[22]);

        dySingleCY = new DyLineChartUtils(lineChartSingleCY, "", colors[5], getContext());
        dySingleCY.setYAxis(singleChartMax[23], singleChartMin[23], singleChartLabelCount[23]);

        dySingleCYTemp = new DyLineChartUtils(lineChartSingleCYTemp, "", colors[6], getContext());
        dySingleCYTemp.setYAxis(singleChartMax[24], singleChartMin[24], singleChartLabelCount[24]);

        dySingleTSS = new DyLineChartUtils(lineChartSingleTSS, "", colors[7], getContext());
        dySingleTSS.setYAxis(singleChartMax[25], singleChartMin[25], singleChartLabelCount[25]);

        dySingleTSSTemp = new DyLineChartUtils(lineChartSingleTSSTemp, "", colors[8], getContext());
        dySingleTSSTemp.setYAxis(singleChartMax[26], singleChartMin[26], singleChartLabelCount[26]);

        dySingleTSM = new DyLineChartUtils(lineChartSingleTSM, "", colors[8], getContext());
        dySingleTSM.setYAxis(singleChartMax[27], singleChartMin[27], singleChartLabelCount[27]);

        dySingleTSMTemp = new DyLineChartUtils(lineChartSingleTSMTemp, "", colors[0], getContext());
        dySingleTSMTemp.setYAxis(singleChartMax[28], singleChartMin[28], singleChartLabelCount[28]);

        dySingleOW = new DyLineChartUtils(lineChartSingleOW, "", colors[1], getContext());
        dySingleOW.setYAxis(singleChartMax[29], singleChartMin[29], singleChartLabelCount[29]);

        dySingleOWTemp = new DyLineChartUtils(lineChartSingleOWTemp, "", colors[2], getContext());
        dySingleOWTemp.setYAxis(singleChartMax[30], singleChartMin[30], singleChartLabelCount[30]);


        // 单参数
//        for(int i = 0; i < dySingles.length; i ++) {
//            dySingles[i] = new DyLineChartUtils(lcSingles[i], "", colors[i % colors.length],getContext());
//            dySingles[i].setYAxis(singleChartMax[i], singleChartMin[i], singleChartLabelCount[i]);
//            System.out.println("单参数折线图初始化" + i);
//        }

        // 多参数
//        for(int i = 0; i < dys.length; i ++) {
//            dys[i] = new DyLineChartUtils(lcMutils[i], "", colors[i % colors.length],getContext());
//            dys[i].setYAxis(mutilChartMax[i], mutilChartMin[i], mutilChartLabelCount[i]);
//            System.out.println("多参数折线图初始化" + i);
//        }


        dyDemo = new DyLineChartUtils(lineChartDemo, "", colors[7], getContext());
        dyDemo.setYAxis(100, 0, mutilChartLabelCount[0]);

        dy0 = new DyLineChartUtils(lineChart0, "", colors[3], getContext());
        dy0.setYAxis(mutilChartMax[0], mutilChartMin[0], mutilChartLabelCount[0]);

        dyCOD = new DyLineChartUtils(lineChartCOD, "", colors[4], getContext());
        dyCOD.setYAxis(mutilChartMax[1], mutilChartMin[1], mutilChartLabelCount[1]);

        dyCODNT = new DyLineChartUtils(lineChartCODNT, "", colors[5], getContext());
        dyCODNT.setYAxis(mutilChartMax[2], mutilChartMin[2], mutilChartLabelCount[2]);

        dyEC = new DyLineChartUtils(lineChartEC, "", colors[6], getContext());
        dyEC.setYAxis(mutilChartMax[3], mutilChartMin[3], mutilChartLabelCount[3]);

        dyPH = new DyLineChartUtils(lineChartPH, "", colors[7], getContext());
        dyPH.setYAxis(mutilChartMax[4], mutilChartMin[4], mutilChartLabelCount[4]);

        dyORP = new DyLineChartUtils(lineChartORP, "", colors[8], getContext());
        dyORP.setYAxis(mutilChartMax[5], mutilChartMin[5], mutilChartLabelCount[5]);

        dyDO = new DyLineChartUtils(lineChartDO, "", colors[0], getContext());
        dyDO.setYAxis(mutilChartMax[6], mutilChartMin[6], mutilChartLabelCount[6]);

        dyNH = new DyLineChartUtils(lineChartNH, "", colors[1], getContext());
        dyNH.setYAxis(mutilChartMax[7], mutilChartMin[7], mutilChartLabelCount[7]);

        dyNT = new DyLineChartUtils(lineChartNT, "", colors[2], getContext());
        dyNT.setYAxis(mutilChartMax[8], mutilChartMin[8], mutilChartLabelCount[8]);
    }

    // 折线图数据规整
    public void resetLineChart(LineChart lineChart) {
//        lineChart.fitScreen();
//        lineChart.clear();

    }

    /**
     * 隐藏所有的折线图
     */
    public void hideLineChart() {
        lineChartDemo.setVisibility(View.GONE);
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

    // 隐藏所有的数据框
    public void hideDataView() {
        singleLayoutEC1.setVisibility(View.GONE);
        singleLayoutEC2.setVisibility(View.GONE);
        singleLayoutPH.setVisibility(View.GONE);
        singleLayoutORP.setVisibility(View.GONE);
        singleLayoutRDO.setVisibility(View.GONE);
        singleLayoutNHN.setVisibility(View.GONE);
        singleLayoutZS.setVisibility(View.GONE);
        singleLayoutSAL.setVisibility(View.GONE);
        singleLayoutCOD.setVisibility(View.GONE);
        singleLayoutRC.setVisibility(View.GONE);
        singleLayoutCH.setVisibility(View.GONE);
        singleLayoutCY.setVisibility(View.GONE);
        singleLayoutTSS.setVisibility(View.GONE);
        singleLayoutTSM.setVisibility(View.GONE);
        singleLayoutOW.setVisibility(View.GONE);

        basicData1.setVisibility(View.GONE);
        basicData2.setVisibility(View.GONE);
        proData.setVisibility(View.GONE);
    }

    // 数据框变为默认的样式
    public void displayAttributeToCommon() {
        dataEC1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataEC1.setTextColor(Color.BLACK);
        dataTempEC1.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempEC1.setTextColor(Color.BLACK);
        dataEC2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataEC2.setTextColor(Color.BLACK);
        dataTempEC2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempEC2.setTextColor(Color.BLACK);
        dataPH.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataPH.setTextColor(Color.BLACK);
        dataTempPH.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempPH.setTextColor(Color.BLACK);
        dataORP.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataORP.setTextColor(Color.BLACK);
        dataRDO.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataRDO.setTextColor(Color.BLACK);
        dataTempRDO.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempRDO.setTextColor(Color.BLACK);
        dataNHN.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataNHN.setTextColor(Color.BLACK);
        dataTempNHN.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempNHN.setTextColor(Color.BLACK);
        dataZS.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataZS.setTextColor(Color.BLACK);
        dataTempZS.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempZS.setTextColor(Color.BLACK);
        dataSAL.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataSAL.setTextColor(Color.BLACK);
        dataTempSAL.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempSAL.setTextColor(Color.BLACK);
        dataCOD.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataCOD.setTextColor(Color.BLACK);
        dataCODZS.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataCODZS.setTextColor(Color.BLACK);
        dataCODBOD.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataCODBOD.setTextColor(Color.BLACK);
        dataTempCOD.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempCOD.setTextColor(Color.BLACK);
        dataRC.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataRC.setTextColor(Color.BLACK);
        dataTempRC.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempRC.setTextColor(Color.BLACK);
        dataCH.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataCH.setTextColor(Color.BLACK);
        dataTempCH.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempCH.setTextColor(Color.BLACK);
        dataCY.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataCY.setTextColor(Color.BLACK);
        dataTempCY.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempCY.setTextColor(Color.BLACK);
        dataTSS.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTSS.setTextColor(Color.BLACK);
        dataTempTSS.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempTSS.setTextColor(Color.BLACK);
        dataTSM.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTSM.setTextColor(Color.BLACK);
        dataTempTSM.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempTSM.setTextColor(Color.BLACK);
        dataOW.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataOW.setTextColor(Color.BLACK);
        dataTempOW.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
        dataTempOW.setTextColor(Color.BLACK);

        TextView[] pro = {pro01, pro02, pro03, pro04, pro05, pro06, pro07, pro08, pro09};
        for (int i = 0; i < pro.length; i++) {
            pro[i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));//取消加粗
            pro[i].setTextColor(Color.BLACK);
        }
    }

    public void jumpTempModify() {
        if (!globalDeviceIsConnect) {
            return;
        }
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.select_skip_order_time))
                .setNegativeButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), TimeOrderSetActivity.class);
                        startActivity(intent);
                    }
                }).setPositiveButton(getString(R.string.cancel), null)
                .setCancelable(true)
                .show();
    }

    /**
     * 计算8个数据的上限计算
     * @param arrays 需要计算的数组
     * @return 最大值
     */
    public double getMax(double[] arrays) {
        double max = arrays[0];
        for (int i = 1; i < 9; i++) {
            if (max < arrays[i]) {
                max = arrays[i];
            }
        }
        // System.out.println("最大值：" + arrays[0]+","+arrays[1]+","+arrays[2]+","+arrays[3]+","+arrays[4]+","+arrays[5]+","+arrays[6]+","+arrays[7]+",");
//        System.out.println("最大值：" + max);
        return max;
    }

    /**
     * 计算8个数据的下限计算
     * @param arrays 需要计算的数组
     * @return 最小值
     */
    public double getMin(double[] arrays) {
        double min = 0;
        for (int i = 1; i < 9; i++) {
            if (min > arrays[i]) {
                min = arrays[i];
            }
        }
//        System.out.println("最小值：" + min);
        return min;
    }

    /**
     * 初始化8个数据的上下限
     */
    public void initCUAL() {
        for (int i = 0; i < 9; i++) {
            controlUpperAndLowerData[i] = 0;
            controlUpperAndLowerBOD[i] = 0;
            controlUpperAndLowerZS[i] = 0;
            controlUpperAndLowerMutilCOD[i] = 0;
            controlUpperAndLowerMutilCODNT[i] = 0;
            controlUpperAndLowerMutilEC[i] = 0;
            controlUpperAndLowerMutilPH[i] = 0;
            controlUpperAndLowerMutilORP[i] = 0;
            controlUpperAndLowerMutilDO[i] = 0;
            controlUpperAndLowerMutilNH[i] = 0;
            controlUpperAndLowerMutilNT[i] = 0;
        }
    }

    /**
     * 更新8个数据的上下限
     * @param d 数据值
     * @param type 类型
     */
    public void updateCUAL(double d, int type) {
        if (type == 0) {
            controlUpperAndLowerData[CUALDSign++] = d;
            CUALDSign = CUALDSign % 9;
        } else if (type == 1) {
            controlUpperAndLowerBOD[CUALBSign++] = d;
            CUALBSign = CUALBSign % 9;
        } else if (type == 2) {
            controlUpperAndLowerZS[CUALZSign++] = d;
            CUALZSign = CUALZSign % 9;
        } else if (type == 3) {
            controlUpperAndLowerMutilCOD[CUALMCODSign++] = d;
            CUALMCODSign = CUALMCODSign % 9;
        } else if (type == 4) {
            controlUpperAndLowerMutilCODNT[CUALMCODNTSign++] = d;
            CUALMCODNTSign = CUALMCODNTSign % 9;
        } else if (type == 5) {
            controlUpperAndLowerMutilEC[CUALMECSign++] = d;
            CUALMECSign = CUALMECSign % 9;
        } else if (type == 6) {
            controlUpperAndLowerMutilPH[CUALMPHSign++] = d;
            CUALMPHSign = CUALMPHSign % 9;
        } else if (type == 7) {
            controlUpperAndLowerMutilORP[CUALMORPSign++] = d;
            CUALMORPSign = CUALMORPSign % 9;
        } else if (type == 8) {
            controlUpperAndLowerMutilDO[CUALMDOSign++] = d;
            CUALMDOSign = CUALMDOSign % 9;
        } else if (type == 9) {
            controlUpperAndLowerMutilNH[CUALMNHSign++] = d;
            CUALMNHSign = CUALMNHSign % 9;
        } else if (type == 10) {
            controlUpperAndLowerMutilNT[CUALMNTSign++] = d;
            CUALMNTSign = CUALMNTSign % 9;
        }
    }

    // 初始化折线图demo数据
    public void initLineChartDemo() {
        double[] demo = {48.3, 56.8, 42.5, 70.6, 14.5, 52.2, 43.9, 48.8};
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dyDemo.addEntry(demo[finalI]);
                }
            }, 180 * i);
        }

    }

    /**
     * 返回是否需要存储实时数据
     * @return
     */
    public boolean isRealDataDownload() {
        // 获取 SharedPreferences
        SharedPreferences pref = getActivity().getSharedPreferences("data", getContext().MODE_PRIVATE);

        if (pref.getString("realDataDownload", "").equals("true")) {
            // 说明此时是开启的
            return true;
        }
        return false;
    }

    public void downloadRealData(int type, String data) {
        if (isRealDataDownload()) {
            String[] ds = data.split(",");
            int num = 0;
            String result = "";
            // DDM1
            if (type == 1) {
                num = DDM1++;
                result = ds[0] + "," + ds[1];
            }// DDM2
            else if (type == 2) {
                num = DDM2++;
                result = ds[0] + "," + ds[1];
            }// PHG
            else if (type == 3) {
                num = PHG++;
                result = ds[0] + "," + ds[1];
            }// ORP
            else if (type == 4) {
                num = ORP++;
                result = ds[0];
            }// RDO
            else if (type == 5) {
                num = RDO++;
                result = ds[0] + "," + ds[1];
            }// NHN
            else if (type == 6) {
                num = NHN++;
                result = ds[0] + "," + ds[1];
            }// ZS
            else if (type == 7) {
                num = ZS++;
                result = ds[0] + "," + ds[1];
            }// DDMS
            else if (type == 8) {
                num = DDMS++;
                result = ds[0] + "," + ds[1];
            }// COD
            else if (type == 9) {
                num = COD++;
                result = ds[0] + "," + ds[1] + "," + ds[2] + "," + ds[3];
            }// CL
            else if (type == 10) {
                num = CL++;
                result = ds[0] + "," + ds[1];
            }// CHLO
            else if (type == 11) {
                num = CHLO++;
                result = ds[0] + "," + ds[1];
            }// BGA
            else if (type == 12) {
                num = BGA++;
                result = ds[0] + "," + ds[1];
            }// TPS
            else if (type == 13) {
                num = TPS++;
                result = ds[0] + "," + ds[1];
            }// TSS
            else if (type == 14) {
                num = TSS++;
                result = ds[0] + "," + ds[1];
            }
            // OIL
            else if (type == 15) {
                num = OIL++;
                result = ds[0] + "," + ds[1];
            }
            // 多参数
            else if (type == 999) {
                num = MUTIL++;
                if(mutilSetUnit[0] == 0) {
                    ds[1] = "---";
                    ds[2] = "---";
                }
                for(int i = 2; i < 8 ; i ++) {
                    if(mutilSetUnit[i] == 0) {
                        ds[i + 1] = "---";
                    }
                }
                result = ds[0] + "," + ds[1] + "," + ds[2] + "," + ds[3] + "," + ds[4] + "," + ds[5] + "," + ds[6] + "," + ds[7] + "," + ds[8];
            }

            // 开始存储
            MyLog.downloadRealDataMain(num, type, result, ((MyApplication) getActivity().getApplication()).getMutilSensor());
        }
    }


    public void initRealDataDownloadNum() {
        DDM1 = 1;
        DDM2 = 1;
        PHG = 1;
        ORP = 1;
        RDO = 1;
        NHN = 1;
        ZS = 1;
        DDMS = 1;
        COD = 1;
        CL = 1;
        CHLO = 1;
        BGA = 1;
        TPS = 1;
        TSS = 1;
        OIL = 1;
        MUTIL = 1;
    }

    /**
     * 初始化多参数传感器的配置信息
     */
    public void initMutilSensorSet() {

        System.out.println("初始化多参数传感器的配置信息");
        // 判断多参数是否进行了更改配置
        SharedPreferences pref = getActivity().getSharedPreferences("data", getContext().MODE_PRIVATE);
        // 如果进行过配置
        if (pref.getString("mutilIsSet", "").equals("true")) {
            int[] unit = {2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 0};
            String[] name = new String[]{getString(R.string.data_EC), getString(R.string.data_PH), getString(R.string.data_ORP_0),
                    getString(R.string.data_DO), getString(R.string.data_NH4), getString(R.string.data_ZS),
                    getString(R.string.data_salinity), getString(R.string.data_residual_chlorine), getString(R.string.data_Chlorophyl),
                    getString(R.string.data_blue_green_algae), getString(R.string.data_Transparency), getString(R.string.data_Suspended_Solids),
                    getString(R.string.data_oil_in_water), getString(R.string.ununited)};

            // 更改多参数各传感器名称的数组
            if(pref.getString("mutilParameter1", "").equals("1")) {
                mutilSetName[0] = getString(R.string.ununited);
                mutilSetName[1] = getString(R.string.ununited);
            } else {
                mutilSetName[0] = getString(R.string.data_COD_0);
                mutilSetName[1] = getString(R.string.data_COD_ZS);
            }
            mutilSetName[2] = name[Integer.parseInt(pref.getString("mutilParameter2", ""))];
            mutilSetName[3] = name[Integer.parseInt(pref.getString("mutilParameter3", ""))];
            mutilSetName[4] = name[Integer.parseInt(pref.getString("mutilParameter4", ""))];
            mutilSetName[5] = name[Integer.parseInt(pref.getString("mutilParameter5", ""))];
            mutilSetName[6] = name[Integer.parseInt(pref.getString("mutilParameter6", ""))];
            mutilSetName[7] = name[Integer.parseInt(pref.getString("mutilParameter7", ""))];

            if(pref.getString("mutilParameter1", "").equals("1")) {
                mutilSetUnit[0] = 0;
                mutilSetUnit[1] = 0;
            } else {
                mutilSetUnit[0] = 9;
                mutilSetUnit[1] = 7;
            }
            mutilSetUnit[2] = unit[Integer.parseInt(pref.getString("mutilParameter2", ""))];
            mutilSetUnit[3] = unit[Integer.parseInt(pref.getString("mutilParameter3", ""))];
            mutilSetUnit[4] = unit[Integer.parseInt(pref.getString("mutilParameter4", ""))];
            mutilSetUnit[5] = unit[Integer.parseInt(pref.getString("mutilParameter5", ""))];
            mutilSetUnit[6] = unit[Integer.parseInt(pref.getString("mutilParameter6", ""))];
            mutilSetUnit[7] = unit[Integer.parseInt(pref.getString("mutilParameter7", ""))];

        } else {
            mutilSetName[0] = getString(R.string.data_COD_0);
            mutilSetName[1] = getString(R.string.data_COD_ZS);
            mutilSetName[2] = getString(R.string.data_EC_salinity);
            mutilSetName[3] = getString(R.string.data_PH);
            mutilSetName[4] = getString(R.string.data_ORP_0);
            mutilSetName[5] = getString(R.string.data_DO);
            mutilSetName[6] = getString(R.string.data_NH4);
            mutilSetName[7] = getString(R.string.data_ZS);
        }

    }

    // 获取当前经纬度
    public Location getCurrentLngAndLat() {
        //获取地理位置管理器
        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO:去请求权限后再获取
//            System.out.print("需要请求权限在获取数据");
//            return null;
//        }
//        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
//        for (String provider : providers) {
//            Location l = mLocationManager.getLastKnownLocation(provider);
//            if (l == null) {
//                continue;
//            }
//            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                bestLocation = l;
//            }
//        }
//// 在一些手机5.0(api21)获取为空后，采用下面去兼容获取。
//        if (bestLocation==null){
//            Criteria criteria = new Criteria();
//            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//            criteria.setAltitudeRequired(false);
//            criteria.setBearingRequired(false);
//            criteria.setCostAllowed(true);
//            criteria.setPowerRequirement(Criteria.POWER_LOW);
//            String provider = mLocationManager.getBestProvider(criteria, true);
//            if (!TextUtils.isEmpty(provider)){
//                bestLocation = mLocationManager.getLastKnownLocation(provider);
//            }
//        }
        //获取所有可用的位置提供器
        List<String> providers = mLocationManager.getProviders(true);
        String locationProvider = null;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            System.out.print("locationProvider1:" + locationProvider);
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            System.out.print("locationProvider2:" + locationProvider);
        } else {
            System.out.println("没有可用的位置提供器");
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.print("没有权限");
            return null;
        }

        try {
            bestLocation = mLocationManager.getLastKnownLocation(locationProvider);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            // 如果打开了蓝牙权限
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};// 访问精确的位置
            for (String permission : permissions) {
                // ContextCompat类的checkSelfPermission方法用于检测用户是否授权了某个权限。
                // 第一个参数需要传入Context,第二个参数需要传入需要检测的权限，返回值0已授权，-1未授权
                int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {// 许可授予
                    //位置权限
                    onPermissionGranted(permission);
                }
            }
        }
//        //获取Location
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            System.out.print("没有权限");
//            return null;
//        }
//        List<String> providers = mLocationManager.getProviders(true);
//        Location bestLocation = null;
//        for (String provider : providers) {
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                System.out.println("为空");
//                return null;
//            }
//            Location l = mLocationManager.getLastKnownLocation(provider);
//            if (l == null) {
//                continue;
//            }
//            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                // Found best last known location: %s", l);
//                bestLocation = l;
//            }
//        }


        return bestLocation;

    }

    //在许可授予定位
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION://访问精确的位置
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.storage2)
                            .setPositiveButton(R.string.ensure,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        //ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS应用管理
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                }
                break;
        }
    }
    //检查GPS是否打开INPUT_SERVICE
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);//定位服务
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    public void httpRequest(String lng, String lat) {
        String u = "http://api.map.baidu.com/ag/coord/convert?from=0&to=4&x=" + lng + "&y=" + lat;
        StringBuffer buffer = new StringBuffer();
        final String[] result = {""};
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(u);
                    HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();

                    httpUrlConn.setDoOutput(false);
                    httpUrlConn.setDoInput(true);
                    httpUrlConn.setUseCaches(false);

                    httpUrlConn.setRequestMethod("GET");
                    httpUrlConn.connect();

                    // 将返回的输入流转换成字符串
                    InputStream inputStream = httpUrlConn.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String str = null;
                    while ((str = bufferedReader.readLine()) != null) {
                        buffer.append(str);
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    // 释放资源
                    inputStream.close();
                    httpUrlConn.disconnect();
                    System.out.println("获取到的数据：" + buffer.toString());

                    try {
                        String lngAndLatBase64 = buffer.toString();
                        lngAndLatBase64 = lngAndLatBase64.replace("\"", "");
                        lngAndLatBase64 = lngAndLatBase64.replace("}", "");
                        System.out.println("格式修改：" + lngAndLatBase64);
                        String[] lls = lngAndLatBase64.split(",");
                        String lng = changeLngAndLatBase64(lls[1].split(":")[1]);
                        String lat = changeLngAndLatBase64(lls[2].split(":")[1]);

                        try{
                            result[0] = getString(R.string.longitude) + ":" + lng.substring(0, 12) + "  ";
                        } catch (StringIndexOutOfBoundsException e) {
                            result[0] = getString(R.string.longitude) + ":" + lng + "  ";
                        }
                        try {
                            result[0] = result[0] + getString(R.string.latitude) + ":" + lat.substring(0, 11);
                        } catch (StringIndexOutOfBoundsException e) {
                            result[0] = result[0] + getString(R.string.latitude) + ":" + lat;
                        }

//                        result[0] = getString(R.string.longitude) + ":" + lng + "  " + getString(R.string.latitude) + ":" + lat;
                    } catch(IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return;
                    }
                    displayLngAndLat(result[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Location updateToNewLocation(Location location) {
        System.out.println("--------zhixing--2--------");
        String latLongString;
        double lat = 0;
        double lng=0;

        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            latLongString = "纬度:" + lat + "\n经度:" + lng;
            System.out.println("经度："+lng+"纬度："+lat);
        } else {
            latLongString = "无法获取地理信息，请稍后...";
        }
        if(lat!=0){
            System.out.println("--------反馈信息----------"+ String.valueOf(lat));
        }
        String result = "";
        try {
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());
            System.out.println("现在：" + longitude + "   ;   " + latitude);
            httpRequest(longitude, latitude);



//                        result[0] = getString(R.string.longitude) + ":" + lng + "  " + getString(R.string.latitude) + ":" + lat;
        } catch(IndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            lngAndLat.setVisibility(View.VISIBLE);
            lngAndLatValue.setText(getString(R.string.gps_info_error));
            System.out.println("空指针异常");
        }

        System.out.println("latLongString:" + latLongString);

        return location;

    }

    public final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateToNewLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            updateToNewLocation(null);
        }
    };

    /**
     * Base64解码成字符串
     * @param value
     * @return
     */
    public String changeLngAndLatBase64(String value) {
        // 编码
//        String s = "120.36439523";
//        System.out.println(Base64.encodeToString(s.getBytes(), Base64.DEFAULT));
//
//        String str = "MTIwLjM3NTM5NDU1Nzc5";
        byte[] bytes = Base64.decode(value, Base64.DEFAULT);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] < 0) {
                bytes[i] += 256;
            }
        }
        return new String(bytes);
    }

    // 显示经纬度框
    public void displayLngAndLat(String result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lngAndLat.setVisibility(View.VISIBLE);
                lngAndLatValue.setText(result);
            }
        });
    }

    public void getCurrentLngAndLat2() {
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) getActivity().getSystemService(serviceName); // 查找到服务信息
        //locationManager.setTestProviderEnabled("gps", true);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("notnotnotnotnotnotnot");
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600 * 1000, 0, mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600 * 1000, 0, mLocationListener);
    }
}
