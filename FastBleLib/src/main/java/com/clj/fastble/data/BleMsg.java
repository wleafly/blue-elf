package com.clj.fastble.data;



public class BleMsg {


    // Scan
    public static final int MSG_SCAN_DEVICE = 0X00;//扫描设备

    // Connect
    public static final int MSG_CONNECT_FAIL = 0x01;//连接失败
    public static final int MSG_DISCONNECTED = 0x02;//断开连接
    public static final int MSG_RECONNECT = 0x03;//重新连接
    public static final int MSG_DISCOVER_SERVICES = 0x04;//扫描外设中的服务
    public static final int MSG_DISCOVER_FAIL = 0x05;//扫描失败
    public static final int MSG_DISCOVER_SUCCESS = 0x06;//扫描成功
    public static final int MSG_CONNECT_OVER_TIME = 0x07;//随着时间的推移连接

    // Notify通告
    public static final int MSG_CHA_NOTIFY_START = 0x11;//通知启动
    public static final int MSG_CHA_NOTIFY_RESULT = 0x12;//通知结果
    public static final int MSG_CHA_NOTIFY_DATA_CHANGE = 0x13;//MSG CHA数据变更通知
    public static final String KEY_NOTIFY_BUNDLE_STATUS = "notify_status";//通知状态
    public static final String KEY_NOTIFY_BUNDLE_VALUE = "notify_value";//通知值

    // Indicate显示
    public static final int MSG_CHA_INDICATE_START = 0x21;//表示开始
    public static final int MSG_CHA_INDICATE_RESULT = 0x22;//结果表明效果明显
    public static final int MSG_CHA_INDICATE_DATA_CHANGE = 0x23;//显示数据的修改
    public static final String KEY_INDICATE_BUNDLE_STATUS = "indicate_status";//后台状态标记
    public static final String KEY_INDICATE_BUNDLE_VALUE = "indicate_value";//指示值

    // Write
    public static final int MSG_CHA_WRITE_START = 0x31;//聪明写作指南
    public static final int MSG_CHA_WRITE_RESULT = 0x32;//写的结果
    public static final int MSG_SPLIT_WRITE_NEXT = 0x33;//拆分写入下一步
    public static final String KEY_WRITE_BUNDLE_STATUS = "write_status";//包状态
    public static final String KEY_WRITE_BUNDLE_VALUE = "write_value";//包的价值

    // Read
    public static final int MSG_CHA_READ_START = 0x41;//读取
    public static final int MSG_CHA_READ_RESULT = 0x42;//读取结果
    public static final String KEY_READ_BUNDLE_STATUS = "read_status";//键读BUNDLE状态
    public static final String KEY_READ_BUNDLE_VALUE = "read_value";

    // Rssi
    public static final int MSG_READ_RSSI_START = 0x51;//RSSI开始
    public static final int MSG_READ_RSSI_RESULT = 0x52;//RSSI的结果
    public static final String KEY_READ_RSSI_BUNDLE_STATUS = "rssi_status";
    public static final String KEY_READ_RSSI_BUNDLE_VALUE = "rssi_value";

    // Mtu
    public static final int MSG_SET_MTU_START = 0x61;//设置MTU START
    public static final int MSG_SET_MTU_RESULT = 0x62;//设置MTU结果
    public static final String KEY_SET_MTU_BUNDLE_STATUS = "mtu_status";//设置MTU绑定状态key
    public static final String KEY_SET_MTU_BUNDLE_VALUE = "mtu_value";//设置MTU绑定状态值



}
