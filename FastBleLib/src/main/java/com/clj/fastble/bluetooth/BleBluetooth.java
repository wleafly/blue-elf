package com.clj.fastble.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleConnectStateParameter;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleMsg;
import com.clj.fastble.exception.ConnectException;
import com.clj.fastble.exception.OtherException;
import com.clj.fastble.exception.TimeoutException;
import com.clj.fastble.utils.BleLog;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleBluetooth {

    private BleGattCallback bleGattCallback;
    private BleRssiCallback bleRssiCallback;
    private BleMtuChangedCallback bleMtuChangedCallback;
    private HashMap<String, BleNotifyCallback> bleNotifyCallbackHashMap = new HashMap<>();//ble通知回调HashMap
    private HashMap<String, BleIndicateCallback> bleIndicateCallbackHashMap = new HashMap<>();//ble回调HashMap
    private HashMap<String, BleWriteCallback> bleWriteCallbackHashMap = new HashMap<>();//ble写回调HashMap
    private HashMap<String, BleReadCallback> bleReadCallbackHashMap = new HashMap<>();//ble读取回调HashMap

    private LastState lastState; //最后的状态
    private boolean isActiveDisconnect = false;//是主动断开连接
    private BleDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private MainHandler mainHandler = new MainHandler(Looper.getMainLooper());
    private int connectRetryCount = 0;

    // BleBluetooth类构造方法
    // 传入一个蓝牙设备对象
    // 在多连接的情况下，有多少外围设备，设备池中就维护着多少的BleBluetooth对象
    public BleBluetooth(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public BleConnector newBleConnector() {
        return new BleConnector(this);
    }

    //Ble关贸总协定回调
    public synchronized void addConnectGattCallback(BleGattCallback callback) {
        bleGattCallback = callback;
    }

    public synchronized void removeConnectGattCallback() {
        bleGattCallback = null;
    }

    //Ble通知回调
    public synchronized void addNotifyCallback(String uuid, BleNotifyCallback bleNotifyCallback) {
        bleNotifyCallbackHashMap.put(uuid, bleNotifyCallback);
    }

    public synchronized void addIndicateCallback(String uuid, BleIndicateCallback bleIndicateCallback) {
        bleIndicateCallbackHashMap.put(uuid, bleIndicateCallback);
    }

    public synchronized void addWriteCallback(String uuid, BleWriteCallback bleWriteCallback) {
        bleWriteCallbackHashMap.put(uuid, bleWriteCallback);
    }

    public synchronized void addReadCallback(String uuid, BleReadCallback bleReadCallback) {
        bleReadCallbackHashMap.put(uuid, bleReadCallback);
    }

    public synchronized void removeNotifyCallback(String uuid) {
        if (bleNotifyCallbackHashMap.containsKey(uuid))
            bleNotifyCallbackHashMap.remove(uuid);
    }

    public synchronized void removeIndicateCallback(String uuid) {
        if (bleIndicateCallbackHashMap.containsKey(uuid))
            bleIndicateCallbackHashMap.remove(uuid);
    }

    //删除写回调
    public synchronized void removeWriteCallback(String uuid) {
        if (bleWriteCallbackHashMap.containsKey(uuid))
            bleWriteCallbackHashMap.remove(uuid);
    }

    //删除读回调
    public synchronized void removeReadCallback(String uuid) {
        if (bleReadCallbackHashMap.containsKey(uuid))
            bleReadCallbackHashMap.remove(uuid);
    }

    //清晰的字符回调
    public synchronized void clearCharacterCallback() {
        if (bleNotifyCallbackHashMap != null)
            bleNotifyCallbackHashMap.clear();
        if (bleIndicateCallbackHashMap != null)
            bleIndicateCallbackHashMap.clear();
        if (bleWriteCallbackHashMap != null)
            bleWriteCallbackHashMap.clear();
        if (bleReadCallbackHashMap != null)
            bleReadCallbackHashMap.clear();
    }

    //添加Rssi回调
    public synchronized void addRssiCallback(BleRssiCallback callback) {
        bleRssiCallback = callback;
    }

    public synchronized void removeRssiCallback() {
        bleRssiCallback = null;
    }

    //添加Mtu更改回调
    public synchronized void addMtuChangedCallback(BleMtuChangedCallback callback) {
        bleMtuChangedCallback = callback;//回调
    }

    //删除Mtu更改回调
    public synchronized void removeMtuChangedCallback() {
        bleMtuChangedCallback = null;
    }


    public String getDeviceKey() {
        return bleDevice.getKey();
    }

    public BleDevice getDevice() {
        return bleDevice;
    }

    //有蓝牙关贸总协定
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public synchronized BluetoothGatt connect(BleDevice bleDevice,
                                              boolean autoConnect,
                                              BleGattCallback callback) {
        return connect(bleDevice, autoConnect, callback, 0);
    }

    public synchronized BluetoothGatt connect(BleDevice bleDevice,
                                              boolean autoConnect,
                                              BleGattCallback callback,
                                              int connectRetryCount) {
        BleLog.i("connect device连接设备: " + bleDevice.getName()
                + "\nmac: " + bleDevice.getMac()
                + "\nautoConnect: " + autoConnect
                + "\ncurrentThread: " + Thread.currentThread().getId()
                + "\nconnectCount:" + (connectRetryCount + 1));
        if (connectRetryCount == 0) {
            this.connectRetryCount = 0;
        }

        addConnectGattCallback(callback);

        lastState = LastState.CONNECT_CONNECTING;//连接连接

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = bleDevice.getDevice().connectGatt(BleManager.getInstance().getContext(),
                    autoConnect, coreGattCallback, TRANSPORT_LE);
        } else {
            bluetoothGatt = bleDevice.getDevice().connectGatt(BleManager.getInstance().getContext(),
                    autoConnect, coreGattCallback);
        }
        if (bluetoothGatt != null) {
            if (bleGattCallback != null) {
                bleGattCallback.onStartConnect();
            }
            Message message = mainHandler.obtainMessage();
            message.what = BleMsg.MSG_CONNECT_OVER_TIME;//随着时间的推移连接
            mainHandler.sendMessageDelayed(message, BleManager.getInstance().getConnectOverTime());

        } else {
            disconnectGatt();
            refreshDeviceCache();
            closeBluetoothGatt();
            lastState = LastState.CONNECT_FAILURE;//连接失败
            BleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleBluetooth.this);
            if (bleGattCallback != null)
                bleGattCallback.onConnectFail(bleDevice, new OtherException("GATT connect exception occurred!GATT连接异常"));

        }
        return bluetoothGatt;
    }

    //断开
    public synchronized void disconnect() {
        isActiveDisconnect = true;
        disconnectGatt();
    }

    public synchronized void destroy() {
        lastState = LastState.CONNECT_IDLE;//连接空闲
        disconnectGatt();
        refreshDeviceCache();
        closeBluetoothGatt();
        removeConnectGattCallback();
        removeRssiCallback();
        removeMtuChangedCallback();
        clearCharacterCallback();
        mainHandler.removeCallbacksAndMessages(null);
    }

    private synchronized void disconnectGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    private synchronized void refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");//刷新
            if (refresh != null && bluetoothGatt != null) {
                boolean success = (Boolean) refresh.invoke(bluetoothGatt);
                BleLog.i("refreshDeviceCache, is success 刷新缓存设备成功:  " + success);
            }
        } catch (Exception e) {
            BleLog.i("exception occur while refreshing device 刷新设备时出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void closeBluetoothGatt() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    private final class MainHandler extends Handler {

        MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BleMsg.MSG_CONNECT_FAIL: {//连接失败
                    disconnectGatt();
                    refreshDeviceCache();
                    closeBluetoothGatt();

                    if (connectRetryCount < BleManager.getInstance().getReConnectCount()) {
                        BleLog.e("Connect fail, try reconnect 连接失败，请重新连接 " + BleManager.getInstance().getReConnectInterval() + " 毫秒后millisecond later");
                        ++connectRetryCount;

                        Message message = mainHandler.obtainMessage();
                        message.what = BleMsg.MSG_RECONNECT;//重新连接
                        mainHandler.sendMessageDelayed(message, BleManager.getInstance().getReConnectInterval());
                    } else {
                        lastState = LastState.CONNECT_FAILURE;//连接失败
                        BleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleBluetooth.this);

                        BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                        int status = para.getStatus();
                        if (bleGattCallback != null)
                            bleGattCallback.onConnectFail(bleDevice, new ConnectException(bluetoothGatt, status));
                    }
                }
                break;

                case BleMsg.MSG_DISCONNECTED: {//断开连接
                    lastState = LastState.CONNECT_DISCONNECT;//快速拆接法
                    BleManager.getInstance().getMultipleBluetoothController().removeBleBluetooth(BleBluetooth.this);

                    disconnect();
                    refreshDeviceCache();
                    closeBluetoothGatt();
                    removeRssiCallback();
                    removeMtuChangedCallback();
                    clearCharacterCallback();
                    mainHandler.removeCallbacksAndMessages(null);

                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                    boolean isActive = para.isActive();
                    int status = para.getStatus();
                    if (bleGattCallback != null)
                        bleGattCallback.onDisConnected(isActive, bleDevice, bluetoothGatt, status);
                }
                break;

                case BleMsg.MSG_RECONNECT: {//重新连接
                    connect(bleDevice, false, bleGattCallback, connectRetryCount);
                }
                break;

                case BleMsg.MSG_CONNECT_OVER_TIME: {//随着时间的推移连接
                    disconnectGatt();
                    refreshDeviceCache();
                    closeBluetoothGatt();

                    lastState = LastState.CONNECT_FAILURE;//连接失败
                    BleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleBluetooth.this);

                    if (bleGattCallback != null)
                        bleGattCallback.onConnectFail(bleDevice, new TimeoutException());
                }
                break;

                case BleMsg.MSG_DISCOVER_SERVICES: {//扫描外设中的服务
                    if (bluetoothGatt != null) {
                        boolean discoverServiceResult = bluetoothGatt.discoverServices();
                        if (!discoverServiceResult) {
                            Message message = mainHandler.obtainMessage();
                            message.what = BleMsg.MSG_DISCOVER_FAIL;//扫描失败
                            mainHandler.sendMessage(message);
                        }
                    } else {
                        Message message = mainHandler.obtainMessage();
                        message.what = BleMsg.MSG_DISCOVER_FAIL;//扫描失败
                        mainHandler.sendMessage(message);
                    }
                }
                break;

                case BleMsg.MSG_DISCOVER_FAIL: {//扫描失败
                    disconnectGatt();
                    refreshDeviceCache();
                    closeBluetoothGatt();

                    lastState = LastState.CONNECT_FAILURE;//连接失败
                    BleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleBluetooth.this);

                    if (bleGattCallback != null)
                        bleGattCallback.onConnectFail(bleDevice,
                                new OtherException("GATT discover services exception occurred! GATT发现服务异常"));
                }
                break;

                case BleMsg.MSG_DISCOVER_SUCCESS: {//扫描成功
                    lastState = LastState.CONNECT_CONNECTED;//微控开关连接线接反
                    isActiveDisconnect = false;
                    BleManager.getInstance().getMultipleBluetoothController().removeConnectingBle(BleBluetooth.this);
                    BleManager.getInstance().getMultipleBluetoothController().addBleBluetooth(BleBluetooth.this);

                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                    int status = para.getStatus();
                    if (bleGattCallback != null)
                        bleGattCallback.onConnectSuccess(bleDevice, bluetoothGatt, status);
                }
                break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }


    /**
     * BlueBleGattback这个类非常重要，所有的GATT操作的回调都是在这里
     * 此处的coreGattCallback应该就扮演着这个角色，它是BluetoothGattCallback的实现类对象，对操作回调结果做了封装和分发
     */
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BleLog.i("BluetoothGattCallback：onConnectionStateChange "
                    + '\n' + "status: " + status
                    + '\n' + "newState: " + newState
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            bluetoothGatt = gatt;

            mainHandler.removeMessages(BleMsg.MSG_CONNECT_OVER_TIME);//随着时间的推移连接

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Message message = mainHandler.obtainMessage();
                message.what = BleMsg.MSG_DISCOVER_SERVICES;//扫描外设中的服务
                mainHandler.sendMessageDelayed(message, 500);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断连状态
                if (lastState == LastState.CONNECT_CONNECTING) {//连接连接
                    Message message = mainHandler.obtainMessage();
                    message.what = BleMsg.MSG_CONNECT_FAIL;//连接失败
                    message.obj = new BleConnectStateParameter(status);
                    mainHandler.sendMessage(message);

                } else if (lastState == LastState.CONNECT_CONNECTED) {//微控开关连接线接反
                    Message message = mainHandler.obtainMessage();
                    message.what = BleMsg.MSG_DISCONNECTED;//断开连接
                    BleConnectStateParameter para = new BleConnectStateParameter(status);
                    para.setActive(isActiveDisconnect);//是主动断开连接
                    message.obj = para;
                    mainHandler.sendMessage(message);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            BleLog.i("BluetoothGattCallback：onServicesDiscovered蓝牙关贸总协定回调:在发现的服务上 "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            bluetoothGatt = gatt;

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Message message = mainHandler.obtainMessage();
                message.what = BleMsg.MSG_DISCOVER_SUCCESS;////扫描成功
                message.obj = new BleConnectStateParameter(status);
                mainHandler.sendMessage(message);

            } else {
                Message message = mainHandler.obtainMessage();
                message.what = BleMsg.MSG_DISCOVER_FAIL;////扫描失败
                mainHandler.sendMessage(message);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Iterator iterator = bleNotifyCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleNotifyCallback) {
                    BleNotifyCallback bleNotifyCallback = (BleNotifyCallback) callback;
                    if (characteristic.getUuid().toString().equalsIgnoreCase(bleNotifyCallback.getKey())) {
                        Handler handler = bleNotifyCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE;//MSG CHA数据变更通知
                            message.obj = bleNotifyCallback;
                            Bundle bundle = new Bundle();
                            bundle.putByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE, characteristic.getValue());//通知值
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }

            iterator = bleIndicateCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleIndicateCallback) {
                    BleIndicateCallback bleIndicateCallback = (BleIndicateCallback) callback;
                    if (characteristic.getUuid().toString().equalsIgnoreCase(bleIndicateCallback.getKey())) {
                        Handler handler = bleIndicateCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_INDICATE_DATA_CHANGE;////显示数据的修改
                            message.obj = bleIndicateCallback;
                            Bundle bundle = new Bundle();
                            bundle.putByteArray(BleMsg.KEY_INDICATE_BUNDLE_VALUE, characteristic.getValue());//指示值
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            Iterator iterator = bleNotifyCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleNotifyCallback) {
                    BleNotifyCallback bleNotifyCallback = (BleNotifyCallback) callback;
                    if (descriptor.getCharacteristic().getUuid().toString().equalsIgnoreCase(bleNotifyCallback.getKey())) {
                        Handler handler = bleNotifyCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_NOTIFY_RESULT;//通知结果
                            message.obj = bleNotifyCallback;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS, status);//通知状态
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }

            iterator = bleIndicateCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleIndicateCallback) {
                    BleIndicateCallback bleIndicateCallback = (BleIndicateCallback) callback;
                    if (descriptor.getCharacteristic().getUuid().toString().equalsIgnoreCase(bleIndicateCallback.getKey())) {
                        Handler handler = bleIndicateCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_INDICATE_RESULT;//结果表明效果明显
                            message.obj = bleIndicateCallback;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS, status);//后台状态标记
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Iterator iterator = bleWriteCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleWriteCallback) {
                    BleWriteCallback bleWriteCallback = (BleWriteCallback) callback;
                    if (characteristic.getUuid().toString().equalsIgnoreCase(bleWriteCallback.getKey())) {
                        Handler handler = bleWriteCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_WRITE_RESULT;//写的结果
                            message.obj = bleWriteCallback;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BleMsg.KEY_WRITE_BUNDLE_STATUS, status);//包状态
                            bundle.putByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE, characteristic.getValue());//包的价值
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Iterator iterator = bleReadCallbackHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                Object callback = entry.getValue();
                if (callback instanceof BleReadCallback) {
                    BleReadCallback bleReadCallback = (BleReadCallback) callback;
                    if (characteristic.getUuid().toString().equalsIgnoreCase(bleReadCallback.getKey())) {
                        Handler handler = bleReadCallback.getHandler();
                        if (handler != null) {
                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_READ_RESULT;//读取结果
                            message.obj = bleReadCallback;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BleMsg.KEY_READ_BUNDLE_STATUS, status);//键读BUNDLE状态
                            bundle.putByteArray(BleMsg.KEY_READ_BUNDLE_VALUE, characteristic.getValue());//键读BUNDLE结果
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            if (bleRssiCallback != null) {
                Handler handler = bleRssiCallback.getHandler();
                if (handler != null) {
                    Message message = handler.obtainMessage();
                    message.what = BleMsg.MSG_READ_RSSI_RESULT;//MSG读取RSSI结果
                    message.obj = bleRssiCallback;
                    Bundle bundle = new Bundle();
                    bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS, status);//KEY读取RSSI BUNDLE的状态
                    bundle.putInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE, rssi);//KEY读取RSSI BUNDLE的值
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            if (bleMtuChangedCallback != null) {
                Handler handler = bleMtuChangedCallback.getHandler();
                if (handler != null) {
                    Message message = handler.obtainMessage();
                    message.what = BleMsg.MSG_SET_MTU_RESULT;//设置MTU结果
                    message.obj = bleMtuChangedCallback;
                    Bundle bundle = new Bundle();
                    bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS, status);//KEY设置MTU绑定状态
                    bundle.putInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE, mtu);//KEY设置MTU绑定值
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        }
    };

    enum LastState {
        CONNECT_IDLE,//连接空闲
        CONNECT_CONNECTING,//连接连接
        CONNECT_CONNECTED,//微控开关连接线接反
        CONNECT_FAILURE,//连接失败
        CONNECT_DISCONNECT//快速拆接法
    }

}
