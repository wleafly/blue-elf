package com.clj.blesample.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.utils.BackEndUtil;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmDeviceActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private BleDevice bleDevice;
    private BleManager bleManager;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattCharacteristic characteristic3;
    private int chipType = 0; // 芯片类型

    // 测试使用
    private TextView testResult;
    private EditText orderInput;
    private Button testBtn;
    private TextView testResult2;
    private EditText orderInput2;
    private Button testBtn2;
    private TextView testResult3;
    private EditText orderInput3;
    private Button testBtn3;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_device);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.alarm_activity));
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

        // 操作Operation来获取设备的实时数据等情况
        // 获得这个设备的特性
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
        System.out.println("characteristic的数据--->getUuid:" + characteristic.getUuid().toString());
        System.out.println("characteristicRead的数据--->getUuid:" + characteristicRead.getUuid().toString());


        // 测试
        orderInput = findViewById(R.id.test_edit);
        testResult = findViewById(R.id.test_result);
        testBtn = findViewById(R.id.test_btn5);
        orderInput2 = findViewById(R.id.test_edit_2);
        testResult2 = findViewById(R.id.test_result_2);
        testBtn2 = findViewById(R.id.test_btn6);
        orderInput3 = findViewById(R.id.test_edit_3);
        testResult3 = findViewById(R.id.test_result_3);
        testBtn3 = findViewById(R.id.test_btn7);

        // 测试使用
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testResult.setText("");
                final String hex = orderInput.getText().toString();

//                new Handler().postDelayed(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//                    @Override
//                    public void run() {


                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                HexUtil.hexStringToBytes(hex),
                                new BleWriteCallback() {

                                    @Override
                                    public void onWriteSuccess(int current, int total, byte[] justWrite) {

                                        System.out.println("notify指令发送成功:" + hex);


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
                                                                Date date = new Date();
                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                                                String dateString = formatter.format(date);
                                                                System.out.println("开始：" + dateString);
                                                            }

                                                            @Override
                                                            public void onNotifyFailure(BleException exception) {
                                                                System.out.println("notify发送失败");
                                                            }

                                                            @Override
                                                            public void onCharacteristicChanged(byte[] data) {

                                                                runOnUiThread(() -> {
                                                                    System.out.println("开始读取notify回调信息...");
//                                                                        new Handler().postDelayed(new Runnable() {
//                                                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//                                                                            @Override
//                                                                            public void run() {
                                                                            Date date = new Date();
                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                                                            String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
//                                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                            String s1 = HexUtil.byteToString(data);
                                                                            String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                                                            String s3 = "时间：" + dateString + ",数值：" + HexUtil.byteToString(data);
                                                                            System.out.println("回调的值：" + s3);
                                                                            System.out.println("s2的值：" + s2);
                                                                            System.out.println(" ");
                                                                            String s = testResult.getText().toString();
                                                                            testResult.setText(s + "\n" + s3);

//                                                                                System.out.println("原始s1" + s1);
//                                                                                System.out.println("原始s1长度" + s1.split(",").length);

//                                                                            }
//                                                                        },0);


                                                                });
                                                            }
                                                        });

                                            }
                                        }, 100);


//


                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {
                                        System.out.println("notify指令失败：" + exception);
                                    }
                                });


//                    }
//                }, 1000);
            }
        });

        // 测试使用
        testBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                testResult2.setText("");
                final String hex = orderInput2.getText().toString();

                System.out.println("--->getUuid:" + characteristic.getUuid().toString());
                System.out.println("--->getInstanceId:" + characteristic.getInstanceId());
                System.out.println("--->getProperties:" + characteristic.getProperties());
                System.out.println("--->getWriteType:" + characteristic.getWriteType());
                System.out.println("--->getPermissions:" + characteristic.getPermissions());

                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        HexUtil.hexStringToBytes(hex),
                        new BleWriteCallback() {

                            @Override
                            public void onWriteSuccess(int current, int total, byte[] justWrite) {

                                System.out.println("写指令发送成功:" + hex);

//                                for(int i = 0; i < 10; i ++) {
//                                    new Handler().postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {

//                                            BleManager.getInstance().read(
//                                                    bleDevice,
//                                                    characteristicRead.getService().getUuid().toString(),
//                                                    characteristicRead.getUuid().toString(),
//                                                    new BleReadCallback() {
//
//
//                                                        @Override
//                                                        public void onReadSuccess(byte[] data) {
//                                                            System.out.println("开始读取read信息...");
//                                                            Date date = new Date();
//                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//                                                            String dateString = formatter.format(date);
//                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
//                                                            System.out.println("s1的值是：" + s1);
//                                                            if(s1 != null) {
//                                                                String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
//                                                                System.out.println("s2的值：" + s2);
//                                                                String s = testResult2.getText().toString();
//                                                                testResult2.setText(s + "\n" + s2);
//                                                            }
//                                                        }
//
//                                                        @Override
//                                                        public void onReadFailure(BleException exception) {
//
//                                                            System.out.println("读取read信息失败...");
//                                                        }
//                                                    });
//                                        }
//                                    }, 5025);


//                                }
                            }

                            @Override
                            public void onWriteFailure(BleException exception) {
                                System.out.println("read指令失败：" + exception);
                            }
                        });


//                    }
//                }, 1000);
            }
        });

        // 测试使用
//        testBtn3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                testResult3.setText("");
//
//                final String hex = orderInput3.getText().toString();
//                BleManager.getInstance().write(bleDevice, characteristic3.getService().getUuid().toString(),
//                        characteristic3.getUuid().toString(),
//                        hex.getBytes(),
//                        new BleWriteCallback() {
//
//                            @Override
//                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//
//                                System.out.println("写指令发送成功:" + hex);
//
////                                for(int i = 0; i < 10; i ++) {
////                                    new Handler().postDelayed(new Runnable() {
////                                        @Override
////                                        public void run() {
////                                testResult3.setText(justWrite.toString());
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        BleManager.getInstance().read(
//                                                bleDevice,
//                                                characteristic3.getService().getUuid().toString(),
//                                                characteristic3.getUuid().toString(),
//                                                new BleReadCallback() {
//
//
//                                                    @Override
//                                                    public void onReadSuccess(byte[] data) {
//                                                        System.out.println("开始读取read信息...");
//                                                        Date date = new Date();
//                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//                                                        String dateString = formatter.format(date);
//                                                        String s1 = HexUtil.formatHexString(characteristic3.getValue());
//                                                        System.out.println("s1的值是：" + s1);
//                                                        if(s1 != null) {
//                                                            String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
//                                                            System.out.println("s2的值：" + s2);
//                                                            String s = testResult3.getText().toString();
//                                                            testResult3.setText(s + "\n" + s2);
//                                                        }
//                                                    }
//
//                                                    @Override
//                                                    public void onReadFailure(BleException exception) {
//
//                                                        System.out.println("读取read信息失败...");
//                                                    }
//                                                });
//                                    }
//                                },200);
//
//
//
//                            }
//
//                            @Override
//                            public void onWriteFailure(BleException exception) {
//                                System.out.println("write指令失败：" + exception);
//                            }
//                        });
//
//
////                    }
////                }, 1000);
//            }
//        });

        testBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testResult3.setText("");

                System.out.println("--->getUuid:" + characteristicRead.getUuid().toString());
                System.out.println("--->getInstanceId:" + characteristicRead.getInstanceId());
                System.out.println("--->getProperties:" + characteristicRead.getProperties());
                System.out.println("--->getWriteType:" + characteristicRead.getWriteType());
                System.out.println("--->getPermissions:" + characteristicRead.getPermissions());

                System.out.println("service的特征值：" + characteristicRead.getService().getUuid().toString());
                System.out.println("characteristicRead：" + characteristicRead.getUuid().toString());
                BleManager.getInstance().notify(
                        bleDevice,
                        characteristicRead.getService().getUuid().toString(),
                        characteristicRead.getUuid().toString(),
                        new BleNotifyCallback() {


                            @Override
                            public void onNotifySuccess() {
                                System.out.println("notify发送成功");
                                Date date = new Date();
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                String dateString = formatter.format(date);
                                System.out.println("开始：" + dateString);
                            }

                            @Override
                            public void onNotifyFailure(BleException exception) {
                                System.out.println("notify发送失败：" + exception);
                            }

                            @Override
                            public void onCharacteristicChanged(byte[] data) {

                                runOnUiThread(() -> {
//                                                                    System.out.println("开始读取notify信息...");
//                                                                        new Handler().postDelayed(new Runnable() {
//                                                                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//                                                                            @Override
//                                                                            public void run() {
                                    Date date = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
                                    String dateString = formatter.format(date);

//                                                                                System.out.println("原始数据：" + characteristicRead.getValue());
//                                                                                for(byte b : characteristicRead.getValue()) {
//                                                                                    System.out.println(b);
//                                                                                }
                                    String s1 = HexUtil.byteToString(data);
                                    String s2 = "时间:" + dateString + ",数值:" + s1 + "长度：" + s1.split(",").length;
                                    String s3 = "时间：" + dateString + ",数值：" + HexUtil.byteToString(data);
                                    System.out.println("S3的值：" + s3);
                                    System.out.println("s2的值：" + s2);
                                    System.out.println(" ");
                                    String s = testResult3.getText().toString();
                                    testResult3.setText(s + "\n" + s3);


//                                                                                System.out.println("原始s1" + s1);
//                                                                                System.out.println("原始s1长度" + s1.split(",").length);

//                                                                            }
//                                                                        },0);


                                });
                            }
                        });
            }
        });

//        testBtn3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                testResult3.setText("");
//                BleManager.getInstance().read(
//                        bleDevice,
//                        characteristicRead.getService().getUuid().toString(),
//                        characteristicRead.getUuid().toString(),
//                        new BleReadCallback() {
//
//
//                            @Override
//                            public void onReadSuccess(byte[] data) {
//
//                                testResult3.setText("read成功");
//                                String s1 = HexUtil.byteToString(data);
//                                System.out.println(" ");
//                                String s = testResult3.getText().toString();
//                                testResult3.setText(s + "\n" + s1);
//                            }
//
//                            @Override
//                            public void onReadFailure(BleException exception) {
//                                testResult3.setText("read失败");
//                            }
//
//                        });
//            }
//        });
    }
}
