package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class HistoryDataDeleteActivity extends AppCompatActivity {

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

    private TextView historyDataDeleteDescribe;
    private ImageView deviceImage;
    private Button btn;
    private View layoutDataDelete;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_history_data_delete);

        btn = (Button)findViewById(R.id.btn);
        deviceImage = findViewById(R.id.device_image);
        layoutDataDelete = findViewById(R.id.layout_data_delete);
        historyDataDeleteDescribe = findViewById(R.id.history_data_delete_describe);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.history_data_delete_activity));
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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(layoutDataDelete.getContext()).setMessage(getString(R.string.history_data_delete_confirm)).setNegativeButton(getString(R.string.ensure),
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
                                                        new AlertDialog.Builder(layoutDataDelete.getContext()).setMessage(getString(R.string.history_data_delete_success)).setPositiveButton(getString(R.string.ensure),null).show();

                                                    }
                                                });
                                            }

                                            @Override
                                            public void onWriteFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                addText(input, exception.toString());
                                                        new AlertDialog.Builder(layoutDataDelete.getContext()).setMessage(getString(R.string.history_data_delete_fail)).setPositiveButton(getString(R.string.ensure),null).show();
                                                    }
                                                });
                                            }
                                        });


                            }
                        }).setPositiveButton(getString(R.string.cancel),null).show();



            }
        });

    }
}

