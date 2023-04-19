
package com.clj.fastble.callback;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.os.Build;

import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//：Ble关贸总协定回调
public abstract class BleGattCallback extends BluetoothGattCallback {

    //开始连接
    public abstract void onStartConnect();

    //连接失败
    public abstract void onConnectFail(BleDevice bleDevice, BleException exception);

    //连接成功
    public abstract void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status);

    //连接断开
    public abstract void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status);

}