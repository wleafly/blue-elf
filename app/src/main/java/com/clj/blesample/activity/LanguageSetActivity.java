package com.clj.blesample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.utils.DyLineChartUtils;
import com.clj.blesample.utils.OpenFileUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

import static android.support.v4.content.FileProvider.getUriForFile;


public class LanguageSetActivity extends AppCompatActivity {

    private Boolean globalDeviceIsConnect; // 设备是否连接
    private BleDevice bleDevice;
    private BluetoothGatt gatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicRead;
    private int chipType = 0; // 芯片类型

    private Toolbar toolbar;
    private View simpleChinese;
    private View complexChinese;
    private View english;

    private ImageView simpleChineseImage;
    private ImageView complexChineseImage;
    private ImageView englishImage;

    private View languageSet;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_set);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

            StrictMode.setVmPolicy(builder.build());

        }

        Button btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openAssignFolder("storage/emulated/0/蓝牙/2022-02-25/氨氮.txt");
                openAssignFolder("/storage/emulated/0/Android/data/com/cli.blesample/files/蓝牙/2022-02-28/氨氮.txt");
//                openAssignFolder("sdcard/蓝牙/2022-02-25/氨氮.txt");
            }
        });

        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openAssignFolder2("storage/emulated/0/蓝牙/2022-02-25/氨氮.txt");
//                openAssignFolder2("/storage/emulated/0/Android/data/com/cli.blesample/files/蓝牙/2022-02-28/氨氮.txt");
//                openAssignFolder2("sdcard/蓝牙/2022-02-25/氨氮.txt");
                openAssignFolder2("sdcard/蓝牙/");
            }
        });

        Button btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAssignFolder3("storage/emulated/0/蓝牙");
//                openAssignFolder3("/storage/emulated/0/Android/data/com/cli.blesample/files/蓝牙/2022-03-03/氨氮.txt");
//                openAssignFolder3("sdcard/蓝牙/2022-02-25/氨氮.txt");
//                openAssignFolder3("sdcard/");
            }
        });


        globalDeviceIsConnect = ((MyApplication)getApplication()).getBasicDeviceIsConnect();
        // 获取全局数据
        if(globalDeviceIsConnect) {
            bleDevice = ((MyApplication)getApplication()).getBasicBleDevice();
            gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
            if (gatt != null) {
                for (BluetoothGattService s : gatt.getServices()) {
                    service = s;
                    if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                        // 安信可芯片
                        BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
                        int ii = 0;
                        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                            System.out.println("特征值值内容：" + c.getUuid().toString());
                            cs[ii ++] = c;
                        }
                        characteristicRead = cs[1];
                        characteristic = cs[0];
                        chipType = 1;
                        break;
                    }
                    chipType = 2;
                }
            }
            if(chipType == 2) {
                // BT02-E104芯片
                for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                    // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
                    String uid = c.getUuid().toString().split("-")[0];
                    if ("0000fff1".equals(uid)) {
                        //                if ("5833ff02".equals(uid)) {
                        characteristicRead = c;
                    } else if ("0000fff2".equals(uid)) {
                        //                } else if ("5833ff03".equals(uid)) {
                        characteristic = c;
                    }
                }
            }

            BleManager.getInstance().stopNotify(bleDevice,characteristicRead.getService().getUuid().toString(),characteristicRead.getUuid().toString());

        }


        // 初始化导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.language_set_activity));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 设置导航（返回图片）的点击事件
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 退出当前页面
                finish();
            }
        });

        languageSet = findViewById(R.id.language_set);


        simpleChinese = findViewById(R.id.simple_chinese);
        complexChinese = findViewById(R.id.complex_chinese);
        english = findViewById(R.id.english);

        simpleChineseImage = (ImageView) findViewById(R.id.simple_chinese_image);
        complexChineseImage = (ImageView) findViewById(R.id.complex_chinese_image);
        englishImage = (ImageView) findViewById(R.id.english_image);


        Drawable drawableImage = getResources().getDrawable(R.mipmap.ic_select);

        // 初始化语言选择
        SharedPreferences test = getSharedPreferences("data",MODE_PRIVATE);
        if(test.getString("locale","").equals("SIMPLECHINESE")) {
            setImage(drawableImage, 1);
        }else if(test.getString("locale","").equals("COMPLEXCHINESE")) {
            setImage(drawableImage, 2);
        }else if(test.getString("locale","").equals("ENGLISH")) {
            setImage(drawableImage, 3);
        }else {
            setImage(drawableImage, 1);
        }

        simpleChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage(drawableImage, 1);
            }
        });
        complexChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage(drawableImage, 2);
            }
        });
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setImage(drawableImage, 3);
            }
        });

        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("我点击了");
                Resources resources = (LanguageSetActivity.this).getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                // 应用用户选择语言
                config.locale = Locale.ENGLISH;
                resources.updateConfiguration(config, dm);

                SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                if(simpleChineseImage.getBackground() == drawableImage) {
                    System.out.println("111");
                    editor.putString("locale","SIMPLECHINESE");
                }else if(complexChineseImage.getBackground() == drawableImage) {
                    System.out.println("222");
                    editor.putString("locale","COMPLEXCHINESE");
                }else if(englishImage.getBackground() == drawableImage) {
                    System.out.println("333");
                    editor.putString("locale","ENGLISH");
                }else{
                    System.out.println("444");
                    editor.putString("locale","");
                }

                editor.commit();

                System.out.println(pref.getString("locale",""));

                new AlertDialog.Builder(languageSet.getContext()).setMessage(getString(R.string.language_set_success)).setNegativeButton(getString(R.string.language_effect), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ((MyApplication)getApplication()).setBasicDeviceIsConnect(false);
                        Intent intent = new Intent(LanguageSetActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }).setCancelable(false).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setImage(Drawable drawableImage, int n) {
        simpleChineseImage.setBackground(null);
        complexChineseImage.setBackground(null);
        englishImage.setBackground(null);

        if(n == 1) {
            simpleChineseImage.setBackground(drawableImage);
        }else if(n == 2) {
            complexChineseImage.setBackground(drawableImage);
        }else if(n == 3) {
            englishImage.setBackground(drawableImage);
        }
    }
//    private void openAssignFolder(String path){
//        File file = new File(path);
//        if(null==file || !file.exists()){
//            return;
//        }
////        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
////        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addCategory("android.intent.category.DEFAULT");
////        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        intent.setDataAndType(Uri.fromFile(file), "file/*");
//
////        Uri fileUri = FileProvider.getUriForFile(this, "com.clj.blesample.fileprovider", file);
//        Uri fileUri = FileProvider.getUriForFile(this, "com.clj.blesample.fileprovider", file);
//
//        // 重新构造Uri：content://
////        File imagePath = new File(Context.getFilesDir(), "images");
////        if (!imagePath.exists()){imagePath.mkdirs();}
////        File newFile = new File(imagePath, "default_image.jpg");
////        File imagePath = new File(Context.getFilesDir(), "蓝牙");
////        if (!imagePath.exists()){imagePath.mkdirs();}
////        File newFile = new File(imagePath, "default_image.jpg");
////        Uri contentUri = getUriForFile(getContext(),
////                "com.mydomain.fileprovider", newFile);
//        Uri downloadFileUri = Uri.parse("file://" + path);
//        Intent installIntent = new Intent(Intent.ACTION_VIEW);
//        installIntent.setDataAndType(fileUri, "*/*");
//
//        intent.setDataAndType(fileUri, "text/plain");
//        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION
//                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
////        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//        // 授予目录临时共享权限
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        startActivityForResult(intent, 100);
////        try {
////            startActivity(intent);
////            startActivity(Intent.createChooser(intent,"选择浏览工具"));
////        } catch (ActivityNotFoundException e) {
////            e.printStackTrace();
////        }
//    }

//    private void openAssignFolder(String path) {
//        System.out.println("点击");
//        File file = new File(path);
//        if(null==file || !file.exists()){
//            System.out.println("未找到对应文件");
//            return;
//        }
//        try {
//            OpenFileUtils.openFile(this, file);
//        } catch (Exception e) {
//            System.out.println("errorerrorerrorerrorerrorerrorerrorerrorerrorerrorerrorerrorerror");
//            e.printStackTrace();
//        }
//    }

    private void openAssignFolder(String figurepath) {
//        String figurepath;
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            System.out.println("yesyesyesyesyesyesyesyesyesyes");
            if (Build.VERSION.SDK_INT >= 29){
                sdDir = getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();
            }
        } else {
            System.out.println("nononononononononononononono");
            sdDir = Environment.getRootDirectory();
        }
//        figurepath = sdDir.getAbsolutePath() + "/蓝牙/20";
//        figurepath = "/M2102J2SC/蓝牙";
        System.out.println(figurepath);

        // 手机打得开，ES文件管理器路径正确，文件打不开
//        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.parse(figurepath), "*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,0);

//        平板调用ES文件管理器可打开，手机打不开
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse(figurepath), "*/*");
//        startActivityForResult(intent,0);

    }

    private void openAssignFolder2(String figurepath) {
//        String figurepath;
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            System.out.println("yesyesyesyesyesyesyesyesyesyes");
            if (Build.VERSION.SDK_INT >= 29){
                sdDir = getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();
            }
        } else {
            System.out.println("nononononononononononononono");
            sdDir = Environment.getRootDirectory();
        }
//        figurepath = sdDir.getAbsolutePath() + "/蓝牙/20";
//        figurepath = "/M2102J2SC/蓝牙";
        System.out.println(figurepath);

        // 手机打得开，ES文件管理器路径正确，文件打不开
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.parse(figurepath), "*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(intent,0);

//        平板调用ES文件管理器可打开，手机打不开
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(figurepath), "*/*");
        startActivityForResult(intent,0);

    }

    private void openAssignFolder3(String figurepath) {
//        String figurepath;
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            System.out.println("yesyesyesyesyesyesyesyesyesyes");
            if (Build.VERSION.SDK_INT >= 29){
                sdDir = getExternalFilesDir(null);
            }else {
                sdDir = Environment.getExternalStorageDirectory();
            }
        } else {
            System.out.println("nononononononononononononono");
            sdDir = Environment.getRootDirectory();
        }
//        figurepath = sdDir.getAbsolutePath() + "/蓝牙/";
//        figurepath = "/M2102J2SC/蓝牙";
        System.out.println(figurepath);
        System.out.println(Uri.parse(figurepath));

        // 手机打得开，ES文件管理器路径正确，文件打不开
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.parse(figurepath), "*/*");
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        startActivityForResult(intent,0);

//        平板调用ES文件管理器可打开，手机打不开
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(Uri.parse(figurepath), "text/plain");
//        intent.addCategory("android.intent.category.DEFAULT");
//        startActivityForResult(intent,0);


        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 跳转到最近
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setDataAndType(Uri.parse(figurepath), "*/*");
        startActivityForResult(intent,0);


    }



}
