package com.clj.blesample.application;

import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;

import com.clj.blesample.adapter.DeviceAdapter;
import com.clj.blesample.operation.CharacteristicListFragment;
import com.clj.blesample.operation.CharacteristicOperationFragment;
import com.clj.blesample.operation.ServiceListFragment;
import com.clj.blesample.tab.DeviceConnectFragment;
import com.clj.blesample.tab.DeviceSettingFragment;
import com.clj.blesample.tab.RealDataFragment;
import com.clj.fastble.BleManager;
import com.clj.fastble.bluetooth.BleBluetooth;
import com.clj.fastble.data.BleDevice;

public class MyApplication extends Application {

    private Context context;
    private Boolean basicDeviceIsConnect; // 设备是否连接
    private Boolean basicRealDataIsWorking; // 实时数据页面是否正在显示数据
    private Boolean basicRealDataIsNotify; // 实时数据页面是否已经Notify
    private Boolean basicDeviceChange; // 是否更换了连接设备
    private int basicDeviceBox; // 设备电池宽度
    private Boolean basicHistoryDataSign; // 历史数据标志

    /**
     * 基本的设备属性
     */
    private String basicDeviceName; // 设备名
    private String basicMACAddress; // 设备MAC地址
    private int basicDeviceAddress; // 设备地址
    private int basicDeviceType; // 传感器类型
    private String basicIntervalTime; // 间隔时间
    private String basicTestTime; // 测试时间
    private int basicType; // 0:多参数  1：多参数
    private int intervalTime; // 间隔时间
    private int testTime; // 测试时间
    private int cleaningTime; // 清洗间隔时间
    private int cleaningCycles; // 清洗圈数

    /**
     * 重要的对象属性
     */
    private BleManager basicBleManager;
    private BleDevice basicBleDevice;
    private DeviceAdapter basicDeviceAdapter;
    private BleBluetooth basicBleBluetooth;
    private BluetoothGattService basicBluetoothGattService;
    private ServiceListFragment basicServiceListFragment;
    private Character basicCharacter;
    private CharacteristicListFragment basicCharacteristicListFragment;
    private CharacteristicOperationFragment basicCharacteristicOperationFragment;
    private BluetoothGatt basicBluetoothGatt;

    private BluetoothGatt basicGatt;
    private BluetoothGattService basicService;
    private BluetoothGattCharacteristic basicCharacteristic;
    private BluetoothGattCharacteristic basicCharacteristicRead;


    /**
     * MainActivity的Fragment
     */
    private DeviceSettingFragment basicDeviceSettingFragment;
    private DeviceConnectFragment basicDeviceConnectFragment;
    private RealDataFragment basicRealDataFragment;

    private int[] mutilSensor = {9,7,2,3,4,5,6,7};

    private int state = 0; //0为老版本蓝精灵，1为新版本蓝精灵




    public int getCleaningTime() {
        return cleaningTime;
    }

    public void setCleaningTime(int cleaningTime) {
        this.cleaningTime = cleaningTime;
    }

    public int getCleaningCycles() {
        return cleaningCycles;
    }

    public void setCleaningCycles(int cleaningCycles) {
        this.cleaningCycles = cleaningCycles;
    }

    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public int getBasicType() {
        return basicType;
    }

    public void setBasicType(int basicType) {
        this.basicType = basicType;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getBasicDeviceName() {
        return basicDeviceName;
    }

    public void setBasicDeviceName(String basicDeviceName) {
        this.basicDeviceName = basicDeviceName;
    }

    public String getBasicMACAddress() {
        return basicMACAddress;
    }

    public void setBasicMACAddress(String basicMACAddress) {
        this.basicMACAddress = basicMACAddress;
    }

    public int getBasicDeviceAddress() {
        return basicDeviceAddress;
    }

    public void setBasicDeviceAddress(int basicDeviceAddress) {
        this.basicDeviceAddress = basicDeviceAddress;
    }

    public int getBasicDeviceType() {
        return basicDeviceType;
    }

    public void setBasicDeviceType(int basicDeviceType) {
        this.basicDeviceType = basicDeviceType;
    }

    public BleManager getBasicBleManager() {
        return basicBleManager;
    }

    public void setBasicBleManager(BleManager basicBleManager) {
        this.basicBleManager = basicBleManager;
    }

    public BleDevice getBasicBleDevice() {
        return basicBleDevice;
    }

    public void setBasicBleDevice(BleDevice basicBleDevice) {
        this.basicBleDevice = basicBleDevice;
    }

    public DeviceAdapter getBasicDeviceAdapter() {
        return basicDeviceAdapter;
    }

    public void setBasicDeviceAdapter(DeviceAdapter basicDeviceAdapter) {
        this.basicDeviceAdapter = basicDeviceAdapter;
    }

    public BleBluetooth getBasicBleBluetooth() {
        return basicBleBluetooth;
    }

    public void setBasicBleBluetooth(BleBluetooth basicBleBluetooth) {
        this.basicBleBluetooth = basicBleBluetooth;
    }

    public BluetoothGattService getBasicBluetoothGattService() {
        return basicBluetoothGattService;
    }

    public void setBasicBluetoothGattService(BluetoothGattService basicBluetoothGattService) {
        this.basicBluetoothGattService = basicBluetoothGattService;
    }

    public ServiceListFragment getBasicServiceListFragment() {
        return basicServiceListFragment;
    }

    public void setBasicServiceListFragment(ServiceListFragment basicServiceListFragment) {
        this.basicServiceListFragment = basicServiceListFragment;
    }

    public Character getBasicCharacter() {
        return basicCharacter;
    }

    public void setBasicCharacter(Character basicCharacter) {
        this.basicCharacter = basicCharacter;
    }

    public CharacteristicListFragment getBasicCharacteristicListFragment() {
        return basicCharacteristicListFragment;
    }

    public void setBasicCharacteristicListFragment(CharacteristicListFragment basicCharacteristicListFragment) {
        this.basicCharacteristicListFragment = basicCharacteristicListFragment;
    }

    public CharacteristicOperationFragment getBasicCharacteristicOperationFragment() {
        return basicCharacteristicOperationFragment;
    }

    public void setBasicCharacteristicOperationFragment(CharacteristicOperationFragment basicCharacteristicOperationFragment) {
        this.basicCharacteristicOperationFragment = basicCharacteristicOperationFragment;
    }

    public BluetoothGatt getBasicBluetoothGatt() {
        return basicBluetoothGatt;
    }

    public void setBasicBluetoothGatt(BluetoothGatt basicBluetoothGatt) {
        this.basicBluetoothGatt = basicBluetoothGatt;
    }

    public DeviceSettingFragment getBasicDeviceSettingFragment() {
        return basicDeviceSettingFragment;
    }

    public void setBasicDeviceSettingFragment(DeviceSettingFragment basicDeviceSettingFragment) {
        this.basicDeviceSettingFragment = basicDeviceSettingFragment;
    }

    public DeviceConnectFragment getBasicDeviceConnectFragment() {
        return basicDeviceConnectFragment;
    }

    public void setBasicDeviceConnectFragment(DeviceConnectFragment basicDeviceConnectFragment) {
        this.basicDeviceConnectFragment = basicDeviceConnectFragment;
    }

    public RealDataFragment getBasicRealDataFragment() {
        return basicRealDataFragment;
    }

    public void setBasicRealDataFragment(RealDataFragment basicRealDataFragment) {
        this.basicRealDataFragment = basicRealDataFragment;
    }

    public Boolean getBasicDeviceIsConnect() {
        return basicDeviceIsConnect;
    }

    public void setBasicDeviceIsConnect(Boolean basicDeviceIsConnect) {
        this.basicDeviceIsConnect = basicDeviceIsConnect;
    }

    public BluetoothGatt getBasicGatt() {
        return basicGatt;
    }

    public void setBasicGatt(BluetoothGatt basicGatt) {
        this.basicGatt = basicGatt;
    }

    public BluetoothGattService getBasicService() {
        return basicService;
    }

    public void setBasicService(BluetoothGattService basicService) {
        this.basicService = basicService;
    }

    public BluetoothGattCharacteristic getBasicCharacteristic() {
        return basicCharacteristic;
    }

    public void setBasicCharacteristic(BluetoothGattCharacteristic basicCharacteristic) {
        this.basicCharacteristic = basicCharacteristic;
    }

    public BluetoothGattCharacteristic getBasicCharacteristicRead() {
        return basicCharacteristicRead;
    }

    public void setBasicCharacteristicRead(BluetoothGattCharacteristic basicCharacteristicRead) {
        this.basicCharacteristicRead = basicCharacteristicRead;
    }

    public Boolean getBasicRealDataIsWorking() {
        return basicRealDataIsWorking;
    }

    public void setBasicRealDataIsWorking(Boolean basicRealDataIsWorking) {
        this.basicRealDataIsWorking = basicRealDataIsWorking;
    }

    public Boolean getBasicRealDataIsNotify() {
        return basicRealDataIsNotify;
    }

    public void setBasicRealDataIsNotify(Boolean basicRealDataIsNotify) {
        this.basicRealDataIsNotify = basicRealDataIsNotify;
    }

    public String getBasicIntervalTime() {
        return basicIntervalTime;
    }

    public void setBasicIntervalTime(String basicIntervalTime) {
        this.basicIntervalTime = basicIntervalTime;
    }

    public String getBasicTestTime() {
        return basicTestTime;
    }

    public void setBasicTestTime(String basicTestTime) {
        this.basicTestTime = basicTestTime;
    }

    public Boolean getBasicDeviceChange() {
        return basicDeviceChange;
    }

    public void setBasicDeviceChange(Boolean basicDeviceChange) {
        this.basicDeviceChange = basicDeviceChange;
    }

    public int getBasicDeviceBox() {
        return basicDeviceBox;
    }

    public void setBasicDeviceBox(int basicDeviceBox) {
        this.basicDeviceBox = basicDeviceBox;
    }

    public Boolean getBasicHistoryDataSign() {
        return basicHistoryDataSign;
    }

    public void setBasicHistoryDataSign(Boolean basicHistoryDataSign) {
        this.basicHistoryDataSign = basicHistoryDataSign;
    }

    public int[] getMutilSensor() {
        return mutilSensor;
    }

    public void setMutilSensor(int[] mutilSensor) {
        this.mutilSensor = mutilSensor;
    }

    public void setState(int state){
        this.state = state;
    }

    public int getState(){
        return state;
    }

    // 初始化多参数配置
    public void resetMutilSensor() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        if(pref.getString("mutilIsSet", "").equals("true")) {
            int[] unit = {2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 0};
            int[] type = {9, 7, 2, 3, 4, 5, 6, 7};
            if(pref.getString("mutilParameter1", "").equals("1")) {
                type[0] = 0;
                type[1] = 0;
            } else {
                type[0] = 9;
                type[1] = 7;
            }
            type[2] = unit[Integer.parseInt(pref.getString("mutilParameter2", ""))];
            type[3] = unit[Integer.parseInt(pref.getString("mutilParameter3", ""))];
            type[4] = unit[Integer.parseInt(pref.getString("mutilParameter4", ""))];
            type[5] = unit[Integer.parseInt(pref.getString("mutilParameter5", ""))];
            type[6] = unit[Integer.parseInt(pref.getString("mutilParameter6", ""))];
            type[7] = unit[Integer.parseInt(pref.getString("mutilParameter7", ""))];
            setMutilSensor(type);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setBasicDeviceIsConnect(false);
        setBasicRealDataIsWorking(false);
        setBasicRealDataIsNotify(false);
        setBasicDeviceChange(false);
        setBasicDeviceType(-1);
        setBasicType(-1);
        setIntervalTime(-1);
        setTestTime(-1);
        setCleaningCycles(-1);
        setCleaningTime(-1);
        setBasicDeviceBox(0);
        setBasicHistoryDataSign(true);
        resetMutilSensor();

    }


}
