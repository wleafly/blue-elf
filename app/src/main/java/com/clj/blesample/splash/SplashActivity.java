package com.clj.blesample.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.TextView;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;

public class SplashActivity extends Activity {

    // private final int SPLASH_DISPLAY_LENGHT = 2000; // 两秒后进入系统
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        //getSupportActionBar().hide();//隐藏标题栏
        setContentView(R.layout.activity_splash);



        TextView t = (TextView) findViewById(R.id.textView);

        CountDownTimer timer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
                t.setText(" " + millisUntilFinished / 1000 + "  S ");
            }

            @Override
            public void onFinish() {
                t.setText(" " + 0 + "  S ");
                Intent it = new Intent(getApplicationContext(), MainActivity.class);//启动MainActivity
                startActivity(it);
                finish();//关闭当前活动
            }
        };
        timer.start();

//        Thread myThread = new Thread() {//创建子线程
//            @Override
//            public void run() {
//                try {
//                    t.setText(" 1  S ");
//                    sleep(1000);//使程序休眠五秒
//                    Intent it = new Intent(getApplicationContext(), MainActivity.class);//启动MainActivity
//                    startActivity(it);
//                    finish();//关闭当前活动
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        myThread.start();//启动线程


    }
}
