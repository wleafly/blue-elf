package com.clj.blesample.entity;

import com.clj.blesample.utils.DyLineChartUtils;

public class Chart {
    String param;
    String value;
    String unit;
    int max;
    String address;
    DyLineChartUtils chartUtil;
    String temperature;
    String mud;
    String bod;


    public Chart(String param, String value,String unit,int max,String address) {
        this.param = param;
        this.value = value;
        this.unit = unit;
        this.max = max;
        this.address = address;
    }

    public String getMud() {
        return mud;
    }

    public void setMud(String mud) {
        this.mud = mud;
    }

    public String getBod() {
        return bod;
    }

    public void setBod(String bod) {
        this.bod = bod;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DyLineChartUtils getChartUtil() {
        return chartUtil;
    }

    public void setChartUtil(DyLineChartUtils chartUtil) {
        this.chartUtil = chartUtil;
    }
}
