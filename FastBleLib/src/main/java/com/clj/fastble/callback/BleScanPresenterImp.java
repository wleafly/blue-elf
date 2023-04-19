package com.clj.fastble.callback;

import com.clj.fastble.data.BleDevice;

/**
 * Ble扫描主讲人
 */
public interface BleScanPresenterImp {

    //扫描开始
    void onScanStarted(boolean success);

    //
    void onScanning(BleDevice bleDevice);

}
