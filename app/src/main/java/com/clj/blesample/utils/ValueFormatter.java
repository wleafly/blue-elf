package com.clj.blesample.utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ViewPortHandler;

public interface ValueFormatter {

    String getFormattedValue(float value, Entry entry, int dataSetIndex,
                             ViewPortHandler viewPortHandler);
}
