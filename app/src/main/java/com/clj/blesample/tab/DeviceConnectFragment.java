package com.clj.blesample.tab;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.adapter.DeviceAdapter;
import com.clj.blesample.application.MyApplication;
import com.clj.blesample.comm.ObserverManager;
import com.clj.blesample.operation.OperationActivity;
import com.clj.blesample.utils.BackEndUtil;
import com.clj.blesample.utils.GlobalData;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.MyLog;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;


public class DeviceConnectFragment extends Fragment implements View.OnClickListener {

    MainActivity act = (MainActivity)getActivity();

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_OPEN_GPS = 1;//请求代码打开GPS
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 2; //请求代码权限位置
    private static  boolean acc=false; //请求代码权限位置

    private LinearLayout layout_setting;
    private TextView txt_setting;
    private ImageView img_loading;//图片视图
    private Button btn_scan;//btn扫描
    private EditText et_name, et_mac, et_uuid;
    private Switch sw_auto;

    private View connectLayout;
    private Drawable drawableInit;
    private Drawable drawable;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    private View v;
    private View view;
    private View deviceLayout;
    private WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;

    private View connectDeviceList;
    private ListView usableDeviceList;
    private View deviceLayout1;
    private View deviceLayout2;
    private View deviceLayout3;
    private TextView deviceName1;
    private TextView deviceName2;
    private TextView deviceName3;
    private View usableTitle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.device_connect_list2, null);
            v = view;
            initView(view);
            System.out.println("DeviceConnectFragment中的onCreateView()方法");
        }

        connectDeviceList = v.findViewById(R.id.device_connected);
        usableDeviceList = v.findViewById(R.id.usable_device_list);
        deviceLayout1 = v.findViewById(R.id.device_layout2);
        deviceLayout2 = v.findViewById(R.id.device_layout2);
        deviceLayout3 = v.findViewById(R.id.device_layout3);
        deviceName1 = v.findViewById(R.id.txt_name1);
        deviceName2 = v.findViewById(R.id.txt_name2);
        deviceName3 = v.findViewById(R.id.txt_name3);
        usableTitle = v.findViewById(R.id.usable_title);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // 当活动可见时调用
    @Override
    public void onResume() {
        super.onResume();
        System.out.println("DeviceConnectFragment中的onResume()方法");
        showConnectedDevice();
    }

    private void initView(View v) {
        //加载
        img_loading = (ImageView) v.findViewById(R.id.img_loading);
        // 载入动画：开始扫描的时候，绿色的转圈动画
        operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(getContext());

        connectLayout = v.findViewById(R.id.connect_layout);
        drawableInit = getResources().getDrawable(R.mipmap.ic_down_init);
        drawable = getResources().getDrawable(R.color.background_gray2);

        deviceLayout = v.findViewById(R.id.device_layout);
        btn_scan = (Button) v.findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));

        btn_scan.setOnClickListener(this);

        // 下拉样式
        mWaveSwipeRefreshLayout = (WaveSwipeRefreshLayout) v.findViewById(R.id.main_swipe);
        mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.BLACK);
        mWaveSwipeRefreshLayout.setWaveColor(getResources().getColor(R.color.colorPrimary));
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override public void onRefresh() {

                System.out.println("下拉刷新");
                connectLayout.setBackground(drawable);
                usableTitle.setVisibility(View.VISIBLE);

                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("权限识别未打开");

                    new AlertDialog.Builder(getContext()).setMessage(getString(R.string.permisson_open)).setNegativeButton(getString(R.string.permisson_to_open), (dialog, which) -> {
                        mWaveSwipeRefreshLayout.setRefreshing(false);
                        // 获取权限
                        setPermissions();
                    }).setCancelable(false).show();
                    return;
                }
                checkPermissions();
            }
        });


        // 设备适配器
        mDeviceAdapter = new DeviceAdapter(getContext());

        // 给适配器创建一个点击事件
        // 在页面单击：连接、断开连接、进入操作
        // 接口在DeviceAdapter.java里面
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            // 连接套接口时触发事件
            // 点击  连接  按钮触发事件
            public void onConnect(BleDevice bleDevice) {
                System.out.println("点击连接事件触发");

                // 如果蓝牙没有连接
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    // 取消扫描
                    BleManager.getInstance().cancelScan();
                    if(BleManager.getInstance().isConnected(((MyApplication)getActivity().getApplication()).getBasicBleDevice())) {
                        onDisConnect(((MyApplication)getActivity().getApplication()).getBasicBleDevice());
//                        progressDialog.show();
                    }
                    // 进行该设备的连接
                    connect(bleDevice);
                }else {

                    // 在这里发送一条数据
                    // BackEndUtil.sendConnectionCode(bleDevice, BleManager.getInstance().getBluetoothGatt(bleDevice),"bb");
                    // 取消该设备的连接
                    onDisConnect(bleDevice);
                }
            }

            // 点击  断开连接  触发事件
            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                // 如果该设备连接成功
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    // 取消该设备的连接
                    BleManager.getInstance().disconnect(bleDevice);
//                    mDeviceAdapter.removeDevice(bleDevice);
                }
            }

            // 点击 进入操作 触发事件
            @Override
            public void onDetail(BleDevice bleDevice) {
                // 如果该设备连接成功
                if (BleManager.getInstance().isConnected(bleDevice)) {

                    // 从这个页面开始，跳转到OperationActivity.java
                    Intent intent = new Intent((MainActivity)getActivity(), OperationActivity.class);
                    // 给目标传递值
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    // 不需要返回值，直接进行
                    startActivity(intent);
                }
            }
        });

        // 获取设备列表，将设备适配器放到里面去
//        ListView listView_device = (ListView) v.findViewById(R.id.list_device);
//        listView_device.setAdapter(mDeviceAdapter);

        ListView listView_device= v.findViewById(R.id.usable_device_list);
        listView_device.setAdapter(mDeviceAdapter);




    }

    // 显示连接设备
    public void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }

        mDeviceAdapter.notifyDataSetChanged();
    }

    //设置扫描规则
    private void setScanRule() {
        String[] uuids;
//        String str_uuid = et_uuid.getText().toString();
        String str_uuid = "";
        if (TextUtils.isEmpty(str_uuid)) {
            uuids = null;
        } else {
            uuids = str_uuid.split(",");
        }
        UUID[] serviceUuids = null;
        if (uuids != null && uuids.length > 0) {
            serviceUuids = new UUID[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                String name = uuids[i];
                String[] components = name.split("-");
                if (components.length != 5) {
                    serviceUuids[i] = null;
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i]);
                }
            }
        }

        String[] names;
//        String str_name = et_name.getText().toString();
        String str_name = "";
        if (TextUtils.isEmpty(str_name)) {
            names = null;
        } else {
            names = str_name.split(",");
        }

//        String mac = et_mac.getText().toString();
        String type = "";
//        boolean isAutoConnect = sw_auto.isChecked();

        String mac = "";
        boolean isAutoConnect = false;

        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
                .setDeviceName(true, names)   // 只扫描指定广播名的设备，可选
                .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    // 开始扫描
    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
                System.out.println("正在搜索设备");
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            // 扫描开始
            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }


            // 扫描完成
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                mWaveSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    // 页面点击按钮进行连接
    // 传入设备信息
    private void connect(final BleDevice bleDevice) {
        // 调用BleManager,等待回调
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            //开始连接
            @Override
            public void onStartConnect() {
                // progressDialog进度对话框：连接过程中的转圈圈
                progressDialog.show();
            }

            //连接失败
            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(getContext(), getString(R.string.connect_fail), Toast.LENGTH_LONG).show();
            }

            // 连接成功
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss(); // 进度对话框
                mDeviceAdapter.addDevice(bleDevice); // 把此设备加入到连接的设备中去
                // notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
                mDeviceAdapter.notifyDataSetChanged();

                // 连接成功，就显示到列表最上方
//                ListView listView_device = (ListView) v.findViewById(R.id.list_device);
//                listView_device.setStackFromBottom(false);
                LinearLayout deviceConnected = v.findViewById(R.id.device_connected);
                deviceConnected.setVisibility(View.VISIBLE);
                TextView deviceConnectedName = v.findViewById(R.id.device_connected_name);
                deviceConnectedName.setText(bleDevice.getName());


                // 新版本将设备信息BleManager公用,将数据存储到全局文件中去
                System.out.println("这里把bleDevice信息存储到全局变量里面去");
                ((MyApplication)getActivity().getApplication()).setBasicBluetoothGatt(gatt);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceName(bleDevice.getName());
                ((MyApplication)getActivity().getApplication()).setBasicMACAddress(bleDevice.getMac());
                ((MyApplication)getActivity().getApplication()).setBasicBleDevice(bleDevice);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceAdapter(mDeviceAdapter);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceIsConnect(true);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceChange(true);

//                if(((MyApplication)getActivity().getApplication()).getBasicDeviceAddress() != 0) {
//                    System.out.println("在这里不等于0，执行此函数");
//                    BleManager.getInstance().stopNotify(
//                            ((MyApplication)getActivity().getApplication()).getBasicBleDevice(),
//                            ((MyApplication)getActivity().getApplication()).getBasicCharacteristic().getService().getUuid().toString(),
//                            ((MyApplication)getActivity().getApplication()).getBasicCharacteristic().getUuid().toString());
//
//                }

                ((MyApplication)getActivity().getApplication()).setBasicBleDevice(bleDevice);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceAddress(0);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceType(-1);



                // 在这里发送一条数据
//                BackEndUtil.sendConnectionCode(bleDevice, BleManager.getInstance().getBluetoothGatt(bleDevice),"f5");
//                mDeviceAdapter.removeDevice(bleDevice);


                new AlertDialog.Builder(deviceConnected.getContext()).setMessage(getString(R.string.connect_success)).setPositiveButton(getString(R.string.connect_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        TextView name = deviceConnected.findViewById(R.id.txt_name);
//                        System.out.println("名字：" + name.getText());
//                        String energy = "87";
//                        name.setText(name.getText() + "    (" + energy + "%)");
//                        System.out.println("名字：" + name.getText());

                        //获取BluetoothGattCharacteristic
                        // 打开监听
                        BluetoothGattCharacteristic characteristicRead = null;
                        BluetoothGattCharacteristic characteristicWrite = null;
                        BluetoothGattService service = null;
                        int chipType = 0;
                        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
                        if (gatt != null) {
                            for (BluetoothGattService s : gatt.getServices()) {
                                service = s;
                                if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
                                    // 安信可芯片
                                    System.out.println("安信可芯片");
                                    BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
                                    int ii = 0;
                                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                                        System.out.println("特征值值内容：" + c.getUuid().toString());
                                        cs[ii ++] = c;
                                    }
                                    characteristicWrite = cs[0];
                                    characteristicRead = cs[1];

                                    //                        //向蓝牙设备发送f5
                                    BluetoothGattCharacteristic finalCharacteristicRead = characteristicRead;
                                    BleManager.getInstance().write(bleDevice, characteristicWrite.getService().getUuid().toString(),
                                            characteristicWrite.getUuid().toString(),
                                            HexUtil.hexStringToBytes("f5"),
                                            new BleWriteCallback() {

                                                @Override
                                                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                                    Log.i(TAG, "发送f5返回的current结果: "+current);
                                                    Log.i(TAG, "发送f5返回的total结果: "+total);

//                                                    new Handler().postDelayed(new Runnable() {
//                                                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//                                                        @Override
//                                                        public void run() {
                                                            BleManager.getInstance().notify(
                                                                    bleDevice,
                                                                    finalCharacteristicRead.getService().getUuid().toString(),
                                                                    finalCharacteristicRead.getUuid().toString(),
                                                                    new BleNotifyCallback() {

                                                                        @Override
                                                                        public void onNotifySuccess() {
                                                                            System.out.println("onNotifySuccess被执行");
                                                                        }

                                                                        @Override
                                                                        public void onNotifyFailure(final BleException exception) {
                                                                            System.out.println("onNotifyFailure被执行");
                                                                        }

                                                                        @Override
                                                                        public void onCharacteristicChanged(final byte[] data) {
                                                                                System.out.println("fa指令开始读取地址和设备类型信息...");
                                                                                new Handler().postDelayed(new Runnable() {
                                                                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                                                                    @Override
                                                                                    public void run() {
                                                                                        String type = HexUtil.byteToString(data);
                                                                                        System.out.println("设备类型如下：");
                                                                                        System.out.println(type);
                                                                                        if (type.contains("[MANy]")){
                                                                                            //通过设置全局变量修改实时数据fragment
                                                                                            System.out.println("执行切换碎片语句");
                                                                                            GlobalData.isNewDevice = true;
                                                                                            BottomAdapter adapter = ((MainActivity) getActivity()).getAdapter();
                                                                                            adapter.updateFragment(0,new RealDataFragmentNew());
//                                                                                                System.out.println(adapter.getFragments());
                                                                                        }

                                                                                    }
                                                                                }, 100);



                                                                        }
                                                                    });


//                                                        }
//                                                    }, 1000);

                                                }

                                                @Override
                                                public void onWriteFailure(BleException exception) {
                                                    Log.i(TAG, "发送f5失败");

                                                }
                                            });

                                    chipType = 1;
                                    break;
                                }
                                chipType = 2;
                            }

                        }



//                        if(false) {
//                            // 打开监听
//                            BluetoothGattCharacteristic characteristicRead = null;
//                            BluetoothGattService service = null;
//                            int chipType = 0;
//                            BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
//                            if (gatt != null) {
//                                for (BluetoothGattService s : gatt.getServices()) {
//                                    service = s;
//                                    if("55535343-fe7d-4ae5-8fa9-9fafd205e455".equals(s.getUuid().toString())) {
//                                        // 安信可芯片
//                                        System.out.println("安信可芯片");
//                                        BluetoothGattCharacteristic[] cs = new BluetoothGattCharacteristic[2];
//                                        int ii = 0;
//                                        for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
//                                            System.out.println("特征值值内容：" + c.getUuid().toString());
//                                            cs[ii ++] = c;
//                                        }
//                                        characteristicRead = cs[1];
//                                        chipType = 1;
//                                        break;
//                                    }
//                                    chipType = 2;
//                                }
//
//                            }
//                            if(chipType == 2) {
//                                // BT02-E104芯片
//                                for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
//                                    // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
//                                    System.out.println("BT02-E104芯片");
//                                    String uid = c.getUuid().toString().split("-")[0];
//                                    if ("0000fff1".equals(uid)) {
//                                        //                if ("5833ff02".equals(uid)) {
//                                        characteristicRead = c;
//                                    }
//                                }
//                            }
//
//                            BleManager.getInstance().notify(
//                                    bleDevice,
//                                    characteristicRead.getService().getUuid().toString(),
//                                    characteristicRead.getUuid().toString(),
//                                    new BleNotifyCallback() {
//
//                                        @Override
//                                        public void onNotifySuccess() {
//                                            System.out.println("成功");
//                                        }
//
//                                        @Override
//                                        public void onNotifyFailure(BleException exception) {
//                                            System.out.println("失败");
//                                        }
//
//                                        @Override
//                                        public void onCharacteristicChanged(byte[] data) {
//                                            Date date = new Date();
//                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
//                                            String dateString = formatter.format(date);
//                                            String s1 = HexUtil.byteToString(data);
//                                            System.out.println(dateString + "    " + s1);
//                                        }
//                                    });
//                        }
                    }
                }).show();
            }

            // 连接断开
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                if(getContext() == null) {
                    return;
                }
                if (isActiveDisConnected) {
                    Toast.makeText(getContext(), getString(R.string.active_disconnected), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), getString(R.string.disconnected), Toast.LENGTH_LONG).show();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
                System.out.println("在这里断开了哦");


                // 这里表示当前设备已经取消连接了
                // 修改全局设备的内容
                ((MyApplication)getActivity().getApplication()).setBasicDeviceIsConnect(false);
                ((MyApplication)getActivity().getApplication()).setBasicRealDataIsWorking(false);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceName(null);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceAddress(0);
                ((MyApplication)getActivity().getApplication()).setBasicDeviceType(0);
                ((MyApplication)getActivity().getApplication()).setBasicType(-1);

                ((MyApplication)getActivity().getApplication()).setIntervalTime(-1);
                ((MyApplication)getActivity().getApplication()).setTestTime(-1);
                ((MyApplication)getActivity().getApplication()).setCleaningCycles(-1);
                ((MyApplication)getActivity().getApplication()).setCleaningTime(-1);

//                ((MyApplication)getActivity().getApplication()).getBasicBluetoothGatt().close();
//                ((MyApplication)getActivity().getApplication()).setBasicBluetoothGatt(null);
//                ((MyApplication)getActivity().getApplication()).setBasicCharacteristic(null);
//                ((MyApplication)getActivity().getApplication()).setBasicCharacteristicRead(null);

                connectDeviceList.setVisibility(View.GONE);

            }
        });
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    // 点击开始扫描，从核实权限开始
                    checkPermissions();

                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    // 点击停止扫描，就调用取消扫描函数
                    BleManager.getInstance().cancelScan();
                }
                break;

        }
    }

//    public void getDeviceAdapter(CallBack2 callBack){
//        callBack.getDeviceAdapter(mDeviceAdapter);
//    }
//
//    public void getExample(CallBack callBack){
//        callBack.getResult("example");
//    }
//
//    public interface CallBack{
//        public void getResult(String result);
//    }
//    public interface CallBack2{
//        public void getDeviceAdapter(DeviceAdapter deviceAdapter);
//    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("DeviceConnectFragment调用onPause()方法");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onStop() {
        super.onStop();
        System.out.println("DeviceConnectFragment调用onStop()方法");
        System.out.println(getFragmentManager().getFragments().size());
    }

    public void checkPermissions() {
        // 获取蓝牙适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
            mWaveSwipeRefreshLayout.setRefreshing(false);
            return;
        }
//        boolean grantExternalRW = isGrantExternalRW(this, 2);
//        if(grantExternalRW){
//            return;
//        };

        // 如果打开了蓝牙权限
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};// 访问精确的位置
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            // ContextCompat类的checkSelfPermission方法用于检测用户是否授权了某个权限。
            // 第一个参数需要传入Context,第二个参数需要传入需要检测的权限，返回值0已授权，-1未授权
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(), permission);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {// 许可授予
                //存储权限
                isGrantExternalRW(getActivity(), 2);
                //位置权限
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
//            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);//请求代码权限位置
            ActivityCompat.requestPermissions(getActivity(), deniedPermissions, 2);//请求代码权限位置

        }

    }

    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = activity.checkSelfPermission(Manifest.permission.CAMERA);
            if (storagePermission != PackageManager.PERMISSION_GRANTED ||
                    cameraPermission != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, requestCode);
                return false;
            }
        }
        return true;
    }

    private void readRssi(BleDevice bleDevice) {
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                Log.i(TAG, "onRssiFailure" + exception.toString());
            }

            @Override
            public void onRssiSuccess(int rssi) {
                Log.i(TAG, "onRssiSuccess: " + rssi);
            }
        });
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            //设置MTU失败
            public void onSetMTUFailure(BleException exception) {
                Log.i(TAG, "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i(TAG, "onMtuChanged: " + mtu);
            }
        });
    }

    private void checkOnPer(){
        boolean grantExternalRW = isGrantExternalRW(getActivity(), 2);
        if (grantExternalRW){
            checkPermissions();
        }
    }

    //在许可授予定位
    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION://访问精确的位置
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.notifyTitle)
                            .setMessage(R.string.storage)
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            act.finish();
                                        }
                                    })
                            .setPositiveButton(R.string.setting,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        //ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS应用管理
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);//操作位置源设置
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);//请求代码打开GPS
                                            acc=true;
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                }
                else {
                    setScanRule();
                    startScan();
                }
                break;
        }
    }

    //检查GPS是否打开INPUT_SERVICE
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);//定位服务
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean checkReedIsOpen() {
        LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);//定位服务

        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.KEY_PROXIMITY_ENTERING);
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //请求代码打开GPS
        if (requestCode == REQUEST_CODE_OPEN_GPS) {//请求代码打开GPS
            if (checkGPSIsOpen()) {
                setScanRule();
                startScan();
            }
        }
    }
    @Override
    //根据请求权限结果
    public final void onRequestPermissionsResult(int requestCode,
                                                 @NonNull String[] permissions,
                                                 @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION: //请求代码权限位置
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) { //许可授予
                            onPermissionGranted(permissions[i]);
                        }
                    }
                }
                break;
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
                            XXPermissions.startPermissionActivity(getActivity(), permissions);
                        } else {
                            System.out.println("获取录音和日历权限失败");
                        }
                    }
                });
    }


}
