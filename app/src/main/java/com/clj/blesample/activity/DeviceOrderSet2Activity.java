package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.TextUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceOrderSet2Activity  extends AppCompatActivity {

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
    private int globalType; // 是否是多参数/单参数

    private ProgressDialog progressDialog; // 提示框

    private int addressAwaitTime = 100;

    private EditText deviceAddress; //
    private EditText tempOrder;
    private EditText zeroOrder;
    private EditText slopeOrder;
    private EditText intervalTime;
    private EditText testTime;
    private EditText specialCODZeroOrder;
    private EditText specialCODSlopeOrder;

    private Button deviceAddressBtn;
    private Button tempOrderBtn;
    private Button zeroOrderBtn;
    private Button slopeOrderBtn;
    private Button timeBtn;
    private Button getDeviceAddressBtn;
    private Button specialCODZeroOrderBtn;
    private Button specialCODSlopeOrderBtn;

    private View orderSetLayout2;
    private View sensorSelect;
    private Spinner sensorType;
    private View sensorSelectUnderline;
    private View cleaningBrush;
    private View specialCODLayout;

    private View generalLayout; // 基本的零点校准和斜率校准框
    private View PHLayout; // 为PH特制的零点校准和斜率校准框
    private Spinner PHSlopeSpinner; // PH特制的斜率校准数据框
    private Button PHZeroButton;
    private Button PHSlopeButton;
    private View slopeLayout;
    private View TempOrderLayout; // 温度校准

    private Handler handler;

    private Runnable runnable;
    private ProgressDialog progressDialogGet;

    private Drawable drawableInit;
    private Drawable drawable;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_order_set3);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.data_order_activity));
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


        orderSetLayout2 = findViewById(R.id.order_set2_layout);
        deviceAddress = findViewById(R.id.device_address);
        tempOrder = findViewById(R.id.temp_order);
        zeroOrder = findViewById(R.id.zero_order);
        slopeOrder = findViewById(R.id.slope_order);
        intervalTime = findViewById(R.id.interval_time);
        testTime = findViewById(R.id.test_time);
        sensorSelect = findViewById(R.id.sensor_select);
        sensorType = findViewById(R.id.sensor_type);
        sensorSelectUnderline = findViewById(R.id.sensor_select_underline);
        cleaningBrush = findViewById(R.id.cleaning_brush);

        // COD特殊情况
        specialCODLayout = findViewById(R.id.special_COD_layout);
        specialCODZeroOrder = findViewById(R.id.special_COD_zero_order);
        specialCODSlopeOrder = findViewById(R.id.special_COD_slope_order);
        specialCODZeroOrderBtn = findViewById(R.id.special_COD_zero_order_btn);
        specialCODSlopeOrderBtn = findViewById(R.id.special_COD_slope_order_btn);

        // PH特殊情况
        PHSlopeSpinner = findViewById(R.id.slope_order_PH);

        TextView test = findViewById(R.id.test);
        TextView test2 = findViewById(R.id.test2);
        TextView test3 = findViewById(R.id.test3);
        TextView test4 = findViewById(R.id.test4);
        Button bbb = findViewById(R.id.test_btn);
        Button bbb2 = findViewById(R.id.test_btn2);
        Button bbb3 = findViewById(R.id.test_btn3);
        Button bbb4 = findViewById(R.id.test_btn4);

        generalLayout = findViewById(R.id.layout_general);
        PHLayout = findViewById(R.id.layout_ph);
        PHSlopeSpinner = findViewById(R.id.slope_order_PH);
        PHZeroButton = findViewById(R.id.zero_order_PH_btn);
        PHSlopeButton = findViewById(R.id.slope_order_PH_btn);

        deviceAddressBtn = findViewById(R.id.device_address_btn);
        tempOrderBtn = findViewById(R.id.temp_order_btn);
        zeroOrderBtn = findViewById(R.id.zero_order_btn);
        slopeOrderBtn = findViewById(R.id.slope_order_btn);
        timeBtn = findViewById(R.id.time_btn);
        slopeLayout = findViewById(R.id.slope_layout);
//        getDeviceAddressBtn = findViewById(R.id.get_device_address_btn);
        TempOrderLayout = findViewById(R.id.temp_order_layout);

        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication)getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication)getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication)getApplication()).getBasicDeviceType();
        globalType = ((MyApplication)getApplication()).getBasicType();

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


        // 如果是单参数
        if(globalType == 0) {
            sensorSelect.setVisibility(View.GONE);
            sensorSelectUnderline.setVisibility(View.GONE);
            deviceAddress.setText(Integer.toString(globalDeviceAddress));

            // 如果是COD这个特殊情况
            if(globalDeviceType == 9) {
                specialCODLayout.setVisibility(View.VISIBLE);
            }

            // 如果是PH这个特殊情况:显示专用的校准框
            if(globalDeviceType == 3) {
                generalLayout.setVisibility(View.GONE);
                PHLayout.setVisibility(View.VISIBLE);
            }

            // 如果是ORP这个特殊情况：没有斜率校准
            if(globalDeviceType == 4) {
                generalLayout.setVisibility(View.VISIBLE);
                slopeLayout.setVisibility(View.GONE);
                PHLayout.setVisibility(View.GONE);
                TempOrderLayout.setVisibility(View.GONE);
            }

        }else if(globalType == 1) {
            // 如果是多参数
            sensorSelect.setVisibility(View.VISIBLE);
            sensorSelectUnderline.setVisibility(View.VISIBLE);
            deviceAddress.setText(Integer.toString(globalDeviceAddress));

        }else if(globalType == -1) {
            // 如果还没有获取是多参数还是单参数
            progressDialog = new ProgressDialog(this);
            progressDialog.setIcon(R.mipmap.ic_launcher);
            progressDialog.setTitle(getString(R.string.init));
            progressDialog.setMessage(getString(R.string.init_device_address_type));
            progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
            progressDialog.setCancelable(true);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
            progressDialog.show();

            final String hex = "f900";

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

                                    System.out.println("实时数据指令发送成功");


                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            BleManager.getInstance().notify(
                                                    bleDevice,
                                                    characteristicRead.getService().getUuid().toString(),
                                                    characteristicRead.getUuid().toString(),
                                                    new BleNotifyCallback() {


                                                        @Override
                                                        public void onNotifySuccess() {
                                                            System.out.println("notify发送成功");
                                                        }

                                                        @Override
                                                        public void onNotifyFailure(BleException exception) {
                                                            System.out.println("notify发送失败");
                                                        }

                                                        @Override
                                                        public void onCharacteristicChanged(byte[] data) {

                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    System.out.println("开始读取实时数据信息...");
                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                        @Override
                                                                        public void run() {
                                                                            Date date = new Date();
                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                            String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
//                                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                            String s1 = HexUtil.byteToString(data);
                                                                            String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                                                            System.out.println("s2的值：" + s2);
                                                                            // 如果获取的数据是这种类型：[12,1,16,]
                                                                            if (s1.split(",").length == 4 && s1.charAt(0) == '[' && s1.charAt(s1.length() - 1) == ']') {
                                                                                System.out.println("地址和类型获取成功：" + s1);
                                                                                s1 = s1.replaceAll("[\\[\\]]", "");
                                                                                int address = Integer.parseInt(s1.split(",")[0]); // 设备地址
                                                                                int type0 = Integer.parseInt(s1.split(",")[1]); // 设备类型（多参数、单参数）
                                                                                int type = Integer.parseInt(s1.split(",")[2]); // 传感器类型



                                                                                // 这里设置地址显示
                                                                                deviceAddress.setText(Integer.toString(address));
                                                                                globalDeviceAddress = address;
                                                                                progressDialog.setMessage(getString(R.string.address_gain_success));
                                                                                ((MyApplication) getApplication()).setBasicDeviceAddress(address);
                                                                                ((MyApplication) getApplication()).setBasicDeviceType(type);
                                                                                ((MyApplication) getApplication()).setBasicType(type0);
                                                                                globalType = type0;
                                                                                globalDeviceType = type;
                                                                                progressDialog.setMessage(getString(R.string.type_gain_success));

                                                                                // 如果是单参数
                                                                                if(globalType == 0) {
                                                                                    sensorSelect.setVisibility(View.GONE);
                                                                                    sensorSelectUnderline.setVisibility(View.GONE);
                                                                                    deviceAddress.setText(Integer.toString(globalDeviceAddress));

                                                                                    // 如果是COD这个特殊情况
                                                                                    if(globalDeviceType == 9) {
                                                                                        specialCODLayout.setVisibility(View.VISIBLE);
                                                                                    }

                                                                                    // 如果是PH这个特殊情况:显示专用的校准框
                                                                                    if(globalDeviceType == 3) {
                                                                                        generalLayout.setVisibility(View.GONE);
                                                                                        PHLayout.setVisibility(View.VISIBLE);
                                                                                    }

                                                                                    // 如果是ORP这个特殊情况：没有斜率校准
                                                                                    if(globalDeviceType == 4) {
                                                                                        generalLayout.setVisibility(View.VISIBLE);
                                                                                        slopeLayout.setVisibility(View.GONE);
                                                                                        PHLayout.setVisibility(View.GONE);
                                                                                        TempOrderLayout.setVisibility(View.GONE);
                                                                                    }

                                                                                }else if(globalType == 1) {
                                                                                    // 如果是多参数
                                                                                    sensorSelect.setVisibility(View.VISIBLE);
                                                                                    sensorSelectUnderline.setVisibility(View.VISIBLE);
                                                                                    deviceAddress.setText(Integer.toString(globalDeviceAddress));

                                                                                }

                                                                                progressDialog.dismiss();

                                                                                BleManager.getInstance().stopNotify(bleDevice,
                                                                                        characteristicRead.getService().getUuid().toString(),
                                                                                        characteristicRead.getUuid().toString());

                                                                            }

                                                                        }
                                                                    },10);


                                                                }
                                                            });
                                                        }
                                                    });

                                        }
                                    }, 100);
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    System.out.println("实时数据指令失败：" + exception);
                                }
                            });


                }
            }, 100);



        }

        drawableInit = getResources().getDrawable(R.drawable.setting_button_edit);
        drawable = getResources().getDrawable(R.color.gray1);

        // 给progressDialog加一个定时关闭的功能
        handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //要做的事情
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.operation_fail)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                handler.removeCallbacks(runnable);
                                deviceAddressBtn.setBackground(drawableInit);
                                deviceAddressBtn.setEnabled(true);
                                tempOrderBtn.setBackground(drawableInit);
                                tempOrderBtn.setEnabled(true);
                                slopeOrderBtn.setBackground(drawableInit);
                                slopeOrderBtn.setEnabled(true);
                                zeroOrderBtn.setBackground(drawableInit);
                                zeroOrderBtn.setEnabled(true);
                                timeBtn.setBackground(drawableInit);
                                timeBtn.setEnabled(true);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                System.out.print("空指针异常");
                            }
                        }
                    }).setCancelable(false).show();

                }
            }
        };

        BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());

        // 更改地址
        deviceAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceAddressBtn.setEnabled(false);

                if(globalDeviceIsConnect == false) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                    return;
                }

                if (deviceAddress.getText().toString().equals("")) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure), null).show();
                    return;
                }
                int address = Integer.parseInt(deviceAddress.getText().toString());
                if (address > 128 || address < 1) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure), null).show();
                    return;
                }

                progressDialog = new ProgressDialog(DeviceOrderSet2Activity.this);
                progressDialog.setIcon(R.mipmap.ic_launcher);
                progressDialog.setTitle(getString(R.string.change_device_address));
                progressDialog.setMessage(getString(R.string.device_address_changing));
                progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                progressDialog.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                progressDialog.show();
                handler.postDelayed(runnable, 12000);//每12秒执行一次runnable.

                deviceAddressBtn.setBackground(drawable);
                deviceAddressBtn.setEnabled(false);

                if(globalDeviceAddress == 0) {
                    // 先读地址
                    getDeviceAddress(2);
                    addressAwaitTime = 3000;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(progressDialogGet.isShowing()) {
                                progressDialogGet.dismiss();
                            }
                            orderDeviceAddress();
                        }
                    },5000);
                }else {
                    orderDeviceAddress();
                }
//                getDeviceAddress();

            }
        });

        bbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test.setText("");
                final String hex = "fb";

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

                                        System.out.println("实时数据指令发送成功");


                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                BleManager.getInstance().notify(
                                                        bleDevice,
                                                        characteristicRead.getService().getUuid().toString(),
                                                        characteristicRead.getUuid().toString(),
                                                        new BleNotifyCallback() {


                                                            @Override
                                                            public void onNotifySuccess() {
                                                                System.out.println("notify发送成功");
                                                            }

                                                            @Override
                                                            public void onNotifyFailure(BleException exception) {
                                                                System.out.println("notify发送失败");
                                                            }

                                                            @Override
                                                            public void onCharacteristicChanged(byte[] data) {

                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        System.out.println("开始读取实时数据信息...");
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                            @Override
                                                                            public void run() {
                                                                                Date date = new Date();
                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
//                                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                String s1 = HexUtil.byteToString(data);
                                                                                String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                                                                System.out.println("s2的值：" + s2);
                                                                                String s = test.getText().toString();
                                                                                test.setText(s + "\n" + s2);

//                                                                                System.out.println("原始s1" + s1);
//                                                                                System.out.println("原始s1长度" + s1.split(",").length);

                                                                            }
                                                                        },10);


                                                                    }
                                                                });
                                                            }
                                                        });

                                            }
                                        }, 100);


//


                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        System.out.println("实时数据指令失败：" + exception);
                                    }
                                });


                    }
                }, 1000);
            }
        });

        bbb4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test4.setText("");
                final String hex = "fa";

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

                                        System.out.println("实时数据指令发送成功");


                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                BleManager.getInstance().notify(
                                                        bleDevice,
                                                        characteristicRead.getService().getUuid().toString(),
                                                        characteristicRead.getUuid().toString(),
                                                        new BleNotifyCallback() {


                                                            @Override
                                                            public void onNotifySuccess() {
                                                                System.out.println("notify发送成功");
                                                            }

                                                            @Override
                                                            public void onNotifyFailure(BleException exception) {
                                                                System.out.println("notify发送失败");
                                                            }

                                                            @Override
                                                            public void onCharacteristicChanged(byte[] data) {

                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        System.out.println("开始读取实时数据信息...");
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                            @Override
                                                                            public void run() {
                                                                                Date date = new Date();
                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
//                                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                String s1 = HexUtil.byteToString(data);
                                                                                String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                                                                System.out.println("s2的值：" + s2);
                                                                                String s = test4.getText().toString();
                                                                                test4.setText(s + "\n" + s2);

//                                                                                System.out.println("原始s1" + s1);
//                                                                                System.out.println("原始s1长度" + s1.split(",").length);

                                                                            }
                                                                        },10);


                                                                    }
                                                                });
                                                            }
                                                        });

                                            }
                                        }, 100);


//


                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        System.out.println("实时数据指令失败：" + exception);
                                    }
                                });


                    }
                }, 1000);
            }
        });

        bbb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test3.setText("");
                final String hex = "f900";

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

                                        System.out.println("实时数据指令发送成功");


                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                BleManager.getInstance().notify(
                                                        bleDevice,
                                                        characteristicRead.getService().getUuid().toString(),
                                                        characteristicRead.getUuid().toString(),
                                                        new BleNotifyCallback() {


                                                            @Override
                                                            public void onNotifySuccess() {
                                                                System.out.println("notify发送成功");
                                                            }

                                                            @Override
                                                            public void onNotifyFailure(BleException exception) {
                                                                System.out.println("notify发送失败");
                                                            }

                                                            @Override
                                                            public void onCharacteristicChanged(byte[] data) {

                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        System.out.println("开始读取实时数据信息...");
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                            @Override
                                                                            public void run() {
                                                                                Date date = new Date();
                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
//                                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                String s1 = HexUtil.byteToString(data);
                                                                                String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                                                                System.out.println("s2的值：" + s2);
                                                                                String s = test3.getText().toString();
                                                                                test3.setText(s + "\n" + s2);

//                                                                                System.out.println("原始s1" + s1);
//                                                                                System.out.println("原始s1长度" + s1.split(",").length);

                                                                            }
                                                                        },10);


                                                                    }
                                                                });
                                                            }
                                                        });

                                            }
                                        }, 100);


//


                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        System.out.println("实时数据指令失败：" + exception);
                                    }
                                });


                    }
                }, 1000);
            }
        });

        bbb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String hex = "fd";

                new Handler().postDelayed(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                    @Override
                    public void run() {

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
                                                System.out.println("数据清除完整");
                                                }
                                        });
                                    }

                                    @Override
                                    public void onWriteFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//
                                            }
                                        });
                                    }
                                });


                    }
                }, 1000);
            }
        });


        // 温度校准
        tempOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("这里是温度校准");
                tempOrderBtn.setEnabled(false);

                String hex = "";

                // 如果是单参数
                if(globalType == 0) {
                    hex = "fe0610";
                }

                // 如果是多参数
                if(globalType == 1) {
                    hex = "fe0600";
                }
                String t = tempOrder.getText().toString();
//                System.out.println("tttttttttttttt" + t);
//                System.out.println("tttttttttttttt" + t.split("/.").length);
//                System.out.println("tttttttttttttt" + t.split(".").length);
//                System.out.println("tttttttttttttt" + t.split("\\.").length);
                if (t.equals("")) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure), null).show();
                    tempOrderBtn.setEnabled(true);
                    return;
                }
                if(t.split("\\.").length > 2) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.decimal_number_error)).setPositiveButton(getString(R.string.ensure), null).show();
                    tempOrderBtn.setEnabled(true);
                    return;
                }
                if(t.split("\\.").length == 2 && t.split("\\.")[1].length() > 1) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.decimal_number_too_long)).setPositiveButton(getString(R.string.ensure), null).show();
                    tempOrderBtn.setEnabled(true);
                    return;
                }
                if(t.split("\\.").length == 2 && t.split("\\.")[1].length() == 0) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.decimal_number_equals_zero)).setPositiveButton(getString(R.string.ensure), null).show();
                    tempOrderBtn.setEnabled(true);
                    return;
                }
                if(t.equals("00") || t.equals("000") || t.equals("0000") || t.equals("00000") || t.equals("0.0") || t.equals("00.0") || t.equals("000.0")) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.data_format_error)).setPositiveButton(getString(R.string.ensure), null).show();
                    tempOrderBtn.setEnabled(true);
                    return;
                }

                progressDialog = new ProgressDialog(DeviceOrderSet2Activity.this);
                progressDialog.setIcon(R.mipmap.ic_launcher);
                progressDialog.setTitle(getString(R.string.temp_order));
                progressDialog.setMessage(getString(R.string.temp_order_writing));
                progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                progressDialog.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                progressDialog.show();
                handler.postDelayed(runnable, 12000);//每12秒执行一次runnable.

                tempOrderBtn.setBackground(drawable);
                tempOrderBtn.setEnabled(false);

                int temp = 0;
                if(tempOrder.getText().toString().split("\\.").length > 1) {
                    temp = Integer.parseInt(tempOrder.getText().toString().replace(".", ""));
                }else {
                    temp = Integer.parseInt(tempOrder.getText().toString()) * 10;
                }

                System.out.println("temp的值是：" + temp);
//                if(temp < 65536) {
//                    hex = hex + "0";
//                }
                if(temp < 4096) {
                    hex = hex + "0";
                }
                if(temp < 256) {
                    hex = hex + "0";
                }
                if(temp < 16) {
                    hex = hex + "0";
                }
                hex = hex + Integer.toHexString(temp);
//                hex = "fe" + tempOrder.getText().toString();
                System.out.println("hex的值是：" + hex);
                BleManager.getInstance().write(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        // 调用HexUtil进行格式转化
                        HexUtil.hexStringToBytes(hex),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("指令输入结果：");
                                        System.out.println("write success, current: " + current
                                                + " total: " + total
                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));


                                        tempOrderBtn.setBackground(drawableInit);
                                        tempOrderBtn.setEnabled(true);
                                        // 读取对应的数据
//                                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                BleManager.getInstance().notify(
                                                        bleDevice,
                                                        characteristicRead.getService().getUuid().toString(),
                                                        characteristicRead.getUuid().toString(),
                                                        new BleNotifyCallback() {

                                                            @Override
                                                            public void onNotifySuccess() {
                                                                System.out.println("notify指令输入成功");
                                                            }

                                                            @Override
                                                            public void onNotifyFailure(BleException exception) {
                                                                System.out.println("notify指令写入失败");
                                                            }

                                                            @Override
                                                            public void onCharacteristicChanged(byte[] data) {

                                                                runOnUiThread(() -> new Handler().postDelayed(() -> {

                                                                    System.out.println("指令函数");
                                                                    Date date = new Date();
                                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                    String dateString = formatter.format(date);
//                                                                    String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                    String s1 = HexUtil.byteToString(data);
                                                                    String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                    System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
                                                                    if(s1.equals("[OK]")) {
                                                                        progressDialog.setMessage(getString(R.string.write_success));
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                progressDialog.dismiss();
                                                                            }
                                                                        }, 800);

                                                                    }

                                                                    if(s1.equals("[Error]")) {
                                                                        progressDialog.setMessage(getString(R.string.write_fail));
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                progressDialog.dismiss();
                                                                            }
                                                                        }, 800);
                                                                    }

                                                                    tempOrderBtn.setBackground(drawableInit);
                                                                    tempOrderBtn.setEnabled(true);

                                                                },500));

                                                            }
                                                        });
                                            }
                                        },1000);
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("指令修改失败：" + exception.toString());
                                        progressDialog.setMessage(getString(R.string.write_fail));
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                tempOrderBtn.setBackground(drawableInit);
                                                tempOrderBtn.setEnabled(true);
                                                progressDialog.dismiss();
                                            }
                                        },800);
                                    }
                                });
                            }

                        });

            }
        });

        // 零点校准
        zeroOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                zeroOrderBtn.setEnabled(false);

                // 单参数,非ORP
                if(globalType == 0 && globalDeviceType != 4) {
                    if(globalDeviceIsConnect == false) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    String str1 = "00";
                    System.out.println("str1的值是：" + str1);
                    String strHex2 = "";
                    String strHex3 = "";

                    String str2 = zeroOrder.getText().toString();

                    if(TextUtils.isEmpty(str2)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }
                    if((Integer.parseInt(str2) > 65535) || (str2.contains("-"))) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    zeroOrderBtn.setBackground(drawable);
                    zeroOrderBtn.setEnabled(false);

                    // 计算数据
                    if(Integer.parseInt(str2) < 4096) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    if(Integer.parseInt(str2) < 256) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    if(Integer.parseInt(str2) < 16) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    System.out.println(strHex2 + ";" + strHex3 + ";" + Integer.toHexString(Integer.parseInt(str2)));
                    strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                    System.out.println("strHex2的值是：" + strHex2);
                    String hex = "fe06" + str1 + strHex2;
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            zeroOrderBtn.setEnabled(true);
                            orderSet(hex);
                        }
                    }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                }
//                System.out.println("这里测试：" + globalDeviceType);
                // ORP 类型
                if(globalDeviceType == 4 && globalType != 1) {
                    if(globalDeviceIsConnect == false) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    String str1 = "00";
                    System.out.println("str1的值是：" + str1);
                    String strHex2 = "";
                    String strHex3 = "";

                    String str2 = zeroOrder.getText().toString();

                    if(TextUtils.isEmpty(str2)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }
                    if(!str2.contains("-") && Integer.parseInt(str2) > 65535) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }
                    if(str2.contains("-") && !str2.equals("-40")) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    zeroOrderBtn.setBackground(drawable);
                    zeroOrderBtn.setEnabled(false);

                    if(str2.equals("-40")) {
                        str1 = "00FFD8";
                        strHex2 = "";
                    }else {
                        // 计算数据
                        if(Integer.parseInt(str2) < 4096) {
                            strHex2 = strHex2 + "0";
                            strHex3 = strHex3 + "0";
                        }
                        if(Integer.parseInt(str2) < 256) {
                            strHex2 = strHex2 + "0";
                            strHex3 = strHex3 + "0";
                        }
                        if(Integer.parseInt(str2) < 16) {
                            strHex2 = strHex2 + "0";
                            strHex3 = strHex3 + "0";
                        }
                        System.out.println(strHex2 + ";" + strHex3 + ";" + Integer.toHexString(Integer.parseInt(str2)));
                        strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                        System.out.println("strHex2的值是：" + strHex2);
                    }
                    String hex = "fe06" + str1 + strHex2;
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            zeroOrderBtn.setEnabled(true);
                            orderSet(hex);
                        }
                    }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                }
                // 多参数
                if (globalType == 1) {
                    if(!globalDeviceIsConnect) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    String str1 = "0000";
                    System.out.println("str1的值是：" + str1);
                    String strHex2 = "";

                    String str2 = zeroOrder.getText().toString();

                    if(TextUtils.isEmpty(str2)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }
                    if(!str2.contains("-") && (Integer.parseInt(str2) > 65535)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }
                    if(str2.contains("-") && !str2.equals("-40")) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    int index = sensorType.getSelectedItemPosition();
                    String[] values = getResources().getStringArray(R.array.sensor_select_1);
                    System.out.println("values是：" + values);


                    // 配置之后的多参数传感器
                    int[] type = ((MyApplication) getApplication()).getMutilSensor();
                    String[] nameList = new String[]{"", "", getString(R.string.data_EC), getString(R.string.data_PH), getString(R.string.data_ORP_0),
                            getString(R.string.data_DO), getString(R.string.data_NH4), getString(R.string.data_ZS),
                            getString(R.string.data_salinity), "", getString(R.string.data_residual_chlorine), getString(R.string.data_Chlorophyl),
                            getString(R.string.data_blue_green_algae), getString(R.string.data_Transparency), getString(R.string.data_Suspended_Solids),
                            getString(R.string.data_oil_in_water)};
                    String[] name = new String[]{getString(R.string.data_COD_0), getString(R.string.data_COD_ZS),
                            nameList[type[2]], nameList[type[3]], nameList[type[4]], nameList[type[5]], nameList[type[6]], nameList[type[7]]};

                    String order = "";
                    if(values[index].equals(name[0])) {
                        order = selectOrder(10); // COD 第一位 零点校准
                    }else if(values[index].equals(name[1])) {
                        order = selectOrder(12); // COD内置浊度 第二位 零点校准
                    }else if(values[index].equals(name[2])) {
                        order = selectOrder(14); // 电导率或盐度 第三位 零点校准
                    }else if(values[index].equals(name[3])) {
                        order = selectOrder(16); // PH 第四位 零点校准
                    }else if(values[index].equals(name[4])) {
                        order = selectOrder(18); // ORP 第五位 零点校准
                    }else if(values[index].equals(name[5])) {
                        order = selectOrder(20); // RDO 第六位 零点校准
                    }else if(values[index].equals(name[6])) {
                        order = selectOrder(22); // NHN 第七位 零点校准
                    }else if(values[index].equals(name[7])) {
                        order = selectOrder(24); // 浊度 第八位 零点校准
                    }else {
                        order = "0000";
                    }

                    System.out.println("临时：order的值是：" + order);

                    zeroOrderBtn.setBackground(drawable);
                    zeroOrderBtn.setEnabled(false);

                    if(str2.equals("-40") && !values[index].equals(name[4])) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        zeroOrderBtn.setEnabled(true);
                        return;
                    }

                    if(str2.equals("-40")) {
                        strHex2 = "FFD8";
                    }else {
                        if (Integer.parseInt(str2) < 4096) {
                            strHex2 = strHex2 + "0";
                        }
                        if (Integer.parseInt(str2) < 256) {
                            strHex2 = strHex2 + "0";
                        }
                        if (Integer.parseInt(str2) < 16) {
                            strHex2 = strHex2 + "0";
                        }
                        strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                        System.out.println("order的值是：" + order);
                    }
                    String hex = order + strHex2;
                    if(order.equals("0000")) {

                        // 读取对应的数据
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_error)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                zeroOrderBtn.setEnabled(true);
                                return;
                            }
                        }).show();
                    } else {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                orderSet(hex);
                            }
                        }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                    }
                    zeroOrderBtn.setEnabled(true);
                }



            }
        });

        // 斜率校准
        slopeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("这里是斜率校准");
                slopeOrderBtn.setEnabled(false);

                // 如果是单参数
                if(globalType == 0) {
                    if(globalDeviceIsConnect == false) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }

                    String str1 = "04";
                    System.out.println("str1的值是：" + str1);
                    String strHex2 = "";
                    String strHex3 = "";
                    String str2 = slopeOrder.getText().toString();
                    if(TextUtils.isEmpty(str2)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }
                    if((Integer.parseInt(str2) > 65535)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }

                    slopeOrderBtn.setBackground(drawable);
                    slopeOrderBtn.setEnabled(false);

                    if(Integer.parseInt(str2) < 4096) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    if(Integer.parseInt(str2) < 256) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    if(Integer.parseInt(str2) < 16) {
                        strHex2 = strHex2 + "0";
                        strHex3 = strHex3 + "0";
                    }
                    System.out.println(strHex2 + ";" + strHex3 + ";" + Integer.toHexString(Integer.parseInt(str2)));
                    strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                    System.out.println("strHex2的值是：" + strHex2);
                    String hex = "fe06" + str1 + strHex2;
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            orderSet(hex);
                        }
                    }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                    slopeOrderBtn.setEnabled(true);

                }

                // 如果是多参数
                if(globalType == 1) {
                    if(globalDeviceIsConnect == false) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }

                    String str1 = "0001";
                    System.out.println("str1的值是：" + str1);
                    String strHex2 = "";
                    String str2 = slopeOrder.getText().toString();
                    if(TextUtils.isEmpty(str2)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }
                    if((Integer.parseInt(str2) > 65535)) {
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }

                    slopeOrderBtn.setBackground(drawable);
                    slopeOrderBtn.setEnabled(false);

                    int index = sensorType.getSelectedItemPosition();
                    String[] values = getResources().getStringArray(R.array.sensor_select_1);
                    System.out.println("values是：" + values);

                    // 配置之后的多参数传感器
                    int[] type = ((MyApplication) getApplication()).getMutilSensor();
                    String[] nameList = new String[]{"", "", getString(R.string.data_EC), getString(R.string.data_PH), getString(R.string.data_ORP_0),
                            getString(R.string.data_DO), getString(R.string.data_NH4), getString(R.string.data_ZS),
                            getString(R.string.data_salinity), "", getString(R.string.data_residual_chlorine), getString(R.string.data_Chlorophyl),
                            getString(R.string.data_blue_green_algae), getString(R.string.data_Transparency), getString(R.string.data_Suspended_Solids),
                            getString(R.string.data_oil_in_water)};
                    String[] name = new String[]{getString(R.string.data_COD_0), getString(R.string.data_COD_ZS),
                            nameList[type[2]], nameList[type[3]], nameList[type[4]], nameList[type[5]], nameList[type[6]], nameList[type[7]]};

                    String order = "";
                    if(values[index].equals(name[0])) {
                        order = selectOrder(11);
                    }else if(values[index].equals(name[1])) {
                        order = selectOrder(13);
                    }else if(values[index].equals(name[2])) {
                        order = selectOrder(15);
                    }else if(values[index].equals(name[3])) {
                        order = selectOrder(17);
                    }else if(values[index].equals(name[4])) {
                        order = selectOrder(19);
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.invalid_argument)).setPositiveButton(getString(R.string.ensure),null).show();
                        slopeOrderBtn.setEnabled(true);
                        return;
                    }else if(values[index].equals(name[5])) {
                        order = selectOrder(21);
                    }else if(values[index].equals(name[6])) {
                        order = selectOrder(23);
                    }else if(values[index].equals(name[7])) {
                        order = selectOrder(25);
                    }else {
                        order = "0000";
                        // 读取对应的数据
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_error)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                slopeOrderBtn.setEnabled(true);
                                return;
                            }
                        }).show();
                    }

                    if(Integer.parseInt(str2) < 4096) {
                        strHex2 = strHex2 + "0";
                    }
                    if(Integer.parseInt(str2) < 256) {
                        strHex2 = strHex2 + "0";
                    }
                    if(Integer.parseInt(str2) < 16) {
                        strHex2 = strHex2 + "0";
                    }
                    strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                    String hex = order + strHex2;
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            orderSet(hex);
                        }
                    }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                    slopeOrderBtn.setEnabled(true);
                }


            }
        });

        // 特殊情况COD：内置浊度零点校准
        specialCODZeroOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                specialCODZeroOrderBtn.setEnabled(false);

                if(globalDeviceIsConnect == false) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODZeroOrderBtn.setEnabled(true);
                    return;
                }

                String str1 = "20";
                System.out.println("str1的值是：" + str1);
                String strHex2 = "";
                String strHex3 = "";

                String str2 = specialCODZeroOrder.getText().toString();

                if(TextUtils.isEmpty(str2)) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODZeroOrderBtn.setEnabled(true);
                    return;
                }
                if((Integer.parseInt(str2) > 65535)) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODZeroOrderBtn.setEnabled(true);
                    return;
                }

                specialCODZeroOrderBtn.setBackground(drawable);
                specialCODZeroOrderBtn.setEnabled(false);

                if(Integer.parseInt(str2) < 4096) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                if(Integer.parseInt(str2) < 256) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                if(Integer.parseInt(str2) < 16) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                System.out.println(strHex2 + ";" + strHex3 + ";" + Integer.toHexString(Integer.parseInt(str2)));
                strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                System.out.println("strHex2的值是：" + strHex2);
                String hex = "fe06" + str1 + strHex2;
                new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        specialCODZeroOrderBtn.setEnabled(true);
                        orderSet(hex);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();

            }
        });

        // 特殊情况COD：内置浊度斜率校准
        specialCODSlopeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                specialCODSlopeOrderBtn.setEnabled(false);

                if(globalDeviceIsConnect == false) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODSlopeOrderBtn.setEnabled(true);
                    return;
                }

                String str1 = "24";
                System.out.println("str1的值是：" + str1);
                String strHex2 = "";
                String strHex3 = "";
                String str2 = specialCODSlopeOrder.getText().toString();
                if(TextUtils.isEmpty(str2)) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODSlopeOrderBtn.setEnabled(true);
                    return;
                }
                if((Integer.parseInt(str2) > 65535)) {
                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    specialCODSlopeOrderBtn.setEnabled(true);
                    return;
                }

                specialCODSlopeOrderBtn.setBackground(drawable);
                specialCODSlopeOrderBtn.setEnabled(false);

                if(Integer.parseInt(str2) < 4096) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                if(Integer.parseInt(str2) < 256) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                if(Integer.parseInt(str2) < 16) {
                    strHex2 = strHex2 + "0";
                    strHex3 = strHex3 + "0";
                }
                System.out.println(strHex2 + ";" + strHex3 + ";" + Integer.toHexString(Integer.parseInt(str2)));
                strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                System.out.println("strHex2的值是：" + strHex2);
                String hex = "fe06" + str1 + strHex2;
                new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        orderSet(hex);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                specialCODSlopeOrderBtn.setEnabled(true);
            }
        });

        // 特殊情况PH：零点校准
        PHZeroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PHZeroButton.setEnabled(false);

                String hex = "fe06000000";
                new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PHZeroButton.setEnabled(true);
                        orderSet(hex);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();


            }
        });

        // 特殊情况PH：斜率校准
        PHSlopeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PHSlopeButton.setEnabled(false);

                String str1 = "00";
                System.out.println("str1的值是：" + str1);
                String strHex2 = "";

                PHSlopeButton.setBackground(drawable);
                PHSlopeButton.setEnabled(false);

                int index = PHSlopeSpinner.getSelectedItemPosition();
                String[] values = getResources().getStringArray(R.array.sensor_select_PH1);
                System.out.println("values是：" + values);

                String order = "";
                if(values[index].equals("4.00")) {
                    order = "02";
                }else if(values[index].equals("9.18")) {
                    order = "04";
                }

                System.out.println("strHex2的值是：" + strHex2);
                String hex = "fe06" + order + "0000";
                new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PHSlopeButton.setEnabled(true);
                        orderSet(hex);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                TextUtil.hideKeyboard(ev, view, DeviceOrderSet2Activity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // 指令写入
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void orderSet(String hex) {
        System.out.println("发送的指令为：" + hex);
        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                // 调用HexUtil进行格式转化
                HexUtil.hexStringToBytes(hex),
//                hex.getBytes(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("指令输入结果：");
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

//                                slopeOrderBtn.setBackground(drawableInit);
                                zeroOrderBtn.setBackground(drawableInit);

                                new Handler().postDelayed(new Runnable() {
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
                                                                System.out.println("notify success通知成功");
//                                                                                    addText(txt,"正在读取数据......");

                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("notify写入失败" + exception.toString());
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCharacteristicChanged(final byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                System.out.println("指令校准输出内容123");
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                String dateString = formatter.format(date);
//                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                String s1 = HexUtil.byteToString(data);
                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                System.out.println(s2);
                                                                System.out.println("非回调值：时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                                // 处理数据
                                                                if(s1.equals("[OK]")) {
                                                                    // 读取对应的数据
                                                                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                                                                }

                                                                if(s1.equals("[Error]")) {
                                                                    // 读取对应的数据
                                                                    new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                                                                }

//                                                                slopeOrderBtn.setEnabled(true);
                                                                zeroOrderBtn.setEnabled(true);


                                                            }
                                                        });
                                                    }

                                                });
                                    }
                                },200);
                            }
                        });
                    }

                    @Override
                    public void onWriteFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("指令修改失败：" + exception.toString());
                                new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                            }
                        });
                    }

                });
    }

    // 时间校准函数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void timeSet(int interval, int test) {
        String strHex1 = "";
        String strHex2 = "";

        if(interval < 16) {
            strHex1 = strHex1 + "0";
        }
        if(test < 16) {
            strHex2 = strHex2 + "0";
        }
        strHex1 = strHex1 + Integer.toHexString(interval);
        strHex2 = strHex2 + Integer.toHexString(test);
        System.out.println(strHex1 + ";;;;;" + strHex2);
        String hex ="fc" + strHex1 + strHex2;

        BleManager.getInstance().write(
                bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                // 调用HexUtil进行格式转化
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        System.out.println("write success, current: " + current
                                + " total: " + total
                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                        // 读取对应的数据
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                        timeBtn.setBackground(drawableInit);
                        timeBtn.setEnabled(true);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        System.out.println(exception.toString());
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                    }
                });
    }

    // 获取地址
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getDeviceAddress(int i) {
        String hex = "f900";

        progressDialogGet = new ProgressDialog(DeviceOrderSet2Activity.this);
        progressDialogGet.setIcon(R.mipmap.ic_launcher);
        progressDialogGet.setTitle(getString(R.string.get_address));
        progressDialogGet.setMessage(getString(R.string.get_address_writing));
        progressDialogGet.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
        progressDialogGet.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
        progressDialogGet.show();
        handler.postDelayed(runnable, 12000);//每12秒执行一次runnable.

        tempOrderBtn.setBackground(drawable);
        tempOrderBtn.setEnabled(false);

        BleManager.getInstance().write(bleDevice,
                characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(hex),
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {

                        System.out.println("write success, current: " + current
                                + " total: " + total
                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BleManager.getInstance().notify(bleDevice,
                                        characteristicRead.getService().getUuid().toString(),
                                        characteristicRead.getUuid().toString(),
                                        new BleNotifyCallback() {


                                            @Override
                                            public void onNotifySuccess() {
                                                System.out.println("notify调用成功");
                                            }

                                            @Override
                                            public void onNotifyFailure(BleException exception) {
                                                System.out.println("notify调用失败");
                                            }

                                            @Override
                                            public void onCharacteristicChanged(byte[] data) {
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        System.out.println("到这里了：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
                                                        if (HexUtil.formatHexString(characteristicRead.getValue(), true).charAt(HexUtil.formatHexString(characteristicRead.getValue(), true).length() - 1) != ',') {
                                                            System.out.println("设备地址数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
                                                            String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                            String s2 = s.substring(1, s.length() - 1);
                                                            int i = Integer.valueOf(s2.split(",")[0]);
                                                            System.out.println("i:" + i);
//                                                            ((MyApplication) getApplication()).setBasicDeviceAddress(i);
                                                            globalDeviceAddress = i;
                                                            if(i == 1) {
                                                                deviceAddress.setText(Integer.toString(globalDeviceAddress));
                                                            }
                                                            progressDialogGet.setMessage(getString(R.string.get_address_success));
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    progressDialogGet.dismiss();
                                                                    BleManager.getInstance().stopNotify(bleDevice,
                                                                            characteristicRead.getService().getUuid().toString(),
                                                                            characteristicRead.getUuid().toString());
                                                                }
                                                            }, 800);
                                                        }
                                                    }
                                                },1000);

                                            }
                                        });
                            }
                        }, 1000);

                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        System.out.println("写入读取地址的指令失败");
                        new AlertDialog.Builder(orderSetLayout2.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                        progressDialog.setMessage(getString(R.string.get_address_fail));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        }, 800);
                    }
                });
    }

    // 地址修改
    public void orderDeviceAddress() {
        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                int address = Integer.parseInt(deviceAddress.getText().toString());
                String hex = "";
                if (address < 16) {
                    hex = "0";
                }
                hex = hex + Integer.toHexString(address);
                hex = "f9" + hex;

//                       String hex = deviceAddress.getText().toString();

                System.out.println("hex的值是：" + hex);
//                        System.out.println("hex" + HexUtil.hexStringToBytes(hex));


                BleManager.getInstance().write(
                        bleDevice,
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        // 调用HexUtil进行格式转化
                        HexUtil.hexStringToBytes(hex),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                System.out.println("指令输入结果：");
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));


                                ((MyApplication)getApplication()).setBasicDeviceAddress(0);
                                // 读取对应的数据

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
                                                                System.out.println("notify success通知成功");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onNotifyFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("notify写入失败" + exception.toString());
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onCharacteristicChanged(final byte[] data) {
                                                        runOnUiThread(() -> new Handler().postDelayed(() -> {

                                                            System.out.println("指令函数");
                                                            Date date = new Date();
                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            String dateString = formatter.format(date);
//                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                            String s1 = HexUtil.byteToString(data);
                                                            String s2 = "时间:" + dateString + ",数值:" + s1;
                                                            System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                            System.out.println("比较" + s1.equals("[OK]") + ";长度：" + s1.length());
//                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                                //判断是否可以写入数据到系统
//                                                                if (!Settings.System.canWrite(orderSetLayout2.getContext())) {
//                                                                    Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                                                                    i.setData(Uri.parse("package:" + orderSetLayout2.getContext().getPackageName()));
//                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                                                    orderSetLayout2.getContext().startActivity(i);
//                                                                } else {
//                                                                    //处理逻辑
//                                                                    System.out.println("第一个if");
//                                                                }
//                                                            } else {
//                                                                //处理逻辑
//                                                            }

                                                            if(s1.equals("[OK]")) {
                                                                System.out.println("进入到if语句中");
                                                                progressDialog.setMessage(getString(R.string.write_success));
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        deviceAddressBtn.setEnabled(true);
                                                                        progressDialog.dismiss();
                                                                        globalDeviceAddress = address;
                                                                        ((MyApplication)getApplication()).setBasicDeviceAddress(address);
                                                                    }
                                                                }, 1200);

                                                            }
                                                            deviceAddressBtn.setBackground(drawableInit);
                                                            deviceAddressBtn.setEnabled(true);

                                                        },500));
                                                    }

                                                });

                                    }
                                },500);
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("指令修改失败：" + exception.toString());
                                progressDialog.setMessage(getString(R.string.write_fail));
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        deviceAddressBtn.setBackground(drawableInit);
                                        deviceAddressBtn.setEnabled(true);
                                        progressDialog.dismiss();
                                    }
                                },800);
                            }
                        });
            }
        }, addressAwaitTime);
    }

    // 指令
    public String selectOrder(int n) {
        switch (n){
            case 0:return "fe0300"; // 读取温度测量值
            case 1:return "fe0302"; // 读取COD测量值
            case 2:return "fe0304"; // 读取COD内置浊度测量值
            case 3:return "fe0306"; // 读取电导率/盐度测量值
            case 4:return "fe0308"; // 读取PH测量值
            case 5:return "fe030a"; // 读取ORP测量值
            case 6:return "fe030c"; // 读取溶解氧测量值
            case 7:return "fe030e"; // 读取NH4+测量值
            case 8:return "fe0310"; // 读取浊度测量值
            case 9:return "fe0600"; // 写入温度校准
            case 10:return "fe0602"; // 写入COD零点校准
            case 11:return "fe0603"; // 写入COD斜率校准
            case 12:return "fe0604"; // 写入COD内置浊度零点校准
            case 13:return "fe0605"; // 写入COD内置浊度斜率校准
            case 14:return "fe0606"; // 写入电导率/盐度零点校准
            case 15:return "fe0607"; // 写入电导率/盐度斜率校准
            case 16:return "fe0608"; // 写入PH零点校准
            case 17:return "fe0609"; // 写入PH斜率校准
            case 18:return "fe060a"; // 写入ORP零点校准
            case 19:return "fe060b"; // 写入ORP斜率校准
            case 20:return "fe060c"; // 写入溶解氧零点校准
            case 21:return "fe060d"; // 写入溶解氧斜率校准
            case 22:return "fe060e"; // 写入NH4+零点校准
            case 23:return "fe060f"; // 写入NH4+斜率校准
            case 24:return "fe0610"; // 写入浊度零点校准
            case 25:return "fe0611"; // 写入浊度斜率校准
        }
        return "";
    }

    public void initSpinnerValues() {

    }
}
