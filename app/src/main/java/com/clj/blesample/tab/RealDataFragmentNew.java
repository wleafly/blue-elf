package com.clj.blesample.tab;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RealDataFragmentNew extends Fragment {
    private LineChart chart; // 样例
    private DyLineChartUtils dyDemo; // 样例
    private BleDevice bleDevice;
    private String incompleteData = ""; // 不完全的实时数据
    private Boolean isRealData = false; // 是都是实时数据
    private Map<String,String> addressToParamIdMap = new HashMap<>();
    private String[] paramArr = new String[]{"DDM_μ","DDM_m","PHG","ORP","RDO","ION","ZS","DDM_S","COD","CL","CHLO","BGA","TPS","TSS","OIL","BOD"};
    private String[] paramArrChinese = new String[]{"电导率","电导率","PH值","ORP","溶解氧","铵氮/离子类","浊度","盐度","COD","余率","叶绿素","蓝绿藻","透明度","悬浮物","水中油","BOD"};
    private String[] unitArr = new String[]{"μS/cm","mS/cm","","mV","mg/L","mg/L","mg/L","NTV","PSU","mg/L","mg/L","μg/L","Kcells/mL","mm","mg/L","mg/L",""};
    private boolean isFirstData = true;
    private int sequence = 0;
    private int dataIndex = 0;
    private TextView time1;
    private TextView time2;

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
        ImageView sensor_type = view.findViewById(R.id.sensor_type);
        time1=view.findViewById(R.id.time1);
        time2=view.findViewById(R.id.time2);

        init(view);

        sensor_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList data = new ArrayList<String []>();
                data.add(new String[]{"3","0","9"});
                data.add(new String[]{"6","0","6"});
                data.add(new String[]{"16","0","4"});
                data.add(new String[]{"3","0.0","0","0.0","0","3.764","3","2"});//带时间
                data.add(new String[]{"6", "0.09", "0", "3.764"});
                data.add(new String[]{"16", "155", "3.764"});
                data.add(new String[]{"3","0.0","0","0.0","0","3.764"});//带时间

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateDate((String[])data.get(sequence));
                        if (sequence==6){
                            sequence = 3;
                        }
                        sequence++;
                    }
                },0,1500);
            }
        });



//        getDeviceInfo();

    }

    private void init(View view) {
        LinearLayout linear = view.findViewById(R.id.linear);
        LinearLayout linear2 = view.findViewById(R.id.linear2);
        LinearLayout linear3 = view.findViewById(R.id.linear3);
        LinearLayout linear_special = view.findViewById(R.id.linear_special);
        TextView main_param = view.findViewById(R.id.main_param);
        TextView main_param2 = view.findViewById(R.id.main_param2);
        TextView main_param3 = view.findViewById(R.id.main_param3);
        TextView main_param_special = view.findViewById(R.id.main_param_special);
        TextView value = view.findViewById(R.id.value);
        TextView value2 = view.findViewById(R.id.value2);
        TextView value3 = view.findViewById(R.id.value3);
        TextView value_special = view.findViewById(R.id.value_special);
        LinearLayout hide_content = view.findViewById(R.id.hide_content);
        LinearLayout hide_content2 = view.findViewById(R.id.hide_content2);
        LinearLayout hide_content3 = view.findViewById(R.id.hide_content3);
        LinearLayout hide_content_special = view.findViewById(R.id.hide_content_special);
        CheckBox chat_show_or_fold = view.findViewById(R.id.chat_show_or_fold);
        CheckBox chat_show_or_fold2 = view.findViewById(R.id.chat_show_or_fold2);
        CheckBox chat_show_or_fold3 = view.findViewById(R.id.chat_show_or_fold3);
        CheckBox chat_show_or_fold_special = view.findViewById(R.id.chat_show_or_fold_special);
        TextView bod_special = view.findViewById(R.id.bod_special);
        TextView electric_special = view.findViewById(R.id.electric_special);

        chart = view.findViewById(R.id.chart);

        dyDemo = new DyLineChartUtils(chart, "", Color.BLUE, getContext());
        dyDemo.setYAxis(100, 0, 8);
        chat_show_or_fold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    hide_content.setVisibility(View.VISIBLE);
                } else {
                    hide_content.setVisibility(View.GONE);
                }
            }
        });
        chat_show_or_fold2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    hide_content2.setVisibility(View.VISIBLE);
                } else {
                    hide_content2.setVisibility(View.GONE);
                }
            }
        });
        chat_show_or_fold3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    hide_content3.setVisibility(View.VISIBLE);
                } else {
                    hide_content3.setVisibility(View.GONE);
                }
            }
        });
        chat_show_or_fold_special.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    hide_content_special.setVisibility(View.VISIBLE);
                } else {
                    hide_content_special.setVisibility(View.GONE);
                }
            }
        });

    }

    // 初始化折线图demo数据
    public void initLineChartDemo() {
//        double[] demo = {48.3, 56.8, 42.5, 70.6, 14.5, 52.2, 43.9, 48.8};
        for (int i = 0; i < 8; i++) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dyDemo.addEntry(Math.random() * 100);
                }
            }, 180 * i);
        }

    }

    private void updateDate(String[] numArr) {
        System.out.println(Arrays.toString(numArr));

        if (numArr.length==3&&numArr[1].equals("0")){ //头数据,表示传感器类型
            String address = numArr[0];
            String paramType = numArr[2];
            addressToParamIdMap.put(address,paramType);
        }else {
            if (isFirstData){ //第一条长度不为3的数据，后两个代表时间
                time1.setText(numArr[numArr.length-2]+"min");
                time2.setText(numArr[numArr.length-1]+"min");
                isFirstData = false;

            }
            switch (numArr.length){
                case 3:
                    System.out.println("ORP");
                    break;
                case 4:
                    System.out.println("一般情况");
                    break;
                case 6:
                    System.out.println("COD");
            }
        }
        dataIndex++;
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    // 首先获取设备的地址和类型
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getDeviceInfo() {
        // 如果此时存储的设备地址是0，表示没有设备地址的存入
//        if(globalDeviceAddress == 0 || globalIntervalTime == -1) {
//        Date date = new Date();
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//        String dateString = formatter.format(date);
        String hex = "f900";
        System.out.println("现在发送的指令是" + hex);
        Boolean isGetData = false;
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
                                                                                        updateDate(s1.substring(1, s1.length() - 1).split(","));
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
                                                                                        updateDate(s1.substring(1, s1.length() - 1).split(","));
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

