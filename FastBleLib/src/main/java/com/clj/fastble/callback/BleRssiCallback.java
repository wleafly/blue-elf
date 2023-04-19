package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * Ble Rssi回调
 */
public abstract class BleRssiCallback extends BleBaseCallback{

    public abstract void onRssiFailure(BleException exception);

    public abstract void onRssiSuccess(int rssi);

}