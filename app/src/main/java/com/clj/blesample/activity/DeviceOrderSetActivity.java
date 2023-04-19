package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;


import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.tab.RealDataFragment;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.TextUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceOrderSetActivity extends AppCompatActivity {

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_order_set);

        final TextView et1 = findViewById(R.id.et1);
        final TextView et2 = findViewById(R.id.et2);
        final View orderSetLayout = findViewById(R.id.order_set_layout);
        final Button btn = findViewById(R.id.btn_submit);
        TextView deviceType = findViewById(R.id.device_type);

        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication)getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication)getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication)getApplication()).getBasicDeviceType();

        bleDevice = ((MyApplication)getApplication()).getBasicBleDevice();
        bleManager = ((MyApplication)getApplication()).getBasicBleManager();
        gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        if(gatt != null) {
            for(BluetoothGattService s : gatt.getServices()) {
                service = s;
            }
        }
        System.out.println("service相关的值是：" + service.getUuid());
        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
            // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
            String uid = c.getUuid().toString().split("-")[0];
            System.out.println("特征值值内容：" + uid);
//            System.out.println("将特征属性加入到mResultAdapter:" + characteristic.getProperties());
            if("0000fff1".equals(uid)) {
                characteristicRead = c;
            }else if("0000fff2".equals(uid)) {
                characteristic = c;
            }
        }
        if(!getDeviceType(globalDeviceAddress).equals("----")) {
            deviceType.setText(getDeviceType(globalDeviceAddress) + "传感器");
        }else {
            String hex = "f9";
            System.out.println("开始读取设备地址指令");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                            characteristic.getUuid().toString(),
                            HexUtil.hexStringToBytes(hex),
                            new BleWriteCallback() {

                                @Override
                                public void onWriteSuccess(int current, int total, byte[] justWrite) {

                                    System.out.println("读取设备地址指令写入成功");
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
                                                                    new AlertDialog.Builder(orderSetLayout.getContext()).setMessage("读取设备地址失败，请稍后重试").setPositiveButton("确定", null).setCancelable(false).show();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onCharacteristicChanged(final byte[] data) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    System.out.println("开始读取地址信息...");
                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Date date = new Date();
                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                            String dateString = formatter.format(date);
                                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                            String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                            System.out.println("s2的值：" + s2);
                                                                            if (HexUtil.formatHexString(characteristicRead.getValue(), true).charAt(HexUtil.formatHexString(characteristicRead.getValue(), true).length() - 1) != ',') {
                                                                                System.out.println("时间：" + dateString + "，设备地址数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                                                globalDeviceType = Integer.parseInt(s1);
                                                                                deviceType.setText(getDeviceType(Integer.parseInt(s1)) + getString(R.string.sensor));
                                                                                ((MyApplication)getApplication()).setBasicDeviceType(globalDeviceType);
                                                                                ((MyApplication)getApplication()).setBasicDeviceAddress(Integer.parseInt(s1));

                                                                            }
                                                                        }
                                                                    },1500);


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
            },500);
        }
        deviceType.setText(getDeviceType(globalDeviceAddress) + "传感器");

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

        et1.setInputType(InputType.TYPE_NULL);
        et1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(orderSetLayout.getContext());
                builder.setTitle(getString(R.string.select_correct_type));
                final String[] correct = {getString(R.string.zero_calibration), getString(R.string.slope_calibration)};
                builder.setItems(correct, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        et1.setText(correct[which]);
                    }
                });
                builder.show();
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str1 = "";
                System.out.println("输入框的校准类型是：" + et1.getText());
                if(et1.getText().toString().equals(getString(R.string.zero_calibration))) {
                    str1 = "0000";
                }else if(et1.getText().toString().equals(getString(R.string.slope_calibration))) {
                    str1 = "0001";
                }
                System.out.println("str1的值是：" + str1);
                String strHex2 = "";
                String str2 = et2.getText().toString();
                if(TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
                    new AlertDialog.Builder(orderSetLayout.getContext()).setMessage(getString(R.string.empty_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    return;
                }
                if((Integer.parseInt(str2) < 0) || (Integer.parseInt(str2) > 65535)) {
                    new AlertDialog.Builder(orderSetLayout.getContext()).setMessage(getString(R.string.out_to_resume_load)).setPositiveButton(getString(R.string.ensure),null).show();
                    return;
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
                System.out.println("strHex2的值是：" + strHex2);
                String hex ="fe" + str1 + strHex2;
                System.out.println("hex的值是：" + hex);
                System.out.print("strHex2的最终值是：" );
                for(byte b : HexUtil.hexStringToBytes(hex)){
                    System.out.println(b);
                }
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

                                        // 读取对应的数据
                                        new AlertDialog.Builder(orderSetLayout.getContext()).setMessage(getString(R.string.order_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
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

                                                                        System.out.println("历史数据函数");
                                                                        Date date = new Date();
                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                        String dateString = formatter.format(date);
                                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                        System.out.println("时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

                                                                    }
                                                                });
                                                            }

                                                        });
                                            }
                                        },2000);
                                    }
                                });
                            }

                            @Override
                            public void onWriteFailure(final BleException exception) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("指令修改失败：" + exception.toString());
                                        new AlertDialog.Builder(orderSetLayout.getContext()).setMessage(getString(R.string.order_set_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                                    }
                                });
                            }

                        });

            }
        });



    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                TextUtil.hideKeyboard(ev, view, DeviceOrderSetActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // 获取设备类型
    public String getDeviceType(int a) {
        if(a == 1) {
            return getString(R.string.data_PH);
        }else if(a == 2) {
            return getString(R.string.data_EC);
        }else if(a == 3) {
            return getString(R.string.data_DO);
        }else if(a == 4) {
            return getString(R.string.data_NH);
        }else if(a == 5) {
            return getString(R.string.data_ZS);
        }
        return "----";
    }


}

