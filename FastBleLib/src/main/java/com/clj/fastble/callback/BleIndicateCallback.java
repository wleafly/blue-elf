package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * Ble显示回调
 */
public abstract class BleIndicateCallback extends BleBaseCallback{

    public abstract void onIndicateSuccess();

    public abstract void onIndicateFailure(BleException exception);

    public abstract void onCharacteristicChanged(byte[] data);
}
