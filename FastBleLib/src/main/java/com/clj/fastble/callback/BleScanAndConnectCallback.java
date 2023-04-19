package com.clj.fastble.callback;


import com.clj.fastble.data.BleDevice;

/**
 * Ble扫描并连接回调
 */
public abstract class BleScanAndConnectCallback extends BleGattCallback implements BleScanPresenterImp {

    public abstract void onScanFinished(BleDevice scanResult);

    public void onLeScan(BleDevice bleDevice) {
    }

}
