package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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


public class DeviceNameSetActivity extends AppCompatActivity {

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
    private int chipType = 0; // 芯片类型

    private ProgressDialog progressDialogReal; // 提示框

    private Toolbar toolbar;
    private TextView deviceName;
    private TextView deviceNewName;
    private ImageView deviceImage;
    private Button btn;
    private View nameSet;
    private TextView numberOfWords;

    private TextView baochang;
    private Button bcBtn;
    private Button bcResetBtn;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_name_set);

        System.out.println("DeviceNameSetActivity的onCreate()方法");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.name_set_activity));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        deviceName = findViewById(R.id.device_name);
        deviceNewName = findViewById(R.id.device_new_name);
        deviceImage = findViewById(R.id.device_image);
        btn = findViewById(R.id.btn);
        nameSet = findViewById(R.id.name_set);
        numberOfWords = findViewById(R.id.number_words);

        baochang = findViewById(R.id.device_baochang);
        bcBtn = findViewById(R.id.btn_baochang);
        bcResetBtn = findViewById(R.id.btn_moreng);



//        deviceNewName.setKeyListener(DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"));

        // 获取全局数据
        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        globalDeviceName = ((MyApplication)getApplication()).getBasicDeviceName();
        globalDeviceAddress = ((MyApplication)getApplication()).getBasicDeviceAddress();
        globalDeviceType = ((MyApplication)getApplication()).getBasicDeviceType();

        bleDevice = ((MyApplication) getApplication()).getBasicBleDevice();
        bleManager = ((MyApplication) getApplication()).getBasicBleManager();
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

        // 设置导航（返回图片）的点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 退出当前页面
                finish();
            }
        });

        final String confirmPwd = "<PWD123456>";  // 设备初始化密码
//        final String getNameheader = "<NAME";
        final String getNameheader = "<";
//
//        final String getNameheader = "<MTU60";
        final String getNameBottom = ">";
        byte[] b = confirmPwd.getBytes();

        if(chipType == 2) {
            BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    b,
                    new BleWriteCallback() {

                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            System.out.println("pwd write success, current: " + current
                                    + " total: " + total
                                    + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                            // 写入成功获取数据
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    BleManager.getInstance().read(
                                            bleDevice,
                                            characteristic.getService().getUuid().toString(),
                                            characteristic.getUuid().toString(),
                                            new BleReadCallback() {
                                                //Ble读回调
                                                @Override
                                                public void onReadSuccess(final byte[] data) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Date date = new Date();
                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                            String dateString = formatter.format(date);
//                                                            String s = HexUtil.formatHexString(characteristic.getValue(), true);
                                                            String s = HexUtil.byteToString(data);
                                                            String s1 = "时间:" + dateString + ",数值:" + s;
                                                            Log.e("test", s1);
                                                            System.out.println(s1);

                                                            // 将数据写到对应sd卡下的蓝牙文件夹下
                                                            // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                        }

                                                    });
                                                }

                                                @Override
                                                public void onReadFailure(final BleException exception) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            System.out.println("密码写入失败：" + exception.toString());
                                                        }
                                                    });
                                                }
                                            });
                                }
                            },500);
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.password_write_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                        }
                    });
        }

        progressDialogReal = new ProgressDialog(this);
        progressDialogReal.setIcon(R.mipmap.ic_launcher);
        progressDialogReal.setTitle(getString(R.string.name_set_activity));
        progressDialogReal.setMessage(getString(R.string.device_name_being_modify));
        progressDialogReal.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
        progressDialogReal.setCancelable(true);// 点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭

        // 这里写修改设备名称的业务功能
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 获取用户添加的新的设备名称
                String name = deviceNewName.getText().toString();
                System.out.println("name的值是：" + name);
                if (TextUtils.isEmpty(name)) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_not_empty)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

                if (name.length() > 18) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_too_long)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

                if (name.length() < 3) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_too_short)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

//                if(name.equals("EXOPacketSet")) {
//                    // 包长设置
//                    EXOPacketSet();
//                    return;
//                }

                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new String(getNameheader + name + getNameBottom).getBytes(),
                        new BleWriteCallback() {


                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true) + characteristic.getUuid().toString());

                                progressDialogReal.show();
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        progressDialogReal.setMessage(getString(R.string.name_set_activity) + getString(R.string.success));
//                                    }
//                                },500);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialogReal.setMessage(getString(R.string.device_name_set_success2));
                                    }
                                },1500);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialogReal.dismiss();
                                        BleManager.getInstance().disconnect(bleDevice);
                                    }
                                },3000);
                                // new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_set_success)).setPositiveButton(getString(R.string.ensure),null).show();


                                // 写入成功获取数据
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        BleManager.getInstance().read(
//                                                bleDevice,
//                                                characteristic.getService().getUuid().toString(),
//                                                characteristic.getUuid().toString(),
//                                                new BleReadCallback() {
//                                                    //Ble读回调
//                                                    @Override
//                                                    public void onReadSuccess(final byte[] data) {
//                                                        runOnUiThread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                Date date = new Date();
//                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                                String dateString = formatter.format(date);
//                                                                String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                                String s1 = "时间:" + dateString + ",数值:" + s;
//                                                                Log.e("test", s1);
//                                                                System.out.println(s1);
//
//                                                                // 将数据写到对应sd卡下的蓝牙文件夹下
//                                                                // MyLog.writeLogToReadFile(s1);
////                                                                              addText(txt, HexUtil.formatHexString(data, true));
//                                                                new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_set_success)).setPositiveButton(getString(R.string.ensure),null).show();
//                                                                deviceNewName.setText("");
//                                                            }
//
//                                                        });
//                                                    }
//
//                                                    @Override
//                                                    public void onReadFailure(final BleException exception) {
//                                                        runOnUiThread(new Runnable() {
//                                                            @Override
//                                                            public void run() {
//                                                                System.out.println("更新新的设备名称失败：" + exception.toString());
//                                                            }
//                                                        });
//                                                    }
//                                                });
//                                    }
//                                },500);


                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("写入设备名称：" + exception.toString());
                            }
                        });

            }
        });


        // 修改包长
        bcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 获取用户添加的新的设备名称
                String name = baochang.getText().toString();
                System.out.println("name的值是：" + name);
                if (TextUtils.isEmpty(name)) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_not_empty)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

                if (name.length() > 3) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_too_long)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

                if (name.length() == 0) {
                    new AlertDialog.Builder(nameSet.getContext()).setMessage(getString(R.string.device_name_too_short)).setPositiveButton(getString(R.string.resume_load),null).show();
                    return;
                }

                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        new String("<MTU" + name + ">").getBytes(),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                System.out.println("write success, current: " + current
                                        + " total: " + total
                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                // 写入成功获取数据
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        BleManager.getInstance().read(
                                                bleDevice,
                                                characteristic.getService().getUuid().toString(),
                                                characteristic.getUuid().toString(),
                                                new BleReadCallback() {
                                                    //Ble读回调
                                                    @Override
                                                    public void onReadSuccess(final byte[] data) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                String dateString = formatter.format(date);
//                                                                String s = HexUtil.formatHexString(characteristic.getValue(), true);

                                                                String s = HexUtil.byteToString(data);
                                                                String s1 = "时间:" + dateString + ",数值:" + s;
                                                                Log.e("test", s1);
                                                                System.out.println(s1);

                                                                // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                                new AlertDialog.Builder(nameSet.getContext()).setMessage("更新成功").setPositiveButton(getString(R.string.ensure),null).show();
                                                                baochang.setText("");
                                                            }

                                                        });
                                                    }

                                                    @Override
                                                    public void onReadFailure(final BleException exception) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                System.out.println("更新包长失败：" + exception.toString());
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                },500);


                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("写入设备名称：" + exception.toString());
                            }
                        });

            }
        });

        // 重置
        bcResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceNewName.setText("EXOmini1A");
                baochang.setText("60");
            }
        });



        // 设置监听名称输入框的字数问题
        deviceNewName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.length() + "/18";
                numberOfWords.setText(str);
            }
        });




    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("DeviceNameSetActivity的onResume()的方法");
    }

    // 点击空白隐藏软键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                TextUtil.hideKeyboard(ev, view, DeviceNameSetActivity.this);//调用方法判断是否需要隐藏键盘
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // 包长设置函数
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void EXOPacketSet() {
        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                new String("<MTU60>").getBytes(),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        System.out.println("write success, current: " + current
                                + " total: " + total
                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                        // 写入成功获取数据
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BleManager.getInstance().read(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleReadCallback() {
                                            //Ble读回调
                                            @Override
                                            public void onReadSuccess(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
                                                        String s = HexUtil.byteToString(data);
                                                        String s1 = "时间:" + dateString + ",数值:" + s;
                                                        Log.e("test", s1);
                                                        System.out.println(s1);

                                                        // 将数据写到对应sd卡下的蓝牙文件夹下
                                                        // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                        new AlertDialog.Builder(nameSet.getContext()).setMessage("包长更新成功").setPositiveButton(getString(R.string.ensure),null).show();
                                                        baochang.setText("");
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onReadFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        System.out.println("更新包长失败：" + exception.toString());
                                                        new AlertDialog.Builder(nameSet.getContext()).setMessage("包长更新失败").setPositiveButton(getString(R.string.ensure),null).show();
                                                    }
                                                });
                                            }
                                        });
                            }
                        },500);


                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        System.out.println("写入设备名称：" + exception.toString());
                    }
                });
    }
}

