package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * ：Ble通知回调
 */
public abstract class BleNotifyCallback extends BleBaseCallback {

    public abstract void onNotifySuccess();

    public abstract void onNotifyFailure(BleException exception);

    public abstract void onCharacteristicChanged(byte[] data);

}
