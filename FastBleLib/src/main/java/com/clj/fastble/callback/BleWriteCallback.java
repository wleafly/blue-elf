package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * 写回调
 */
public abstract class BleWriteCallback extends BleBaseCallback{

    public abstract void onWriteSuccess(int current, int total, byte[] justWrite);

    public abstract void onWriteFailure(BleException exception);

}
