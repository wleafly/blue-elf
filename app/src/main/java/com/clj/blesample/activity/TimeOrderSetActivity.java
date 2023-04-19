package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.TextUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeOrderSetActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private BleDevice bleDevice;
    private BleManager bleManager;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;
    private int chipType = 0; // 蓝牙芯片类型

    private Boolean globalDeviceIsConnect; // 设备是否连接
    private String globalDeviceName; // 全局的设备名称
    private int globalDeviceAddress; // 全局的设备地址
    private int globalDeviceType; // 全局的设备类型
    private int globalType; // 单参数、多参数
    private int globalCleaningTime; // 清洗间隔时间
    private int globalCleaningCycles; // 清洗圈数
    private int globalIntervalTime; // 全局间隔时间
    private int globalTestTime; // 全局测试时间

    private EditText intervalTime;
    private EditText testTime;
    private EditText cleaningTime;
    private EditText cycles;

    private Button timeBtn;
    private Button cleaningTimeBtn;
    private Button cyclesBtn;
    private Button resetBtn;

    private View timeLayout;
    private View cleaningBrush;

    private ProgressDialog progressDialog;
    private Runnable runnable;

    private Drawable drawableInit;
    private Drawable drawable;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_order_set);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.time_order));
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


        timeLayout = findViewById(R.id.time_layout);
        cleaningBrush = findViewById(R.id.cleaning_brush);
        intervalTime = findViewById(R.id.interval_time);
        testTime = findViewById(R.id.test_time);
        cleaningTime = findViewById(R.id.cleaning_time);
        cycles = findViewById(R.id.cycles);

        timeBtn = findViewById(R.id.time_btn);
        cleaningTimeBtn = findViewById(R.id.cleaning_time_btn);
        cyclesBtn = findViewById(R.id.cycles_btn);
        resetBtn = findViewById(R.id.reset_btn);

        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication)getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication)getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication)getApplication()).getBasicDeviceType();
        globalType = ((MyApplication)getApplication()).getBasicType();
        globalCleaningTime = ((MyApplication)getApplication()).getCleaningTime();
        globalCleaningCycles = ((MyApplication)getApplication()).getCleaningCycles();
        globalIntervalTime = ((MyApplication)getApplication()).getIntervalTime();
        globalTestTime = ((MyApplication)getApplication()).getTestTime();

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

        drawableInit = getResources().getDrawable(R.drawable.setting_button_edit);
        drawable = getResources().getDrawable(R.color.gray1);

        // 给progressDialog加一个定时关闭的功能
        Handler handler=new Handler();
        runnable = new Runnable(){
            @Override
            public void run() {
                //要做的事情
//                if(progressDialog.isShowing()){
//                    progressDialog.dismiss();
//                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.operation_fail)).setPositiveButton(getString(R.string.ensure), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            handler.removeCallbacks(runnable);
//                            timeBtn.setBackground(drawableInit);
//                            timeBtn.setEnabled(true);
//                        }
//                    }).setCancelable(false).show();
//
//                }
                if(progressDialog.isShowing()) {
                    progressDialog.setMessage(getString(R.string.try_again_later));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            handler.removeCallbacks(runnable);
                            timeBtn.setBackground(drawableInit);
                            timeBtn.setEnabled(true);

                        }
                    }, 1000);
                }
            }
        };

        BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());

        // 如果间隔时间和测试时间有数据
        if(globalIntervalTime > -1) {
            intervalTime.setText(Integer.toString(globalIntervalTime));
            testTime.setText(Integer.toString(globalTestTime));
            intervalTime.setHint(Integer.toString(globalIntervalTime));
            testTime.setHint(Integer.toString(globalTestTime));
        }

        // 如果是多参数：获取自动清洗圈数和间隔时间
        if(globalType == 1 || globalDeviceType == 9 || globalDeviceType == 13 || globalDeviceType == 14) {
            cleaningBrush.setVisibility(View.VISIBLE);
            System.out.println("多参数清洗时间：" + globalCleaningTime);
            System.out.println("多参数清洗圈数:" + globalCleaningCycles);
            // 如果已经存储了清洗时间
            if(globalCleaningTime > -1) {
                cleaningTime.setText(Integer.toString(globalCleaningTime));
                cleaningTime.setHint(Integer.toString(globalCleaningTime));
                cycles.setText(Integer.toString(globalCleaningCycles));
                cycles.setHint(Integer.toString(globalCleaningCycles));
            }else {
                // 开始获取清洗时间
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getCleaningTime();
                    }
                }, 500);

                progressDialog = new ProgressDialog(this);
                progressDialog.setIcon(R.mipmap.ic_launcher);
                progressDialog.setTitle(getString(R.string.init_data));
                progressDialog.setMessage(getString(R.string.speed_gain_data));
                progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                progressDialog.setCancelable(false);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                progressDialog.show();
                handler.postDelayed(runnable, 15000);//每15秒执行一次runnable.

            }
        }


        // 时间校准
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timeBtn.setEnabled(false);

                if(globalDeviceIsConnect == false) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                    timeBtn.setEnabled(true);
                    return;
                }

                // 此时，通过自定义框中的数据进行指令写入
                if(intervalTime.getText().toString().equals("") || testTime.getText().toString().equals("")) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    timeBtn.setEnabled(true);
                    return;
                }
                int interval = Integer.parseInt(intervalTime.getText().toString());
                int test = Integer.parseInt(testTime.getText().toString());
                if((interval > 255) || (test > 255) || (interval <= 0) || (test <= 0)) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    timeBtn.setEnabled(true);
                    return;
                }
                if(interval < test) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.time_set_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    timeBtn.setEnabled(true);
                    return;
                }

                timeBtn.setBackground(drawable);

                new AlertDialog.Builder(timeLayout.getContext()).setMessage("确定要进行写入操作吗？").setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        timeSet(interval, test);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();

                timeBtn.setEnabled(true);

            }
        });

        // 设置自动清洗间隔时间
        cleaningTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cleaningTimeBtn.setEnabled(false);

                String data = cleaningTime.getText().toString();
                String strHex2 = "";

                if(TextUtils.isEmpty(data)) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    cleaningTimeBtn.setEnabled(true);
                    return;
                }
                if((Integer.parseInt(data) > 6000) || (Integer.parseInt(data) < 6)) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    cleaningTimeBtn.setEnabled(true);
                    return;
                }

                if(Integer.parseInt(data) < 4096) {
                    strHex2 = strHex2 + "0";
                }
                if(Integer.parseInt(data) < 256) {
                    strHex2 = strHex2 + "0";
                }
                if(Integer.parseInt(data) < 16) {
                    strHex2 = strHex2 + "0";
                }

                String hex = "f80600" + strHex2 + Integer.toHexString(Integer.parseInt(data));

                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        orderSet(hex, 1, Integer.parseInt(data));
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();

                cleaningTimeBtn.setEnabled(true);
            }
        });

        // 设置自动清洗圈数
        cyclesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cyclesBtn.setEnabled(false);

                String data = cycles.getText().toString();
                if(TextUtils.isEmpty(data)) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    cyclesBtn.setEnabled(true);
                    return;
                }
                if((Integer.parseInt(data) > 11) || (Integer.parseInt(data) < 0)) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    cyclesBtn.setEnabled(true);
                    return;
                }

                String hex = "f80601000" + Integer.toHexString(Integer.parseInt(data));

                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        orderSet(hex, 2, Integer.parseInt(data));
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();

                cyclesBtn.setEnabled(true);
            }
        });

        // 清洗刷重置
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resetBtn.setEnabled(false);

                String hex = "f0";
                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.select_write)).setPositiveButton(getString(R.string.ensure),new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        orderSet(hex, 3, 0);
                    }
                }).setNegativeButton(getString(R.string.cancel), null).setCancelable(true).show();
                resetBtn.setEnabled(true);
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
                        new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                        timeBtn.setBackground(drawableInit);
                        timeBtn.setEnabled(true);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        System.out.println(exception.toString());
                        new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                    }
                });
    }

    // 标准notify函数:void
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void orderSet(String hex, int n, int value) {
        System.out.println("发送的指令为：" + hex + ";" + value);
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
//                                zeroOrderBtn.setBackground(drawableInit);
//                                slopeOrderBtn.setEnabled(true);
//                                zeroOrderBtn.setEnabled(true);

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

                                                                System.out.println("指令校准输出内容");
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                String dateString = formatter.format(date);
//                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                String s1 = HexUtil.byteToString(data);
                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                                // 处理数据
                                                                if(s1.equals("[OK]")) {
                                                                    // 读取对应的数据
                                                                    if(n == 1) {
                                                                        // 说明是自动清洗时间
                                                                        globalCleaningTime = value;
                                                                        ((MyApplication)getApplication()).setCleaningTime(value);
                                                                    }else if(n == 2) {
                                                                        // 说明是自动清洗圈数
                                                                        globalCleaningCycles = value;
                                                                        ((MyApplication)getApplication()).setCleaningCycles(value);
                                                                    }else if(n == 3) {
                                                                        // 说明是清洁刷重置
                                                                        cleaningTime.setText("30");
                                                                        cleaningTime.setHint("30");
                                                                        globalCleaningTime = 30;
                                                                        ((MyApplication)getApplication()).setCleaningTime(30);

                                                                        cycles.setText("3");
                                                                        cycles.setHint("3");
                                                                        globalCleaningCycles = 3;
                                                                        ((MyApplication)getApplication()).setCleaningCycles(3);
                                                                    }
                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                                                                }

                                                                if(s1.equals("[Error]")) {
                                                                    // 读取对应的数据
                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                                                                }

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
                                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                            }
                        });
                    }

                });
    }

    // 获取清洗间隔时间函数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getCleaningTime() {
        String hex = "f803000000";

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

                                                                System.out.println("指令校准输出内容");
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                String dateString = formatter.format(date);
//                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                String s1 = HexUtil.byteToString(data);
                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

//                                                                System.out.println(judgeCleaning(s1));
                                                                // 获取数据
                                                                if(s1.split(",").length == 2  && s1.charAt(0) == '[' && s1.charAt(s1.length() - 2) == ',' && s1.charAt(s1.length() - 1) == ']') {
                                                                    s1 = s1.replaceAll("[\\[\\]]", "");

                                                                    cleaningTime.setText(s1.split(",")[0]);
                                                                    cleaningTime.setHint(s1.split(",")[0]);
                                                                    globalCleaningTime = Integer.parseInt(s1.split(",")[0]);
                                                                    ((MyApplication)getApplication()).setCleaningTime(globalCleaningTime);

                                                                    BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());

                                                                    if(globalCleaningCycles == -1) {
                                                                        progressDialog.setMessage(getString(R.string.get_cleaning_time_success));
                                                                        // 如果没有存储设备的清洗圈数
                                                                        new Handler().postDelayed(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                getCleaningCycles();
                                                                            }
                                                                        }, 1500);
                                                                    }else {
                                                                        cycles.setText(globalCleaningCycles);
                                                                        cycles.setHint(globalCleaningCycles);
                                                                    }

                                                                }

                                                                // 处理数据
//                                                                if(s1.equals("OK")) {
//                                                                    // 读取对应的数据
//                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                                }
//
//                                                                if(s1.equals("Error")) {
//                                                                    // 读取对应的数据
//                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                                }

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
                                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                            }
                        });
                    }

                });
    }

    // 获取清洗圈数函数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getCleaningCycles() {
        String hex = "f803010000";
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
//                                zeroOrderBtn.setBackground(drawableInit);
//                                slopeOrderBtn.setEnabled(true);
//                                zeroOrderBtn.setEnabled(true);

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

                                                                System.out.println("指令校准输出内容");
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                String dateString = formatter.format(date);
//                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                String s1 = HexUtil.byteToString(data);
                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                                // 获取数据
                                                                if(s1.split(",").length == 2  && s1.charAt(0) == '[' && s1.charAt(s1.length() - 2) == ',' && s1.charAt(s1.length() - 1) == ']') {
                                                                    s1 = s1.replaceAll("[\\[\\]]", "");

                                                                    cycles.setText(s1.split(",")[0]);
                                                                    cycles.setHint(s1.split(",")[0]);

                                                                    globalCleaningCycles = Integer.parseInt(s1.split(",")[0]);
                                                                    ((MyApplication)getApplication()).setCleaningCycles(globalCleaningCycles);

                                                                    progressDialog.setMessage(getString(R.string.get_cleaning_cycles_success));
                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            progressDialog.dismiss();
                                                                        }
                                                                    }, 500);

                                                                    BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());

                                                                }

                                                                // 处理数据
//                                                                if(s1.equals("OK")) {
//                                                                    // 读取对应的数据
//                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                                }
//
//                                                                if(s1.equals("Error")) {
//                                                                    // 读取对应的数据
//                                                                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                                }

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
                                new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                            }
                        });
                    }

                });
    }


    // 点击空白隐藏软键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                TextUtil.hideKeyboard(ev, view, TimeOrderSetActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public Boolean judgeCleaning(String s1) {
        String digits = s1.replaceAll("[^0-9,]", "");
        if(digits.length() != s1.length()) {
            return false;
        }
        return true;
    }

}
