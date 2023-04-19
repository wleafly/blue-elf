package com.clj.blesample.utils;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BatteryView extends View {


    private  int batteryColor = Color.WHITE;
    private  int batteryValue = 10;
    private  int batteryHeight = 0;
    private  int batteryWidth = 0;
    public BatteryView(Context context) {
        super(context);
    }
    public BatteryView(Context context, AttributeSet attrs) {

        super(context, attrs );
    }

    public BatteryView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        batteryHeight=heightMeasureSpec;
        batteryWidth=widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 要画图形，最起码要有三个对象：
     * 1.颜色对象 Color
     * 2.画笔对象 Paint
     * 3.画布对象 Canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //1.创建画笔
        Paint myPaint = new Paint();
        myPaint.setColor(batteryColor);
        //让画出的图形是空心的
        myPaint.setStyle(Paint.Style.STROKE);
        //设置画出的线的 粗细程度
        myPaint.setStrokeWidth(batteryHeight);
        //画出一根线
        canvas.drawLine(0,0, batteryValue,0, myPaint);
        super.onDraw(canvas);
    }
    //重绘画
    public void rePaint(int batteryColor,int batteryValue )
    {
        this.batteryColor=batteryColor;
        this.batteryValue=batteryValue;
        invalidate();
    }
}
