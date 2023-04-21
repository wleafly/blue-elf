package com.clj.blesample;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.adapter.DeviceAdapter;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.tab.BottomAdapter;
import com.clj.blesample.tab.DeviceConnectFragment;
import com.clj.blesample.tab.DeviceSettingFragment;
import com.clj.blesample.tab.RealDataFragment;
import com.clj.blesample.tab.RealDataFragmentNew;
import com.clj.blesample.utils.GlobalData;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

//主要活动
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private LinearLayout layout_setting;
    private TextView txt_setting;
    private Button btn_scan;//btn扫描
    private EditText et_name, et_mac, et_uuid;
    private Switch sw_auto;
    private ImageView img_loading;//图片视图

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;

    private BottomNavigationView mBv;
    private ViewPager mVp;

    private FragmentManager manager;
    private FragmentTransaction transaction;
    private RealDataFragment realDataFragment;
    private RealDataFragmentNew realDataFragmentNew;
    private DeviceSettingFragment deviceSettingFragment;
    private DeviceConnectFragment deviceConnectFragment;

    private long exitTime = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private BottomAdapter adapter;

    public BottomAdapter getAdapter() {
        return adapter;
    }

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Fragment selectedFragment = null;

    private int state = 1;

    // 蓝牙FastBle的初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // android主程序开始，首先显示activity_main页面
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

            StrictMode.setVmPolicy(builder.build());

        }

        SharedPreferences test = getSharedPreferences("data",MODE_PRIVATE);
//        test.edit().clear().commit();
        if(test.getString("locale","") == "") {
            System.out.println("为空");

        }else{
            System.out.println("不为空");

            // 初始化软件语言
            Resources resources = getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();

            if(test.getString("locale","").equals("ENGLISH")) {
                config.locale = Locale.ENGLISH;
            }else{
                config.locale = Locale.CHINESE;
            }
            resources.updateConfiguration(config, dm);

        }


        setContentView(R.layout.activity_main);





        System.out.println("主程序开始运行9");
        System.out.println("MainActivity中的onCreate()方法");
        // 初始化布局
        initView();
//        verifyStoragePermissions(a)
        System.out.println("页面初始化");
        // 调用蓝牙初始化方法，可以进行一些自定义的配置，
        // 比如是否显示框架内部日志，重连次数和重连时间间隔，以及操作超时时间
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            System.out.println(getString(R.string.permisson_not_open));

            new AlertDialog.Builder(this).setMessage(getString(R.string.permisson_open)).setNegativeButton(getString(R.string.permisson_to_open), (dialog, which) -> {
                // 获取权限
                setPermissions();
            }).setCancelable(false).show();

        }

        // 判断蓝牙
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 打开蓝牙：自动跳出
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }



        fragmentManager = getSupportFragmentManager();
        realDataFragment = new RealDataFragment();
        realDataFragmentNew = new RealDataFragmentNew();
        deviceConnectFragment = new DeviceConnectFragment();
        deviceSettingFragment = new DeviceSettingFragment();

        // 默认显示实时数据页面中的一个Fragment
        if (state == 0) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, realDataFragment);
            fragmentTransaction.commit();
        } else if (state == 1) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, realDataFragmentNew);
            fragmentTransaction.commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setLogo(R.mipmap.logo1);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);



        /**
         * 下方的导航栏
         */
        mBv = (BottomNavigationView) findViewById(R.id.bv);
        mVp = (ViewPager) findViewById(R.id.vp);
//        BottomNavigationViewHelper.disableShiftMode(mBv);
        mVp.setOffscreenPageLimit(2);

        //这里可true是一个消费过程，同样可以使用break，外部返回true也可以
        mBv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                state = ((MyApplication) getApplication()).getState();
                switch (item.getItemId()) {
                    case R.id.item_real_data:
                        System.out.println("MainActivity的state:"+state);
                        if (state == 0) {
                            if (realDataFragment.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                                realDataFragment.onResume();
                            }
                            if (deviceConnectFragment.isAdded()) {
                                System.out.println("deviceConnectFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                            }
                            selectedFragment = new RealDataFragment();
                        } else if (state == 1) {
                            if (realDataFragmentNew.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                                realDataFragmentNew.onResume();
                            }
                            if (deviceConnectFragment.isAdded()) {
                                System.out.println("deviceConnectFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                            }
                            selectedFragment = new RealDataFragmentNew();
                        }

                        System.out.println("你点击可realdata");

                        mVp.setCurrentItem(0);
                        break;

                    case R.id.item_device_connect:
                        System.out.println("你点击可deviceConnect");
                        if (state == 0){
                            if (realDataFragment.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                            }
                        } else if (state == 1){
                            if (realDataFragmentNew.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                            }
                        }
                        selectedFragment = new DeviceConnectFragment();
                        mVp.setCurrentItem(1);
                        break;

                    case R.id.item_device_setting:
                        System.out.println("你点击可deviceSetting");
                        if (state == 0){
                            if (realDataFragment.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                            }
                            if (deviceConnectFragment.isAdded()) {
                                System.out.println("deviceConnectFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                                deviceSettingFragment.onResume();
                            }
                        } else if (state==1) {
                            if (realDataFragmentNew.isAdded()) {
                                System.out.println("RealDataFragment已经存在");
                            }
                            if (deviceConnectFragment.isAdded()) {
                                System.out.println("deviceConnectFragment已经存在");
                            }
                            if (deviceSettingFragment.isAdded()) {
                                System.out.println("deviceSettingFragment已经存在");
                                deviceSettingFragment.onResume();
                            }
                        }
                        selectedFragment = new DeviceSettingFragment();
                        mVp.setCurrentItem(2);
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                return true;
            }
        });


        // 数据填充，静态设置所有的Fragment
        setupViewPager(mVp);
        mVp.setCurrentItem(0);
        //ViewPager监听
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onPageSelected(int position) {
                System.out.println("这里是滑动页面" + position);
                switch (position) {
                    case 0:
                        if (state == 0) {
//                            realDataFragment.onResume();
                        } else if (state == 1) {
//                            realDataFragmentNew.onResume();
                        }
                        break;
                    case 1:
                        break;
                    case 2:
                        deviceSettingFragment.onResume();
                        break;

                }
                mBv.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MainActivity中的onResume()方法");

        state = ((MyApplication) getApplication()).getState();
        System.out.println("MainActivity页面的state:"+state);

    }

    //  当活动将被销毁时调用
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }


    /**
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                System.out.println("调用了MainActivity中的onClick");
//                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
//                    // 点击开始扫描，从核实权限开始
//                    deviceConnectFragment.checkPermissions();
//
//                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
//                    // 点击停止扫描，就调用取消扫描函数
//                    BleManager.getInstance().cancelScan();
//                }
                break;

        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    // 初始化布局
    // 初始化视图所进行的页面渲染
    private void initView() {
        /**
         * 上方的ToolBar
         */



        /**
         * 中间的列表层----设备列表
         */
//        btn_scan = (Button) findViewById(R.id.btn_scan);
//        btn_scan.setText(getString(R.string.start_scan));
        // 注册监听器
        // 当前所在类需要implements SDK中Android view的interface OnClickListener
        // 对应 开始扫描 的按钮
//        btn_scan.setOnClickListener(this);

        // 拿到输入蓝牙设备进行查找的控件
//        et_name = (EditText) findViewById(R.id.et_name);
//        et_mac = (EditText) findViewById(R.id.et_mac);
//        et_uuid = (EditText) findViewById(R.id.et_uuid);
//        sw_auto = (Switch) findViewById(R.id.sw_auto);

        // 拿到扫描设备条件的控件
//        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
//        txt_setting = (TextView) findViewById(R.id.txt_setting);
//        txt_setting.setOnClickListener(this);
        // 设置控件属性为隐藏（GONE）、可见（visible）、不可见（invisible）
//        layout_setting.setVisibility(View.GONE);
//        txt_setting.setText(getString(R.string.expand_search_settings));


        /**
         * 这里是开始按钮及其加载项的内容
         */

    }


    //
    private void setupViewPager(ViewPager viewPager) {
        adapter = new BottomAdapter(getSupportFragmentManager());
        if (state == 0) {
            adapter.addFragment(new RealDataFragment());
        } else if (state == 1) {
            adapter.addFragment(new RealDataFragmentNew());
        }
        adapter.addFragment(new DeviceConnectFragment());
        adapter.addFragment(new DeviceSettingFragment());
        viewPager.setAdapter(adapter);
        manager = getSupportFragmentManager();
        if (state == 0) {
            realDataFragment = (RealDataFragment) adapter.getItem(0);
            deviceConnectFragment = (DeviceConnectFragment) adapter.getItem(1);
            deviceSettingFragment = (DeviceSettingFragment) adapter.getItem(2);
        } else if (state == 1) {
            realDataFragmentNew = (RealDataFragmentNew) adapter.getItem(0);
            deviceConnectFragment = (DeviceConnectFragment) adapter.getItem(1);
            deviceSettingFragment = (DeviceSettingFragment) adapter.getItem(2);
        }
    }

    private void setPermissions() {
        // 检查权限
        XXPermissions.with(this)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if(all) {
                            System.out.println("权限获取成功");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            System.out.println("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                            System.out.println("获取录音和日历权限失败");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(this, Permission.ACCESS_FINE_LOCATION)) {
                System.out.println("用户已经在权限设置页授予位置权限");
            } else {
                System.out.println("用户没有在权限设置页授予权限");
            }
            if(XXPermissions.isGranted(this, Permission.WRITE_EXTERNAL_STORAGE) &&
                XXPermissions.isGranted(this, Permission.WRITE_EXTERNAL_STORAGE)) {
                System.out.println("用户已经在权限位置页授予存储权限");
            } else {
                System.out.println("用户没有在权限设置页授予权限");
            }
        }
    }

    @Override
    public void onBackPressed() {
        System.out.println("nihao");
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), getString(R.string.again_quit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            //彻底关闭整个APP
            int currentVersion = android.os.Build.VERSION.SDK_INT;
            if (currentVersion > android.os.Build.VERSION_CODES.ECLAIR_MR1) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                System.exit(0);
            } else {// android2.1
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                am.restartPackage(getPackageName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first_menu, menu);
        return true;
    }



    private void switchRealDataFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment;

        if (state == 0) {
            fragment = new RealDataFragment();
        } else {
            fragment = new RealDataFragmentNew();
        }

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
