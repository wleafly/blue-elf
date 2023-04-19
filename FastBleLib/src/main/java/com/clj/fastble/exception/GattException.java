package com.clj.fastble.exception;


public class GattException extends BleException {

    private int gattStatus;

    public GattException(int gattStatus) {
        super(ERROR_CODE_GATT, "Gatt Exception Occurred! 关贸总协定的异常发生");
        this.gattStatus = gattStatus;
    }

    //得到关贸总协定状态
    public int getGattStatus() {
        return gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    @Override
    public String toString() {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                "} " + super.toString();
    }
}
