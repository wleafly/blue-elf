package com.clj.fastble.callback;


import com.clj.fastble.data.BleDevice;

import java.util.List;

/**
 * Ble扫描回调
 */
public abstract class BleScanCallback implements BleScanPresenterImp {

    //扫描完成
    public abstract void onScanFinished(List<BleDevice> scanResultList);

    public void onLeScan(BleDevice bleDevice) {
    }
}
