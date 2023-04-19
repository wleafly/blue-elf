package com.clj.fastble.callback;


import com.clj.fastble.exception.BleException;

/**
 * Ble Mtu改变回调
 */
public abstract class BleMtuChangedCallback extends BleBaseCallback {

    public abstract void onSetMTUFailure(BleException exception);//设置MTU失败

    public abstract void onMtuChanged(int mtu);

}
