package com.clj.blesample.activity;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

public class TroubleShootingActivity extends AppCompatActivity {
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

    private View timeLayout;

    private Toolbar toolbar;
    private ImageView circle;
    private Button btn;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trouble_shooting);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

            StrictMode.setVmPolicy(builder.build());

        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.trouble_shooting));
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
        System.out.println("故障排查页面characteristic的数据--->getUuid:" + characteristic.getUuid().toString());
        System.out.println("故障排查页面characteristicRead的数据--->getUuid:" + characteristicRead.getUuid().toString());

        circle = (ImageView) findViewById(R.id.circle);
        btn = (Button) findViewById(R.id.shooting_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(globalDeviceIsConnect == false) {
                    new AlertDialog.Builder(timeLayout.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
                    return;
                }


            }
        });
    }

}
