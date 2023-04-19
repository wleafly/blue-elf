package com.clj.blesample.domain;

public class SensorData {

    private String data; // 传感器数值
    private String temp; // 传感器温度
    private int time1; // 传感器间隔时间
    private int time2; // 传感器测试时间

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getTime1() {
        return time1;
    }

    public void setTime1(int time1) {
        this.time1 = time1;
    }

    public int getTime2() {
        return time2;
    }

    public void setTime2(int time2) {
        this.time2 = time2;
    }
}
