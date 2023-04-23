package com.clj.blesample.tab;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.TwoStatePreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.R;
import com.clj.blesample.adapter.ChartAdapter;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.entity.Chart;
import com.clj.blesample.utils.DyLineChartUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.github.mikephil.charting.charts.LineChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RealDataFragmentNew extends Fragment {
    private LineChart chart; // 样例
    private DyLineChartUtils dyDemo; // 样例
    private BleDevice bleDevice;
    private String incompleteData = ""; // 不完全的实时数据
    private Boolean isRealData = false; // 是实时数据
    private List paramIdList = new ArrayList<String>();
    private String[] paramArr = new String[]{"","DDM_μ","DDM_m","PHG","ORP","RDO","ION","ZS","DDM_S","COD","CL","CHLO","BGA","TPS","TSS","OIL","BOD"};
    private String[] paramArrChinese = new String[]{"","电导率","电导率","PH","ORP","溶解氧","铵氮/离子类"," ","盐度","COD","余氯","叶绿素","蓝绿藻","透明度","悬浮物","水中油","BOD"};
    private String[] unitArr = new String[]{"","μS/cm","mS/cm","","mV","mg/L","mg/L","NTV","PSU","mg/L","mg/L","μg/L","Kcells/mL","mm","mg/L","mg/L",""};
    // 单参数数据上下限：占位符，电导率1，电导率2，PH，ORP，溶解氧，氨氮，浊度，盐度，化学需氧量COD,余氯，，叶绿素，蓝绿藻，透明度，悬浮物，水中油,BOD
    private int[] chartMaxArr = {0,100, 100, 14,  100, 20,  100,  100,  70, 100,10,  400,  300,  100,  100, 40, 500};
    private boolean isFirstData = true;
    private int sq = 0;
    private int dataIndex = 0;
    private TextView time1;
    private TextView time2;
    private List<Chart> chartList=new ArrayList<>();
    private ChartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.real_time_data_new, container, false);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView sensor_type = view.findViewById(R.id.sensor_type);
        time1=view.findViewById(R.id.time1);
        time2=view.findViewById(R.id.time2);

//        chartList.add(new Chart("COD","30"));
//        chartList.add(new Chart("COD2","330"));
        RecyclerView chart_recyclerView = view.findViewById(R.id.chart_recyclerView);
        chart_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ChartAdapter(chartList,getActivity());
        chart_recyclerView.setAdapter(adapter);
        getDeviceInfo();


        sensor_type.setOnClickListener(new View.OnClickListener() { //测试用数据，头像作为启动按钮
            @Override
            public void onClick(View view) {
                String[][] fakeData = new String[][]{new String[]{"3","0","9"},new String[]{"6","0","6"},new String[]{"16","0","4"},
                        new String[]{"3","0.0","0","0.0","0","3.764","3","2"},
                        new String[]{"6", "0.09", "0", "3.764"},new String[]{"16", "155", "3.764"},new String[]{"3","0.0","0","0.0","0","3.764"}
                };
                sq = 0;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (sq<3){
                                    updateData(fakeData[sq],true);
                                }else {
                                    updateData(fakeData[sq],false);
                                }
                                sq++;
                                if (sq==7){
                                    sq=4;
                                }
                            }
                        });
                    }
                },0,1500);
            }
        });

    }



    // 初始化折线图demo数据
    public void initLineChartDemo() {
//        dyDemo = new DyLineChartUtils(lineChartDemo, "", Color.BLUE, getContext());
        dyDemo.setYAxis(100, 0, 8);
        for (int i = 0; i < 8; i++) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dyDemo.addEntry(Math.random() * 100);
                }
            }, 180 * i);
        }

    }

    private void updateData(String[] numArr, Boolean isHead) {
        System.out.println(Arrays.toString(numArr));
        if (isHead){ //头数据,表示传感器类型,例如[6,0,9],第1个值表示地址，第3个表示参数类型id
            paramIdList.add(numArr[0]);
            chartList.add(new Chart(paramArr[Integer.parseInt(numArr[2])],"",unitArr[Integer.parseInt(numArr[2])],chartMaxArr[Integer.parseInt(numArr[2])],numArr[0]));
            System.out.println(paramArr[Integer.parseInt(numArr[2])]);
            adapter.notifyDataSetChanged();
        }else {
            if(!paramIdList.contains(numArr[0])){
                Toast.makeText(getActivity(),"地址"+numArr[0]+"未声明",Toast.LENGTH_SHORT).show();
            }

            if (isFirstData){ //第一条带花括号的数据，后两个代表时间
                time1.setText(numArr[numArr.length-2]+"min");
                time2.setText(numArr[numArr.length-1]+"min");
                isFirstData = false;
                adapter.setNeedCreateChat(false);
            }else { //带花括号的数据，排除第一条
                if (numArr.length==6){//长度为3代表是ORP,4是普遍情况,6是COD
                    adapter.updateCodChart(paramIdList.indexOf(numArr[0]),Double.parseDouble(numArr[1]),numArr[2],numArr[3],numArr[4]); //更新表格数据，需要加入浊度和BOD
                }else {
                    adapter.updateChart(paramIdList.indexOf(numArr[0]),Double.parseDouble(numArr[1]),numArr[2]); //更新表格数据
                }
            }


        }
    }


    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    // 首先获取设备的地址和类型
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getDeviceInfo() {
        // 如果此时存储的设备地址是0，表示没有设备地址的存入
        String hex = "f900";
        System.out.println("现在发送的指令是" + hex);
        bleDevice = ((MyApplication) getActivity().getApplication()).getBasicBleDevice();

        //获取BluetoothGattCharacteristic
        // 打开监听
        BluetoothGattCharacteristic characteristicRead = null;
        BluetoothGattCharacteristic characteristicWrite = null;
        BluetoothGattService service = null;
        int chipType = 0;
        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
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
                    characteristicWrite = cs[0];
                    characteristicRead = cs[1];

                    BluetoothGattCharacteristic finalCharacteristicWrite = characteristicWrite;
                    BluetoothGattCharacteristic finalCharacteristicRead = characteristicRead;
                    new Handler().postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            BleManager.getInstance().write(bleDevice, finalCharacteristicWrite.getService().getUuid().toString(),
                                    finalCharacteristicWrite.getUuid().toString(),
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
                                                            finalCharacteristicRead.getService().getUuid().toString(),
                                                            finalCharacteristicRead.getUuid().toString(),
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
//
                                                                                    String s1 = HexUtil.byteToString(data);
                                                                                    String s2 = "时间:" + dateString + ",接收到的f900实时数据:" + s1 + "长度：" + s1.split(",").length;
                                                                                    System.out.println("接收：" + s2);
                                                                                    //判断是否为头数据
                                                                                    if (s1.charAt(0) == '['){
                                                                                        updateData(s1.substring(1, s1.length() - 1).split(","),true);
                                                                                    }


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
                                                                                        System.out.println("此为一条完整的实时数据");
//                                                                                        s1 = s1.replaceAll("[{}]", "");
                                                                                        updateData(s1.substring(1, s1.length() - 1).split(","),false);
                                                                                        isRealData = true;
                                                                                    } else {
                                                                                        isRealData = false;
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
            }
        }
    }


}

