package com.clj.blesample.operation;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.graphics.Region;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.comm.Observer;
import com.clj.blesample.comm.ObserverManager;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;
//维护
public class OperationActivity extends AppCompatActivity implements Observer {

    public static final String KEY_DATA = "key_data";//主要数据

    private BleDevice bleDevice;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    private Toolbar toolbar;
    private List<Fragment> fragments = new ArrayList<>();
    private int currentPage = 0;
    private String[] titles = new String[3];

    // 设置特性的一个标志位
    private int characteristicSign;
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattCharacteristic characteristicRead3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("你好，MainActivity");

        setContentView(R.layout.activity_operation);
        initData();
        initView();
        initPage();


        ObserverManager.getInstance().addObserver(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().clearCharacterCallback(bleDevice);
        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void disConnected(BleDevice device) {
        if (device != null && bleDevice != null && device.getKey().equals(bleDevice.getKey())) {
            finish();
        }
    }

    //在关键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != 0) {
                currentPage--;
                changePage(currentPage);
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        // 初始化服务列表的页面
        // 获取页面工具栏，并对其进行设置
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(titles[0]);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 设置返回上一页
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage != 0) {
                    currentPage--;

                    changePage(currentPage);
                } else {
                    finish();
                }
            }
        });
    }

    private void initData() {
        bleDevice = getIntent().getParcelableExtra(KEY_DATA);
        if (bleDevice == null)
            finish();

        titles = new String[]{
                getString(R.string.service_list),
                getString(R.string.characteristic_list),
                getString(R.string.console)};//操作台
    }

    private void initPage() {
        prepareFragment();
        changePage(0);
    }

    public void changePage(int page) {
        // page：标题的序号（0:服务列表，1:特征列表，2:操作控制台）
        // 根据传入的page值，分别显示对应页面的数据
        System.out.println("监测此方法是否在运行......page值：" + page);
        System.out.println("fragments的大小：" + fragments.size());

        currentPage = page;
        toolbar.setTitle(titles[page]);
        updateFragment(page);

        if (currentPage == 1) {
            // 调用showData显示特征列表
            ((CharacteristicListFragment) fragments.get(1)).showData();
        } else if (currentPage == 2) {
            // 调用showData显示特征操作控制台列表
            ((CharacteristicOperationFragment) fragments.get(2)).showData();
        }
    }

    private void prepareFragment() {
        // fragments:存储服务list、特征list、操作台list
        fragments.add(new ServiceListFragment());
        fragments.add(new CharacteristicListFragment());
        fragments.add(new CharacteristicOperationFragment());

        // 遍历fragments
        for (Fragment fragment : fragments) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment).hide(fragment).commit();
        }
    }

    // 根据position的值提交事务
    private void updateFragment(int position) {
        // 检测position是否在[0,2]
        if (position > fragments.size() - 1) {
            return;
        }
        //
        for (int i = 0; i < fragments.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragments.get(i);

            // 根据i显示对应的列表
            if (i == position) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }

            // 提交事务
            transaction.commit();
        }
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

    public void setCharacteristicSign(int CharacteristicSign) {
        this.characteristicSign = CharacteristicSign;
    }

    public int getCharacteristicSign(){
        return characteristicSign;
    }

    public void setCharacteristicRead(BluetoothGattCharacteristic characteristicRead){
        this.characteristicRead = characteristicRead;
    }

    public BluetoothGattCharacteristic getCharacteristicRead(){
        return characteristicRead;
    }

    public void setCharacteristicRead3(BluetoothGattCharacteristic characteristicRead){
        this.characteristicRead3 = characteristicRead;
    }

    public BluetoothGattCharacteristic getCharacteristicRead3(){
        return characteristicRead3;
    }

}
