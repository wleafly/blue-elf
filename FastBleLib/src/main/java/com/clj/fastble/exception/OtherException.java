package com.clj.fastble.exception;

/**
 * 其他异常
 */
public class OtherException extends BleException {

    public OtherException(String description) {
        super(ERROR_CODE_OTHER, description);
    }

}
