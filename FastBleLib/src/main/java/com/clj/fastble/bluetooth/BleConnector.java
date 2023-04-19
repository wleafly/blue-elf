
package com.clj.fastble.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleMsg;
import com.clj.fastble.data.BleWriteState;
import com.clj.fastble.exception.GattException;
import com.clj.fastble.exception.OtherException;
import com.clj.fastble.exception.TimeoutException;
import com.clj.fastble.utils.HexUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)//版本代码
/**
 * Ble连接器
 */
public class BleConnector {

    // private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";//UUID客户端特征配置描述符
    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";// 安信可
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    private BleBluetooth mBleBluetooth;
    private Handler mHandler;

    BleConnector(BleBluetooth bleBluetooth) {
        this.mBleBluetooth = bleBluetooth;
        this.mBluetoothGatt = bleBluetooth.getBluetoothGatt();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case BleMsg.MSG_CHA_NOTIFY_START: {//通知启动
                        BleNotifyCallback notifyCallback = (BleNotifyCallback) msg.obj;
                        System.out.println("这里是数据notify启动");
                        if (notifyCallback != null)
                            notifyCallback.onNotifyFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_CHA_NOTIFY_RESULT: {//通知结果
                        notifyMsgInit();

                        BleNotifyCallback notifyCallback = (BleNotifyCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_NOTIFY_BUNDLE_STATUS);//通知状态
                        if (notifyCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {//关贸总协定的成功
                                notifyCallback.onNotifySuccess();
                            } else {
                                notifyCallback.onNotifyFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE: {//MSG CHA数据变更通知
                        // 获取回调
                        BleNotifyCallback notifyCallback = (BleNotifyCallback) msg.obj;
                        Bundle bundle = msg.getData();

                        byte[] value = bundle.getByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE);//通知值
//                        System.out.println("这里是数据notify变更");
//                        Date date = new Date();
//                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//                        String dateString = formatter.format(date);

//                        System.out.println("时间：" + dateString + "，BleConnector-->value值：" + HexUtil.byteToString(value));

                        if (notifyCallback != null) {
                            notifyCallback.onCharacteristicChanged(value);
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_INDICATE_START: {//表示开始
                        BleIndicateCallback indicateCallback = (BleIndicateCallback) msg.obj;
                        if (indicateCallback != null)
                            indicateCallback.onIndicateFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_CHA_INDICATE_RESULT: {//结果表明效果明显
                        indicateMsgInit();
                        BleIndicateCallback indicateCallback = (BleIndicateCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_INDICATE_BUNDLE_STATUS);//后台状态标记
                        if (indicateCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                indicateCallback.onIndicateSuccess();
                            } else {
                                indicateCallback.onIndicateFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_INDICATE_DATA_CHANGE: {//显示数据的修改
                        BleIndicateCallback indicateCallback = (BleIndicateCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        byte[] value = bundle.getByteArray(BleMsg.KEY_INDICATE_BUNDLE_VALUE);//指示值
                        if (indicateCallback != null) {
                            indicateCallback.onCharacteristicChanged(value);
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_WRITE_START: {//写作指南
                        BleWriteCallback writeCallback = (BleWriteCallback) msg.obj;
                        System.out.println("这里是数据write写作");
                        if (writeCallback != null) {
                            writeCallback.onWriteFailure(new TimeoutException());
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_WRITE_RESULT: {//写的结果
                        writeMsgInit();

                        BleWriteCallback writeCallback = (BleWriteCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_WRITE_BUNDLE_STATUS);//写包状态
                        byte[] value = bundle.getByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE);//包的价值
                        if (writeCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                writeCallback.onWriteSuccess(BleWriteState.DATA_WRITE_SINGLE, BleWriteState.DATA_WRITE_SINGLE, value);//蓝牙写状态
                            } else {
                                writeCallback.onWriteFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_READ_START: {//读取
                        BleReadCallback readCallback = (BleReadCallback) msg.obj;
                        if (readCallback != null)
                            readCallback.onReadFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_CHA_READ_RESULT: {//读取结果
                        readMsgInit();

                        BleReadCallback readCallback = (BleReadCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_READ_BUNDLE_STATUS);//键读BUNDLE状态
                        byte[] value = bundle.getByteArray(BleMsg.KEY_READ_BUNDLE_VALUE);//键读BUNDLE值
                        if (readCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                readCallback.onReadSuccess(value);
                            } else {
                                readCallback.onReadFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_READ_RSSI_START: {//RSSI开始
                        BleRssiCallback rssiCallback = (BleRssiCallback) msg.obj;
                        if (rssiCallback != null)
                            rssiCallback.onRssiFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_READ_RSSI_RESULT: {//RSSI的结果
                        rssiMsgInit();

                        BleRssiCallback rssiCallback = (BleRssiCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS);
                        int value = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE);
                        if (rssiCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                rssiCallback.onRssiSuccess(value);
                            } else {
                                rssiCallback.onRssiFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_SET_MTU_START: {
                        BleMtuChangedCallback mtuChangedCallback = (BleMtuChangedCallback) msg.obj;
                        if (mtuChangedCallback != null)
                            mtuChangedCallback.onSetMTUFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_SET_MTU_RESULT: {
                        mtuChangedMsgInit();

                        BleMtuChangedCallback mtuChangedCallback = (BleMtuChangedCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS);
                        int value = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE);
                        if (mtuChangedCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                mtuChangedCallback.onMtuChanged(value);
                            } else {
                                mtuChangedCallback.onSetMTUFailure(new GattException(status));
                            }
                        }
                        break;
                    }
                }
            }
        };

    }

    private BleConnector withUUID(UUID serviceUUID, UUID characteristicUUID) {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID);
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        }
        return this;
    }

    public BleConnector withUUIDString(String serviceUUID, String characteristicUUID) {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID));
    }

    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }


    /*------------------------------- main operation ----------------------------------- */


    /**
     * notify 通告
     */
    public void enableCharacteristicNotify(BleNotifyCallback bleNotifyCallback, String uuid_notify,
                                           boolean userCharacteristicDescriptor) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            handleCharacteristicNotifyCallback(bleNotifyCallback, uuid_notify);
            setCharacteristicNotification(mBluetoothGatt, mCharacteristic, userCharacteristicDescriptor, true, bleNotifyCallback);
        } else {
            if (bleNotifyCallback != null)
                bleNotifyCallback.onNotifyFailure(new OtherException("this characteristic not support notify!"));
        }
    }

    /**
     * stop notify  停止通知
     */
    public boolean disableCharacteristicNotify(boolean useCharacteristicDescriptor) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return setCharacteristicNotification(mBluetoothGatt, mCharacteristic,
                    useCharacteristicDescriptor, false, null);
        } else {
            return false;
        }
    }

    /**
     * notify setting 通知设置
     */
    private boolean setCharacteristicNotification(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  boolean useCharacteristicDescriptor,
                                                  boolean enable,
                                                  BleNotifyCallback bleNotifyCallback) {
        if (gatt == null || characteristic == null) {
            notifyMsgInit();
            if (bleNotifyCallback != null)
                bleNotifyCallback.onNotifyFailure(new OtherException("gatt or characteristic equal null"));//gatt或特性等于null
            return false;
        }

        boolean success1 = gatt.setCharacteristicNotification(characteristic, enable);
        if (!success1) {
            notifyMsgInit();
            if (bleNotifyCallback != null)
                bleNotifyCallback.onNotifyFailure(new OtherException("gatt setCharacteristicNotification fail"));
            return false;
        }
//        System.out.println("useCharacteristicDescriptor:" + useCharacteristicDescriptor);
        BluetoothGattDescriptor descriptor;
        if (useCharacteristicDescriptor) {
            descriptor = characteristic.getDescriptor(characteristic.getUuid());
        } else {
            System.out.println("这里来过");
            System.out.println("characteristic:" + characteristic);
            System.out.println("characteristic:" + characteristic.getUuid().toString());

            System.out.println("id:" + formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
            descriptor = characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        }
//        System.out.println("descriptor:" + descriptor.toString());
        if (descriptor == null) {
            notifyMsgInit();
            if (bleNotifyCallback != null)
                bleNotifyCallback.onNotifyFailure(new OtherException("descriptor equals null"));
            return false;
        } else {
            descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean success2 = gatt.writeDescriptor(descriptor);
            if (!success2) {
                notifyMsgInit();
                if (bleNotifyCallback != null)
                    bleNotifyCallback.onNotifyFailure(new OtherException("gatt writeDescriptor fail"));
            }
            return success2;
        }
    }

    /**
     * indicate
     */
    public void enableCharacteristicIndicate(BleIndicateCallback bleIndicateCallback, String uuid_indicate,
                                             boolean useCharacteristicDescriptor) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            handleCharacteristicIndicateCallback(bleIndicateCallback, uuid_indicate);
            setCharacteristicIndication(mBluetoothGatt, mCharacteristic,
                    useCharacteristicDescriptor, true, bleIndicateCallback);
        } else {
            if (bleIndicateCallback != null)
                bleIndicateCallback.onIndicateFailure(new OtherException("this characteristic not support indicate!"));
        }
    }


    /**
     * stop indicate
     */
    public boolean disableCharacteristicIndicate(boolean userCharacteristicDescriptor) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            return setCharacteristicIndication(mBluetoothGatt, mCharacteristic,
                    userCharacteristicDescriptor, false, null);
        } else {
            return false;
        }
    }

    /**
     * indicate setting
     */
    private boolean setCharacteristicIndication(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic,
                                                boolean useCharacteristicDescriptor,
                                                boolean enable,
                                                BleIndicateCallback bleIndicateCallback) {
        if (gatt == null || characteristic == null) {
            indicateMsgInit();
            if (bleIndicateCallback != null)
                bleIndicateCallback.onIndicateFailure(new OtherException("gatt or characteristic equal null"));
            return false;
        }

        boolean success1 = gatt.setCharacteristicNotification(characteristic, enable);
        if (!success1) {
            indicateMsgInit();
            if (bleIndicateCallback != null)
                bleIndicateCallback.onIndicateFailure(new OtherException("gatt setCharacteristicNotification fail"));
            return false;
        }

        BluetoothGattDescriptor descriptor;
        if (useCharacteristicDescriptor) {
            descriptor = characteristic.getDescriptor(characteristic.getUuid());
        } else {
            descriptor = characteristic.getDescriptor(formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        }
        if (descriptor == null) {
            indicateMsgInit();
            if (bleIndicateCallback != null)
                bleIndicateCallback.onIndicateFailure(new OtherException("descriptor equals null"));
            return false;
        } else {
            descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE :
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            boolean success2 = gatt.writeDescriptor(descriptor);
            if (!success2) {
                indicateMsgInit();
                if (bleIndicateCallback != null)
                    bleIndicateCallback.onIndicateFailure(new OtherException("gatt writeDescriptor fail"));
            }
            return success2;
        }
    }

    /**
     * write
     */
    public void writeCharacteristic(byte[] data, BleWriteCallback bleWriteCallback, String uuid_write) {
        if (data == null || data.length <= 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("the data to be written is empty"));
            return;
        }

        if (mCharacteristic == null
                || (mCharacteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("this characteristic not support write!"));
            return;
        }

        if (mCharacteristic.setValue(data)) {
            System.out.println("bleWriteCallback的值是：" + bleWriteCallback);
            handleCharacteristicWriteCallback(bleWriteCallback, uuid_write);
            if (!mBluetoothGatt.writeCharacteristic(mCharacteristic)) {
                writeMsgInit();
                if (bleWriteCallback != null)
                    bleWriteCallback.onWriteFailure(new OtherException("gatt writeCharacteristic fail"));
            }
        } else {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("Updates the locally stored value of this characteristic fail"));
        }
    }

    /**
     * read
     */
    public void readCharacteristic(BleReadCallback bleReadCallback, String uuid_read) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

            handleCharacteristicReadCallback(bleReadCallback, uuid_read);
            if (!mBluetoothGatt.readCharacteristic(mCharacteristic)) {
                readMsgInit();
                if (bleReadCallback != null)
                    bleReadCallback.onReadFailure(new OtherException("gatt readCharacteristic fail"));
            }
        } else {
            if (bleReadCallback != null)
                bleReadCallback.onReadFailure(new OtherException("this characteristic not support read!"));
        }
    }

    /**
     * rssi
     */
    public void readRemoteRssi(BleRssiCallback bleRssiCallback) {
        handleRSSIReadCallback(bleRssiCallback);
        if (!mBluetoothGatt.readRemoteRssi()) {
            rssiMsgInit();
            if (bleRssiCallback != null)
                bleRssiCallback.onRssiFailure(new OtherException("gatt readRemoteRssi fail"));
        }
    }

    /**
     * set mtu
     */
    public void setMtu(int requiredMtu, BleMtuChangedCallback bleMtuChangedCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handleSetMtuCallback(bleMtuChangedCallback);
            if (!mBluetoothGatt.requestMtu(requiredMtu)) {
                mtuChangedMsgInit();
                if (bleMtuChangedCallback != null)
                    bleMtuChangedCallback.onSetMTUFailure(new OtherException("gatt requestMtu fail"));
            }
        } else {
            if (bleMtuChangedCallback != null)
                bleMtuChangedCallback.onSetMTUFailure(new OtherException("API level lower than 21"));
        }
    }

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
     *                           or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
     * @throws IllegalArgumentException If the parameters are outside of their
     *                                  specified range.
     */
    public boolean requestConnectionPriority(int connectionPriority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mBluetoothGatt.requestConnectionPriority(connectionPriority);
        }
        return false;
    }


    /**************************************** Handle call back ******************************************/

    /**
     * notify
     */
    private void handleCharacteristicNotifyCallback(BleNotifyCallback bleNotifyCallback,
                                                    String uuid_notify) {
        if (bleNotifyCallback != null) {

            notifyMsgInit();
            bleNotifyCallback.setKey(uuid_notify);
            bleNotifyCallback.setHandler(mHandler);
            mBleBluetooth.addNotifyCallback(uuid_notify, bleNotifyCallback);
            // 通过message方法
//            Message msg = new Message();
//            mHandler.sendMessage(mHandler.obtainMessage(BleMsg.MSG_CHA_NOTIFY_START, bleNotifyCallback));
//            Date date = new Date();
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//            String dateString = formatter.format(date);
//            System.out.println("BleConnector时间：" + dateString);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_NOTIFY_START, bleNotifyCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * indicate
     */
    private void handleCharacteristicIndicateCallback(BleIndicateCallback bleIndicateCallback,
                                                      String uuid_indicate) {
        if (bleIndicateCallback != null) {
            indicateMsgInit();
            bleIndicateCallback.setKey(uuid_indicate);
            bleIndicateCallback.setHandler(mHandler);
            mBleBluetooth.addIndicateCallback(uuid_indicate, bleIndicateCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_INDICATE_START, bleIndicateCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * write
     */
    private void handleCharacteristicWriteCallback(BleWriteCallback bleWriteCallback,
                                                   String uuid_write) {
        if (bleWriteCallback != null) {
            writeMsgInit();
            bleWriteCallback.setKey(uuid_write);
            bleWriteCallback.setHandler(mHandler);
            mBleBluetooth.addWriteCallback(uuid_write, bleWriteCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_WRITE_START, bleWriteCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * read
     */
    private void handleCharacteristicReadCallback(BleReadCallback bleReadCallback,
                                                  String uuid_read) {
        if (bleReadCallback != null) {
            readMsgInit();
            bleReadCallback.setKey(uuid_read);
            bleReadCallback.setHandler(mHandler);
            mBleBluetooth.addReadCallback(uuid_read, bleReadCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_READ_START, bleReadCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * rssi
     */
    private void handleRSSIReadCallback(BleRssiCallback bleRssiCallback) {
        if (bleRssiCallback != null) {
            rssiMsgInit();
            bleRssiCallback.setHandler(mHandler);
            mBleBluetooth.addRssiCallback(bleRssiCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_READ_RSSI_START, bleRssiCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * set mtu
     */
    private void handleSetMtuCallback(BleMtuChangedCallback bleMtuChangedCallback) {
        if (bleMtuChangedCallback != null) {
            mtuChangedMsgInit();
            bleMtuChangedCallback.setHandler(mHandler);
            mBleBluetooth.addMtuChangedCallback(bleMtuChangedCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_SET_MTU_START, bleMtuChangedCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    public void notifyMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_NOTIFY_START);
    }

    public void indicateMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_INDICATE_START);
    }

    public void writeMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_WRITE_START);
    }

    public void readMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_READ_START);
    }

    public void rssiMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_READ_RSSI_START);
    }

    public void mtuChangedMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_SET_MTU_START);
    }

}
