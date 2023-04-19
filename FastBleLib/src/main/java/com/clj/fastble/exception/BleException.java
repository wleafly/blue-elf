package com.clj.fastble.exception;

import java.io.Serializable;

/**
 * Ble的例外
 */
public abstract class BleException implements Serializable {

    private static final long serialVersionUID = 8004414918500865564L;//序列化的版本号

    public static final int ERROR_CODE_TIMEOUT = 100;//错误代码超时
    public static final int ERROR_CODE_GATT = 101;//错误代码关贸总协定
    public static final int ERROR_CODE_OTHER = 102;//其他错误代码

    private int code;
    private String description;//描述

    public BleException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public BleException setCode(int code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public BleException setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public String toString() {
        return "BleException { " +
               "code=" + code +
               ", description说明='" + description + '\'' +
               '}';
    }
}
