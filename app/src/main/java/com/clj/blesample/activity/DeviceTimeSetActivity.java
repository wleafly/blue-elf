package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.TextUtil;

public class DeviceTimeSetActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private BleDevice bleDevice;
    private BleManager bleManager;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;

    private Boolean globalDeviceIsConnect; // 设备是否连接
    private String globalDeviceName; // 全局的设备名称
    private int globalDeviceAddress; // 全局的设备地址
    private int globalDeviceType; // 全局的设备类型

    private int intervalTime = 0;
    private int testTime = 0;
    private View timeSetLayout;
    private Drawable drawable;
    private Drawable drawableOld;
    private TextView interval_time;
    private TextView test_time;

    private Button btnFiveMinute;
    private Button btnTenMinute;
    private Button btnHalfHour;
    private Button btnPerHour;
    private Button btnTwoHour;
    private Button btnFourHour;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_time_set);

        timeSetLayout = findViewById(R.id.time_set_layout);
        TextView deviceName = findViewById(R.id.device_name);

        // 自定义时间按钮
        btnFiveMinute = (Button) findViewById(R.id.btn_five_minute);
        btnTenMinute = (Button) findViewById(R.id.btn_ten_minute);
        btnHalfHour = (Button) findViewById(R.id.btn_half_hour);
        btnPerHour= (Button) findViewById(R.id.btn_per_hour);
        btnTwoHour = (Button) findViewById(R.id.btn_two_hour);
        btnFourHour = (Button) findViewById(R.id.btn_four_hour);

        // 快速选择框和自定义框的layout
        View timeBtn1 = findViewById(R.id.time_btn_1);
        View timeBtn2 = findViewById(R.id.time_btn_2);

        // 快速选择框的自定义框的图表
        final ImageView timeSet1 = findViewById(R.id.time_set_1);
        final ImageView timeSet2 = findViewById(R.id.time_set_2);

        // 自定义框中的间隔时间和测试时间
        interval_time = findViewById(R.id.interval_time); // 间隔时间
        test_time = findViewById(R.id.test_time); // 测试时间

        // 把6个按钮和2个输入框分成四个layout
        final View list1 = findViewById(R.id.list1);
        final View list2 = findViewById(R.id.list2);
        final View list3 = findViewById(R.id.list3);
        final View list4 = findViewById(R.id.list4);

        // 确认按钮
        Button btnSubmit = findViewById(R.id.btn_submit);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.time_set_activity));
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
                if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                    service = s;
                }
            }
        }
        BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
        int ii = 0;
        System.out.println("service相关的值是：" + service.getUuid());
        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
            // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
//            String uid = c.getUuid().toString().split("-")[0];
            cs[ii++] = c;
//            System.out.println("特征值值内容：" + uid);
//            System.out.println("将特征属性加入到mResultAdapter:" + characteristic.getProperties());
//            if ("0000fff1".equals(uid)) {
//                characteristicRead = c;
//            } else if ("0000fff2".equals(uid)) {
//                characteristic = c;
//            } else if ("0000fff3".equals(uid)) {
//                characteristic3 = c;
//            }
        }
        characteristic = cs[0];
        characteristicRead = cs[1];
        System.out.println("characteristic的数据--->getUuid:" + characteristic.getUuid().toString());
        System.out.println("characteristicRead的数据--->getUuid:" + characteristicRead.getUuid().toString());

        // 按钮样式
        drawable = getResources().getDrawable(R.drawable.button_style_real_time_data);
        drawableOld = btnFiveMinute.getBackground();

        deviceName.setText(getString(R.string.device_name) + globalDeviceName);

        // 切换快捷时间选择按钮
        timeBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list1.getVisibility() == View.VISIBLE) {
                    list1.setVisibility(View.GONE);
                    list2.setVisibility(View.GONE);
                    list3.setVisibility(View.GONE);
                    timeSet1.setImageResource(R.mipmap.down);
                }else if(list1.getVisibility() == View.GONE) {
                    list1.setVisibility(View.VISIBLE);
                    list2.setVisibility(View.VISIBLE);
                    list3.setVisibility(View.VISIBLE);
                    timeSet1.setImageResource(R.mipmap.up);
                }
            }
        });

        // 切换自定义选择按钮
        timeBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(list4.getVisibility() == View.VISIBLE) {
                    list4.setVisibility(View.GONE);
                    timeSet2.setImageResource(R.mipmap.down);
                }else if(list4.getVisibility() == View.GONE) {
                    timeSet2.setImageResource(R.mipmap.up);
                    list4.setVisibility(View.VISIBLE);
                }
            }
        });

        // 自定义选择按钮的点击事件
        // 五分钟记录一次数据
        btnFiveMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnFiveMinute.getBackground()) {
                    setTime2(5, 2);
                }else{
                    setTime1(5, 2);
                }
            }
        });

        // 十分钟记录一次数据
        btnTenMinute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnTenMinute.getBackground()) {
                    setTime2(10, 2);
                }else{
                    setTime1(10, 2);
                }
            }
        });

        // 半小时记录一次数据
        btnHalfHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnHalfHour.getBackground()) {
                    setTime2(30, 2);
                }else{
                    setTime1(30, 2);
                }
            }
        });

        // 一小时记录一次数据
        btnPerHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnPerHour.getBackground()) {
                    setTime2(60, 2);
                }else{
                    setTime1(60, 2);
                }
            }
        });

        // 两小时记录一次数据
        btnTwoHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnTwoHour.getBackground()) {
                    setTime2(120, 2);
                }else{
                    setTime1(120, 2);
                }
            }
        });

        // 四小时记录一次数据
        btnFourHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawable == btnFourHour.getBackground()) {
                    setTime2(240, 2);
                }else{
                    setTime1(240, 2);
                }
            }
        });

        // 触发间隔时间输入框
        interval_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime2(0, 0);
            }
        });

        // 触发测试时间输入框
        test_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTime2(0, 0);
            }
        });

        // 进行数据的传递
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(intervalTime + ";;;;;;;;;;;;;;" + testTime);
                if(intervalTime == 0 && testTime == 0) {
                    // 此时，通过自定义框中的数据进行指令写入
                    if(interval_time.getText().toString().equals("") || test_time.getText().toString().equals("")) {
                        new AlertDialog.Builder(timeSetLayout.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        return;
                    }
                    int a = Integer.parseInt(interval_time.getText().toString());
                    int b = Integer.parseInt(test_time.getText().toString());
                    if((a > 255) || (b > 255) || (a < 0) || (b < 0)) {
                        new AlertDialog.Builder(timeSetLayout.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        return;
                    }
                    if(a < b) {
                        new AlertDialog.Builder(timeSetLayout.getContext()).setMessage(getString(R.string.time_set_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                        return;
                    }
                    intervalTime = Integer.parseInt(interval_time.getText().toString());
                    testTime = Integer.parseInt(test_time.getText().toString());
                }
                System.out.println("intervalTime的值是：" + intervalTime);
                System.out.println("testTime的值是：" + testTime);

                String strHex1 = "";
                String strHex2 = "";

                if(intervalTime < 16) {
                    strHex1 = strHex1 + "0";
                }
                if(testTime < 16) {
                    strHex2 = strHex2 + "0";
                }
                strHex1 = strHex1 + Integer.toHexString(intervalTime);
                strHex2 = strHex2 + Integer.toHexString(testTime);
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
                                new AlertDialog.Builder(timeSetLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println(exception.toString());
                                new AlertDialog.Builder(timeSetLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                            }
                        });

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTime1(int a, int b) {
        intervalTime = a;
        testTime = b;
        interval_time.setText("");
        test_time.setText("");
        btnFiveMinute.setBackground(drawableOld);
        btnTenMinute.setBackground(drawableOld);
        btnHalfHour.setBackground(drawableOld);
        btnPerHour.setBackground(drawableOld);
        btnTwoHour.setBackground(drawableOld);
        btnFourHour.setBackground(drawableOld);
        if(a == 5) {
            btnFiveMinute.setBackground(drawable);
        }else if(a == 10) {
            btnTenMinute.setBackground(drawable);
        }else if(a == 30) {
            btnHalfHour.setBackground(drawable);
        }else if(a == 60) {
            btnPerHour.setBackground(drawable);
        }else if(a == 120) {
            btnTwoHour.setBackground(drawable);
        }else if(a == 240) {
            btnFourHour.setBackground(drawable);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTime2(int a, int b) {
        intervalTime = 0;
        testTime = 0;
        interval_time.setText("");
        test_time.setText("");
        btnFiveMinute.setBackground(drawableOld);
        btnTenMinute.setBackground(drawableOld);
        btnHalfHour.setBackground(drawableOld);
        btnPerHour.setBackground(drawableOld);
        btnTwoHour.setBackground(drawableOld);
        btnFourHour.setBackground(drawableOld);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                TextUtil.hideKeyboard(ev, view, DeviceTimeSetActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("DeviceTimeSetActivity数据销毁");
        intervalTime = 0;
        testTime = 0;
    }
}
