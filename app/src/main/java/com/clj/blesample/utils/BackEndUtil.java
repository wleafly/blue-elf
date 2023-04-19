package com.clj.blesample.utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.blesample.R;

public class BackEndUtil extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static synchronized void sendConnectionCode(BleDevice bleDevice, BluetoothGatt gatt, String value) {
        BluetoothGattService service = null;
        if (gatt != null) {
            for (BluetoothGattService s : gatt.getServices()) {
                if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                    service = s;
                }
            }
        }
        BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
        int ii = 0;
        if(service == null) {
            System.out.println("发送bb的函数：service为空");
            return;
        }
        System.out.println("service相关的值是：" + service.getUuid());
        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
            cs[ii++] = c;
        }
        BluetoothGattCharacteristic characteristic = cs[0];

        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                characteristic.getUuid().toString(),
                HexUtil.hexStringToBytes(value),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        System.out.println("连接芯片code发送成功:" + value + "---------" + HexUtil.byteToString(justWrite));
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        System.out.println("连接芯片code发送失败：" + exception);
                    }
                });

    }
}
