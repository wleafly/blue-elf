package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * Ble读回调
 */
public abstract class BleReadCallback extends BleBaseCallback {

    public abstract void onReadSuccess(byte[] data);

    public abstract void onReadFailure(BleException exception);

}
