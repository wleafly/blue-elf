package com.clj.blesample.operation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.utils.HexUtil;
import com.clj.fastble.utils.MyLog;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//特征操作片段
public class CharacteristicOperationFragment extends Fragment {

    public static final int PROPERTY_READ = 1;
    public static final int PROPERTY_WRITE = 2;
    public static final int PROPERTY_WRITE_NO_RESPONSE = 3;
    public static final int PROPERTY_NOTIFY = 4;
    public static final int PROPERTY_INDICATE = 5;

    // 配置一个属性，用于点击后，读的时候不会直接保存到手机上
    public static final int PROPERTY_READ_ONLY = 6;

    public boolean isStart = false;
    public static final int isTime = 0;


    private LinearLayout layout_container;//布局容器
    private List<String> childList = new ArrayList<>();

    @Override
    //在创建视图
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_operation, null);//片段特性操作
        initView(v);

        return v;
    }

    //初始化视图
    private void initView(View v) {
        layout_container = (LinearLayout) v.findViewById(R.id.layout_container);
    }

    /**
     * 显示数据
     */
    public void showData() {
        System.out.println("showData函数");
        // 获得这个设备的信息
        final BleDevice bleDevice = ((OperationActivity) getActivity()).getBleDevice();
        // 获得这个设备的特性
        final BluetoothGattCharacteristic characteristicRead = ((OperationActivity) getActivity()).getCharacteristicRead();
        final BluetoothGattCharacteristic characteristicRead3 = ((OperationActivity) getActivity()).getCharacteristicRead3();
        final BluetoothGattCharacteristic characteristic = ((OperationActivity) getActivity()).getCharacteristic();
        final int position = ((OperationActivity) getActivity()).getCharacteristicSign();

//        BleManager.getInstance().stopNotify(bleDevice,
//                characteristicRead.getService().getUuid().toString(),
//                characteristicRead.getUuid().toString());
//        BleManager.getInstance().stopNotify(bleDevice,
//                characteristic.getService().getUuid().toString(),
//                characteristic.getUuid().toString());
        System.out.println("测试特征的position的值：" + position);

        // charaProp是特性的标志：1读、2写、打开通知
        final int charaProp = ((OperationActivity) getActivity()).getCharaProp();
        System.out.println("测试特征charaProp的值：" + charaProp);

        final List<String> ab = new ArrayList<>();
        final List<String> ac = new ArrayList<>();

        // child：特性的UUID值+charaProp
        String child = characteristic.getUuid().toString() + String.valueOf(charaProp);

        // 遍历layout_container,每个都设置为隐藏
        for (int i = 0; i < layout_container.getChildCount(); i++) {
            layout_container.getChildAt(i).setVisibility(View.GONE);
        }
        if (false) {
            // childList.contains(child)
            System.out.println("数据childList包含child");
            layout_container.findViewWithTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp).setVisibility(View.VISIBLE);
        } else {
            System.out.println("数据childList不包含child");
            childList.add(child);

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation, null);
            view.setTag(bleDevice.getKey() + characteristic.getUuid().toString() + charaProp);
            final LinearLayout layout_add = (LinearLayout) view.findViewById(R.id.layout_add);
//            final TextView txt_title = (TextView) view.findViewById(R.id.txt_title);

            // layout_characteric_operation.xml
            // 显示*****数据变化：
            // txt_title.setText(String.valueOf(characteristic.getUuid().toString() + getActivity().getString(R.string.data_changed)));
            final TextView txt = (TextView) view.findViewById(R.id.txt);
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());


            // 点击特性按钮，进入对应特性的操作控制台
            switch (charaProp) {
                case PROPERTY_READ: {
                        System.out.println("PROPERTY_READ!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        // LayoutInflater是对当前Activity布局的扩充
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                        Button btn = (Button) view_add.findViewById(R.id.btn);
                        btn.setText(getActivity().getString(R.string.read));

                        View view_add2 = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button2, null);
                        Button btn2 = (Button) view_add2.findViewById(R.id.btn2);
                        btn2.setText(getActivity().getString(R.string.read_only));
                        btn2.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // Toast.makeText(getActivity(),"点击的是按钮2",Toast.LENGTH_LONG).show();
                                System.out.println("点击按钮2");
                                System.out.println("service的uuid:" + characteristic.getService().getUuid().toString());
                                System.out.println("characteristic的uuid:" + characteristic.getUuid().toString());
                                BleManager.getInstance().read(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleReadCallback() {
                                            //Ble读回调
                                            @Override
                                            public void onReadSuccess(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
                                                        String s1 = "时间:" + dateString + ",数值:" + s;
                                                        ac.add(s1);
                                                        Log.e("test", ac.toString());
                                                        addText(txt, s1);

                                                        // 将数据写到对应sd卡下的蓝牙文件夹下
                                                        // MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onReadFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }
                                        });
                            }
                        });

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("点击按钮1");
                                BleManager.getInstance().read(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleReadCallback() {
                                            //Ble读回调
                                            @Override
                                            public void onReadSuccess(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
                                                        String s1 = "时间:" + dateString + ",数值:" + s;
                                                        ac.add(s1);
                                                        Log.e("test", ac.toString());
                                                        addText(txt, s1);

                                                        // 将数据写到对应sd卡下的蓝牙文件夹下
                                                        MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                    }

                                                });
                                            }

                                            @Override
                                            public void onReadFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
                        // 把这个控件加到父页面上去
                        layout_add.addView(view_add);
                        layout_add.addView(view_add2);
                    }
                break;

                // 输入HEX格式指令
                case PROPERTY_WRITE: {
                    System.out.println("PROPERTY_WRITE!!!!!!!!!!!!!!!!!!!!!!");
                    System.out.println("进入写入模块");
                    // 单次读取
                    if(position == 0) {
                        isStart = false;
                        System.out.println("isStart的值：" + isStart);
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                        final Button btn = (Button) view_add.findViewById(R.id.btn);
                        btn.setText(getActivity().getString(R.string.read));

                        View view_add2 = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button2, null);
                        final Button btn2 = (Button) view_add2.findViewById(R.id.btn2);
                        btn2.setText(getActivity().getString(R.string.read_only));

                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                HexUtil.hexStringToBytes("fa"),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                 addText(txt, "write success, current: " + current
//                                                        + " total: " + total
//                                                       + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                // 写入成功，开始读出数据
                                                // 读出数据(通知)
                                                btn2.setOnClickListener(new View.OnClickListener() {

                                                    @Override
                                                    public void onClick(View view) {
                                                        // Toast.makeText(getActivity(),"点击的是按钮2",Toast.LENGTH_LONG).show();
                                                        System.out.println("点击按钮2");
                                                        System.out.println("service的uuid:" + characteristic.getService().getUuid().toString());
                                                        System.out.println("characteristic的uuid:" + characteristic.getUuid().toString());
                                                        int time = 0;
                                                        final Drawable drawable = btn2.getBackground();
                                                        btn2.setBackgroundColor(0xcce2e2e2);
                                                        btn2.setEnabled(false);
                                                        System.out.println("内部isStart的值是：" + isStart);
                                                        if (!isStart) {
                                                            System.out.println("进入非函数");
                                                            btn.setBackgroundColor(0xcce2e2e2);
                                                            btn.setEnabled(false);
                                                            BleManager.getInstance().read(
                                                                    bleDevice,
                                                                    characteristicRead.getService().getUuid().toString(),
                                                                    characteristicRead.getUuid().toString(),
                                                                    new BleReadCallback() {
                                                                        //Ble读回调
                                                                        @Override
                                                                        public void onReadSuccess(final byte[] data) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if (!isStart) {
                                                                                        new Handler().postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                isStart = true;
                                                                                            }
                                                                                        }, isTime);
                                                                                    }
                                                                                    Date date = new Date();
                                                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                    String dateString = formatter.format(date);
                                                                                    String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                    String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                    ac.add(s1);
                                                                                    Log.e("test", ac.toString());
                                                                                    // addText(txt, s1);
                                                                                    addText(txt, "数据读取成功！");
                                                                                    // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                    // MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                                                }

                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onReadFailure(final BleException exception) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    addText(txt, exception.toString());
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                            isStart = true;
                                                            time = 3000;
                                                        }
                                                        if(isStart) {
                                                            System.out.println("进入主函数1");
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    System.out.println("进入主函数2");
                                                                    BleManager.getInstance().read(
                                                                            bleDevice,
                                                                            characteristicRead.getService().getUuid().toString(),
                                                                            characteristicRead.getUuid().toString(),
                                                                            new BleReadCallback() {
                                                                                //Ble读回调
                                                                                @Override
                                                                                public void onReadSuccess(final byte[] data) {
                                                                                    runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {

                                                                                            Date date = new Date();
                                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                            String dateString = formatter.format(date);
                                                                                            String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                            String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                            ac.add(s1);
                                                                                            Log.e("test", ac.toString());
                                                                                            addText(txt, s1);

                                                                                            btn2.setBackgroundDrawable(drawable);
                                                                                            btn2.setEnabled(true);
                                                                                            btn.setBackgroundDrawable(drawable);
                                                                                            btn.setEnabled(true);
                                                                                            // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                            // MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                                                        }

                                                                                    });
                                                                                }

                                                                                @Override
                                                                                public void onReadFailure(final BleException exception) {
                                                                                    runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            addText(txt, exception.toString());
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                }
                                                            },time);
                                                            time = 0;
                                                        }

                                                    }
                                                });

                                                btn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        System.out.println("点击按钮1");
                                                        int time = 0;
                                                        final Drawable drawable = btn.getBackground();
                                                        btn.setBackgroundColor(0xcce2e2e2);
                                                        btn.setEnabled(false);
                                                        if(!isStart) {
                                                            System.out.println("进入非函数");
                                                            btn2.setBackgroundColor(0xcce2e2e2);
                                                            btn2.setEnabled(false);
                                                            BleManager.getInstance().read(
                                                                    bleDevice,
                                                                    characteristicRead.getService().getUuid().toString(),
                                                                    characteristicRead.getUuid().toString(),
                                                                    new BleReadCallback() {
                                                                        //Ble读回调
                                                                        @Override
                                                                        public void onReadSuccess(final byte[] data) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if(!isStart) {
                                                                                        new Handler().postDelayed(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                isStart = true;
                                                                                            }
                                                                                        },isTime);
                                                                                    }
                                                                                    Date date = new Date();
                                                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                    String dateString = formatter.format(date);
                                                                                    String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                    String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                    ac.add(s1);
                                                                                    Log.e("test", ac.toString());
                                                                                    // addText(txt, s1);
                                                                                    addText(txt, "数据读取成功！");
                                                                                    // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                    // MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                                                }

                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onReadFailure(final BleException exception) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    addText(txt, exception.toString());
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                            isStart = true;
                                                            time = 3000;
                                                        }
                                                        if(isStart) {
                                                            System.out.println("进入主函数");
                                                            new Handler().postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    BleManager.getInstance().read(
                                                                            bleDevice,
                                                                            characteristicRead.getService().getUuid().toString(),
                                                                            characteristicRead.getUuid().toString(),
                                                                            new BleReadCallback() {
                                                                                //Ble读回调
                                                                                @Override
                                                                                public void onReadSuccess(final byte[] data) {
                                                                                    runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
//                                                                                            if(!isStart) {
//                                                                                                new Handler().postDelayed(new Runnable() {
//                                                                                                    @Override
//                                                                                                    public void run() {
//                                                                                                        isStart = true;
//                                                                                                    }
//                                                                                                },isTime);
//                                                                                            }
                                                                                            Date date = new Date();
                                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                            String dateString = formatter.format(date);
                                                                                            String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                            String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                            ac.add(s1);
                                                                                            Log.e("test", ac.toString());
                                                                                            addText(txt, s1);
                                                                                            btn.setEnabled(true);
                                                                                            btn.setBackgroundDrawable(drawable);
                                                                                            btn2.setEnabled(true);
                                                                                            btn2.setBackgroundDrawable(drawable);
                                                                                            // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                            MyLog.writeLogToReadFile(s1);
//                                                    addText(txt, HexUtil.formatHexString(data, true));
                                                                                        }

                                                                                    });
                                                                                }

                                                                                @Override
                                                                                public void onReadFailure(final BleException exception) {
                                                                                    runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            addText(txt, exception.toString());
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                }
                                                            },time);
                                                            time = 0;
                                                        }
                                                    }
                                                });


                                            }
                                        });
                                    }

                                    @Override
                                    public void onWriteFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                addText(txt, exception.toString());
                                            }
                                        });
                                    }
                                });


                        // 把这个控件加到父页面上去
                        // 读存
                        layout_add.addView(view_add);
                        // 只读
                        layout_add.addView(view_add2);



                    }
                    // 实时读取
                    else if (position == 1) {
                        System.out.println("进入fa操作测试，指令position3");
                        // 首先写入指令
                        isStart = false;
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et2, null);
                        final TextView et1 = (TextView) view_add.findViewById(R.id.text1);
                        et1.setText("FA实时读取指令：");
                        final TextView input = (TextView) view_add.findViewById(R.id.input);
                        final TextView et2 = (TextView) view_add.findViewById(R.id.text2);
//                        final TextView output = (TextView) view_add.findViewById(R.id.output);
//                        output.setMovementMethod(ScrollingMovementMethod.getInstance());
                        String hex = "fa";
                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                HexUtil.hexStringToBytes(hex),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {


                                                    addText(input, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                    // 读出数据(通知)
//                                                try {
//                                                    // 做个线程延迟
//                                                    Thread.sleep(1000);
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }
                                                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                                progressDialog.setIcon(R.mipmap.ic_launcher);
                                                progressDialog.setTitle("实时数据获取");
                                                progressDialog.setMessage("获取中...");
                                                progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                                                progressDialog.setCancelable(false);//点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                                                progressDialog.show();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        progressDialog.hide();
                                                    }
                                                },1500);
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Boolean isValue = false;
                                                        BleManager.getInstance().notify(
                                                                bleDevice,
                                                                characteristicRead.getService().getUuid().toString(),
                                                                characteristicRead.getUuid().toString(),
                                                                new BleNotifyCallback() {

                                                                    @Override
                                                                    public void onNotifySuccess() {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                addText(txt, "notify success通知成功");
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onNotifyFailure(final BleException exception) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                addText(txt, exception.toString());
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onCharacteristicChanged(final byte[] data) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
//                                                                                if(!isStart) {
//                                                                                    isStart = true;
//                                                                                }else {
                                                                                new Handler().postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Date date = new Date();
                                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                        String dateString = formatter.format(date);
                                                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                                        MyLog.writeLogToNotifyFile(s2);
                                                                                        ab.add(s2);
                                                                                        Log.e("test", ab.toString());
                                                                                        String str = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                        if(str.split(",").length > 2) {
                                                                                            txt.setText("");
                                                                                            addText(txt,"notify success通知成功");
                                                                                            addText(txt, "时间：" + dateString + "，数值：" + str);
                                                                                        }else{
                                                                                            addText(txt, "时间：" + dateString + "，数值：" + str);
                                                                                        }


//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);
                                                                                    }
                                                                                },100);

//                                                                                }

                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                    }
                                                },1500);

                                            }
                                        });
                                    }

                                    @Override
                                    public void onWriteFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addText(input, exception.toString());
                                            }
                                        });
                                    }
                                });
                        layout_add.addView(view_add);
                    }
                    // 历史数据
                    else if (position == 2){
                        isStart = false;
                        System.out.println("进入fb操作测试，指令position=" + position);
                        // 首先写入指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et2, null);
                        final TextView et1 = (TextView) view_add.findViewById(R.id.text1);
                        et1.setText("FB历史数据读取指令：");
                        final TextView input = (TextView) view_add.findViewById(R.id.input);
                        final TextView et2 = (TextView) view_add.findViewById(R.id.text2);
//                        final TextView output = (TextView) view_add.findViewById(R.id.output);
//                        output.setMovementMethod(ScrollingMovementMethod.getInstance());
                        String hex = "fb";
                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                HexUtil.hexStringToBytes(hex),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addText(input, "write success, current: " + current
                                                        + " total: " + total
                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                // 读出数据(通知)

//                                                try {
//                                                    // 做个线程延迟
//                                                    Thread.sleep(100);
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }

                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        BleManager.getInstance().notify(
                                                                bleDevice,
                                                                characteristicRead.getService().getUuid().toString(),
                                                                characteristicRead.getUuid().toString(),
                                                                new BleNotifyCallback() {

                                                                    @Override
                                                                    public void onNotifySuccess() {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                addText(txt, "notify success通知成功");

                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onNotifyFailure(final BleException exception) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                addText(txt, exception.toString());
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onCharacteristicChanged(final byte[] data) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
//                                                                        if(false) {
//                                                                            isStart = true;
//                                                                        }else {
                                                                                new Handler().postDelayed(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        System.out.println("历史数据函数");
                                                                                        Date date = new Date();
                                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                        String dateString = formatter.format(date);
                                                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                                        // 存储到文件中去
                                                                                        MyLog.writeLogToNotifyFile(s2);
                                                                                        ab.add(s2);
                                                                                        Log.e("test", ab.toString());
                                                                                        addText(txt, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);
                                                                                    }
                                                                                },1000);

//                                                                        }
                                                                            }
                                                                        });
                                                                    }
                                                                });


                                                    }
                                                },0);



                                            }
                                        });
                                    }

                                    @Override
                                    public void onWriteFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addText(input, exception.toString());
                                            }
                                        });
                                    }
                                });
                        layout_add.addView(view_add);
                    }
                    // 时间修改
                    else if(position == 3){
                        System.out.println("进入fc操作测试，指令position=" + position);
                        // 首先写入指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et3, null);
                        final TextView text_p1_left = (TextView) view_add.findViewById(R.id.text_p1_left);
                        final TextView text_p1_right = (TextView) view_add.findViewById(R.id.text_p1_right);
                        final TextView text_p2_left = (TextView) view_add.findViewById(R.id.text_p2_left);
                        final TextView text_p2_right = (TextView) view_add.findViewById(R.id.text_p2_right);
                        final TextView et1 = (TextView) view_add.findViewById(R.id.et_p1);
                        final TextView input = (TextView) view_add.findViewById(R.id.input);
                        final TextView et2 = (TextView) view_add.findViewById(R.id.et_p2);
                        final TextView output = (TextView) view_add.findViewById(R.id.output);
                        final TextView text1 = (TextView) view_add.findViewById(R.id.text1);
                        Button btn = (Button) view_add.findViewById(R.id.btn);

                        text_p1_left.setText("间隔时间：");
                        text_p1_right.setText("分钟");
                        text_p2_left.setText("测试时间：");
                        text_p2_right.setText("分钟");
                        et1.setHint("在此输入间隔时间");
                        et2.setHint("在此输入测试时间");
                        text1.setText("");
                        // output.setMovementMethod(ScrollingMovementMethod.getInstance());
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String str1 = et1.getText().toString();
                                String str2 = et2.getText().toString();
                                if(TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
                                    new AlertDialog.Builder(view.getContext()).setMessage("输入的信息不能为空！").setPositiveButton("重新输入",null).show();
                                    return;
                                }
                                if(Integer.parseInt(str1) < Integer.parseInt(str2)) {
                                    new AlertDialog.Builder(view.getContext()).setMessage("测试时间应该要小于间隔时间！").setPositiveButton("重新输入",null).show();
                                    return;
                                }
                                String strHex1 = "";
                                String strHex2 = "";
                                if((Integer.parseInt(str1) > 255) || (Integer.parseInt(str2) > 255) || (Integer.parseInt(str1) < 0) || (Integer.parseInt(str2) < 0)) {
                                    new AlertDialog.Builder(view.getContext()).setMessage("数据超过范围，请重新输入！").setPositiveButton("重新输入",null).show();
                                    return;
                                }
                                if(Integer.parseInt(str1) < 16) {
                                    strHex1 = strHex1 + "0";
                                }
                                if(Integer.parseInt(str2) < 16) {
                                    strHex2 = strHex2 + "0";
                                }
                                strHex1 = strHex1 + Integer.toHexString(Integer.parseInt(str1));
                                strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                                System.out.println(strHex1 + ";;;;;" + strHex2);
                                String hex ="fc" + strHex1 + strHex2;
                                System.out.println("hex的值是：" + hex);
                                System.out.println("转化之后的长度：" + HexUtil.hexStringToBytes(hex).length);
                                System.out.println("转化之后的值是：" + HexUtil.hexStringToBytes(hex));
                                for(Byte b : HexUtil.hexStringToBytes(hex)){
                                    System.out.println(b);
                                }
                                BleManager.getInstance().write(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        // 调用HexUtil进行格式转化
                                        HexUtil.hexStringToBytes(hex),
                                        new BleWriteCallback() {

                                            @Override
                                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        text1.setText("指令输入结果：");
                                                        addText(input, "write success, current: " + current
                                                                + " total: " + total
                                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                        // 读取对应的数据
                                                        new AlertDialog.Builder(getActivity()).setMessage("指令修改成功！").setPositiveButton("确认",null).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onWriteFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(input, exception.toString());
                                                        new AlertDialog.Builder(getActivity()).setMessage("指令修改失败！").setPositiveButton("确认",null).show();
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
                        layout_add.addView(view_add);
                    }
                    // 清除数据
                    else if(position == 4) {
                        System.out.println("进入fd操作测试，指令position=" + position);
                        // 首先写入指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et4, null);
                        Button btn = (Button) view_add.findViewById(R.id.btn);
                        btn.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               new AlertDialog.Builder(layout_add.getContext()).setMessage("确认删除历史数据吗？").setNegativeButton("确认删除",
                                       new DialogInterface.OnClickListener(){

                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                                       characteristic.getUuid().toString(),
                                                       HexUtil.hexStringToBytes("fd"),
                                                       new BleWriteCallback() {
                                                           @Override
                                                           public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                                               runOnUiThread(new Runnable() {
                                                                   @Override
                                                                   public void run() {
//                                                addText(input, "write success, current: " + current
//                                                        + " total: " + total
//                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                                       // 通知用户数据清除完成
                                                                       new AlertDialog.Builder(layout_add.getContext()).setMessage("数据删除成功").setPositiveButton("确认",null).show();

                                                                   }
                                                               });
                                                           }

                                                           @Override
                                                           public void onWriteFailure(final BleException exception) {
                                                               runOnUiThread(new Runnable() {
                                                                   @Override
                                                                   public void run() {
//                                                addText(input, exception.toString());
                                                                       new AlertDialog.Builder(layout_add.getContext()).setMessage("数据删除失败").setPositiveButton("确认",null).show();
                                                                   }
                                                               });
                                                           }
                                                       });


                                           }
                                       }).setPositiveButton("取消",null).show();



                           }
                        });
                        layout_add.addView(view_add);
                    }
                    // 校准指令
                    else if(position == 5) {
                        System.out.println("进入fe操作测试，指令position=" + position);
                        // 首先写入指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et3, null);
                        final TextView et1 = (TextView) view_add.findViewById(R.id.et_p1);
                        final TextView input = (TextView) view_add.findViewById(R.id.input);
                        final TextView et2 = (TextView) view_add.findViewById(R.id.et_p2);
                        final TextView output = (TextView) view_add.findViewById(R.id.output);
                        final TextView text_p1_left = (TextView) view_add.findViewById(R.id.text_p1_left);
                        final TextView text_p1_right = (TextView) view_add.findViewById(R.id.text_p1_right);
                        final TextView text_p2_left = (TextView) view_add.findViewById(R.id.text_p2_left);
                        final TextView text_p2_right = (TextView) view_add.findViewById(R.id.text_p2_right);
                        final TextView text1 = (TextView) view_add.findViewById(R.id.text1);
                        Button btn = (Button) view_add.findViewById(R.id.btn);
                        text_p1_left.setText("校准类型：");
                        text_p1_right.setText("");
                        text_p2_left.setText("校准数据：");
                        text_p2_right.setText("");
                        text1.setText("");
                        et1.setInputType(InputType.TYPE_NULL);
                        et1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("选择校准类型");
                                final String[] correct = {"零点校准","斜率校准"};
                                builder.setItems(correct, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        et1.setText(correct[which]);
                                    }
                                });
                                builder.show();
                            }
                        });
//                        output.setMovementMethod(ScrollingMovementMethod.getInstance());
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                InputMethodManager inputMethodManager = (InputMethodManager) ((OperationActivity)getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                                String str1 = "";
                                System.out.println("输入框的校准类型是：" + et1.getText());
                                if(et1.getText().toString().equals("零点校准")) {
                                    str1 = "0000";
                                }else if(et1.getText().toString().equals("斜率校准")) {
                                    str1 = "0001";
                                }
                                System.out.println("str1的值是：" + str1);
                                String strHex2 = "";
                                String str2 = et2.getText().toString();
                                if(TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2)) {
                                    new AlertDialog.Builder(view.getContext()).setMessage("输入的信息不能为空！请重新输入").setPositiveButton("确定",null).show();
                                    return;
                                }
                                if((Integer.parseInt(str2) < 0) || (Integer.parseInt(str2) > 65535)) {
                                    new AlertDialog.Builder(view.getContext()).setMessage("数据超出范围！请重新输入").setPositiveButton("确定",null).show();
                                    return;
                                }
                                if(Integer.parseInt(str2) < 4096) {
                                    strHex2 = strHex2 + "0";
                                }
                                if(Integer.parseInt(str2) < 256) {
                                    strHex2 = strHex2 + "0";
                                }
                                if(Integer.parseInt(str2) < 16) {
                                    strHex2 = strHex2 + "0";
                                }
                                strHex2 = strHex2 + Integer.toHexString(Integer.parseInt(str2));
                                System.out.println("strHex2的值是：" + strHex2);
                                String hex ="fe" + str1 + strHex2;
                                System.out.println("hex的值是：" + hex);
                                System.out.print("strHex2的最终值是：" );
                                for(byte b : HexUtil.hexStringToBytes(hex)){
                                    System.out.println(b);
                                }
                                BleManager.getInstance().write(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        // 调用HexUtil进行格式转化
                                        HexUtil.hexStringToBytes(hex),
                                        new BleWriteCallback() {

                                            @Override
                                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        text1.setText("指令输入结果：");
                                                        addText(input, "write success, current: " + current
                                                                + " total: " + total
                                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                        // 读取对应的数据
                                                        new AlertDialog.Builder(getActivity()).setMessage("指令修改成功！").setPositiveButton("确认",null).show();
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                BleManager.getInstance().notify(
                                                                        bleDevice,
                                                                        characteristicRead.getService().getUuid().toString(),
                                                                        characteristicRead.getUuid().toString(),
                                                                        new BleNotifyCallback() {

                                                                            @Override
                                                                            public void onNotifySuccess() {
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        addText(input, "notify success通知成功");
//                                                                                    addText(txt,"正在读取数据......");

                                                                                    }
                                                                                });
                                                                            }

                                                                            @Override
                                                                            public void onNotifyFailure(final BleException exception) {
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        addText(input, exception.toString());
                                                                                    }
                                                                                });
                                                                            }

                                                                            @Override
                                                                            public void onCharacteristicChanged(final byte[] data) {
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {

                                                                                        System.out.println("历史数据函数");
                                                                                        Date date = new Date();
                                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                        String dateString = formatter.format(date);
                                                                                        String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                                        // 存储到文件中去
//                                                                                MyLog.writeLogToNotifyFile(s2);
                                                                                        ab.add(s2);
                                                                                        Log.e("test", ab.toString());
                                                                                        addText(input, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));

//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);


//                                                                                    }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                            }
                                                        },2000);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onWriteFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(input, exception.toString());
                                                        new AlertDialog.Builder(getActivity()).setMessage("指令修改失败！").setPositiveButton("确认",null).show();
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
                        layout_add.addView(view_add);
                    }
                    // 地址读取
                    else if(position == 6) {

                        System.out.println("进入f9操作测试，指令position=" + position);
                        // 首先写入指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et2, null);
                        final TextView et1 = (TextView) view_add.findViewById(R.id.text1);
                        et1.setText("F9读取地址指令：");
                        final TextView input = (TextView) view_add.findViewById(R.id.input);
                        final TextView et2 = (TextView) view_add.findViewById(R.id.text2);
//                        final TextView output = (TextView) view_add.findViewById(R.id.output);
//                        output.setMovementMethod(ScrollingMovementMethod.getInstance());
                        String hex = "f9";
                        System.out.println("开始写入f9指令----------------------------------------------");
                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                HexUtil.hexStringToBytes(hex),
                                new BleWriteCallback() {
                                    @Override
                                    public void onWriteSuccess(final int current,final int total,final byte[] justWrite) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                System.out.println("成功写入f9指令----------------------------------------------");
                                                addText(input, "write success, current: " + current
                                                        + " total: " + total
                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                                progressDialog.setIcon(R.mipmap.ic_launcher);
                                                progressDialog.setTitle("获取设备地址");
                                                progressDialog.setMessage("获取中...");
                                                progressDialog.setIndeterminate(false);// 是否形成一个加载动画  true表示不明确加载进度形成转圈动画  false 表示明确加载进度
                                                progressDialog.setCancelable(false);//点击返回键或者dialog四周是否关闭dialog  true表示可以关闭 false表示不可关闭
                                                progressDialog.show();
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressDialog.hide();
                                                    }
                                                },3000);

                                                if(true) {
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            BleManager.getInstance().notify(
                                                                    bleDevice,
                                                                    characteristicRead.getService().getUuid().toString(),
                                                                    characteristicRead.getUuid().toString(),
                                                                    new BleNotifyCallback() {

                                                                        @Override
                                                                        public void onNotifySuccess() {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
//                                                                                    addText(txt, "notify success通知成功");
//                                                                                    addText(txt,"正在读取数据......");

                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onNotifyFailure(final BleException exception) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    addText(txt, exception.toString());
                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onCharacteristicChanged(final byte[] data) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
//                                                                                    if(false) {
//                                                                                        isStart = true;
//                                                                                    }else {


                                                                                            System.out.println("历史数据函数");
                                                                                            Date date = new Date();
                                                                                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                            String dateString = formatter.format(date);
                                                                                            String s1 = HexUtil.formatHexString(characteristicRead.getValue());
                                                                                            String s2 = "时间:" + dateString + ",数值:" + s1;
                                                                                            // 存储到文件中去
                                                                                            MyLog.writeLogToNotifyFile(s2);
                                                                                            ab.add(s2);
                                                                                            Log.e("test", ab.toString());
                                                                                            if(HexUtil.formatHexString(characteristicRead.getValue(), true).charAt(HexUtil.formatHexString(characteristicRead.getValue(), true).length() - 1) != ',') {
                                                                                                addText(txt, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
                                                                                            }
//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);


//                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                            System.out.println("现在关闭中...");


                                                        }
                                                    },1000);


                                                }else {
                                                    // 读出数据(通知)
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            BleManager.getInstance().read(
                                                                    bleDevice,
                                                                    characteristicRead.getService().getUuid().toString(),
                                                                    characteristicRead.getUuid().toString(),
                                                                    new BleReadCallback() {
                                                                        //Ble读回调
                                                                        @Override
                                                                        public void onReadSuccess(final byte[] data) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    Date date = new Date();
                                                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                    String dateString = formatter.format(date);
                                                                                    String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                                    String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                    ac.add(s1);
                                                                                    Log.e("test", ac.toString());
                                                                                    addText(txt, s1);

                                                                                    // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                    // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                                                }

                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void onReadFailure(final BleException exception) {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    addText(txt, exception.toString());
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                        }
                                                    }, 3000);


                                                }

                                            }
                                        });

                                    }

                                    @Override
                                    public void onWriteFailure(final BleException exception) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addText(input, exception.toString());
                                            }
                                        });
                                    }
                                });
                        layout_add.addView(view_add);
                    }
                    // 设备名称修改
                    else if(position == 7) {
                        System.out.println("进入设备名称修改操作页面");
                        // 首先写入密码认证指令
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et3, null);
                        final TextView header = (TextView) view_add.findViewById(R.id.text_header);
                        final TextView p1_l = (TextView) view_add.findViewById(R.id.text_p1_left);
                        final TextView p1 = (TextView) view_add.findViewById(R.id.et_p1);
                        final TextView p1_r = (TextView) view_add.findViewById(R.id.text_p1_right);
                        final TextView p2_l = (TextView) view_add.findViewById(R.id.text_p2_left);
                        final TextView p2 = (TextView) view_add.findViewById(R.id.et_p2);
                        final TextView p2_r = (TextView) view_add.findViewById(R.id.text_p2_right);
                        final TextView btn = (TextView) view_add.findViewById(R.id.btn);
                        final TextView output = (TextView) view_add.findViewById(R.id.output);
                        p1_l.setText("新的设备名称：");
                        p1_r.setVisibility(View.GONE);
                        p2_l.setVisibility(View.GONE);
                        p2.setVisibility(View.GONE);
                        p2_r.setVisibility(View.GONE);
                        p1.setKeyListener(DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"));
                        btn.setText("确认修改");
                        final String confirmPwd = "<PWD123456>";
                        final String getNameheader = "<NAME";
                        final String getNameBottom = ">";

                        byte[] b = confirmPwd.getBytes();
                        for(byte r : b) {
                            System.out.println(r);
                        }

                        System.out.println(HexUtil.hexStringToBytes(confirmPwd));
                        for(byte r : HexUtil.hexStringToBytes(confirmPwd)) {
                            System.out.println(r);
                        }
                        System.out.println(HexUtil.hexStringToBytes("12345"));
                        for(byte r : HexUtil.hexStringToBytes("12345")) {
                            System.out.println(r);
                        }
                        BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                characteristic.getUuid().toString(),
                                b,
                                new BleWriteCallback() {

                                    @Override
                                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                        addText(output, "write success, current: " + current
                                                + " total: " + total
                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                        // 写入成功获取数据
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                BleManager.getInstance().read(
                                                        bleDevice,
                                                        characteristicRead.getService().getUuid().toString(),
                                                        characteristicRead.getUuid().toString(),
                                                        new BleReadCallback() {
                                                            //Ble读回调
                                                            @Override
                                                            public void onReadSuccess(final byte[] data) {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Date date = new Date();
                                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                        String dateString = formatter.format(date);
                                                                        String s = HexUtil.formatHexString(characteristicRead.getValue(), true);
                                                                        String s1 = "时间:" + dateString + ",数值:" + s;
                                                                        ac.add(s1);
                                                                        Log.e("test", ac.toString());
                                                                        addText(txt, s1);

                                                                        // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                        // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                                    }

                                                                });
                                                            }

                                                            @Override
                                                            public void onReadFailure(final BleException exception) {
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        addText(txt, exception.toString());
                                                                    }
                                                                });
                                                            }
                                                        });
                                            }
                                        },1000);

//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
//                                                        characteristic.getUuid().toString(),
//                                                        n,
//                                                        new BleWriteCallback() {
//
//                                                            @Override
//                                                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                                                addText(output, "write success, current: " + current
//                                                                        + " total: " + total
//                                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
//                                                                // 获取设备名称
//                                                                new Handler().postDelayed(new Runnable() {
//                                                                    @Override
//                                                                    public void run() {
//                                                                        BleManager.getInstance().notify(
//                                                                                bleDevice,
//                                                                                characteristicRead.getService().getUuid().toString(),
//                                                                                characteristicRead.getUuid().toString(),
//                                                                                new BleNotifyCallback() {
//
//                                                                                    @Override
//                                                                                    public void onNotifySuccess() {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
//                                                                                    addText(output, "notify success通知成功");
////                                                                                    addText(txt,"正在读取数据......");
//
//                                                                                            }
//                                                                                        });
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onNotifyFailure(final BleException exception) {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
//                                                                                                addText(output, exception.toString());
//                                                                                            }
//                                                                                        });
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onCharacteristicChanged(final byte[] data) {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
////                                                                                    if(false) {
////                                                                                        isStart = true;
////                                                                                    }else {
//
//
//                                                                                                System.out.println("历史数据函数");
//                                                                                                Date date = new Date();
//                                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                                                                String dateString = formatter.format(date);
//                                                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
//                                                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
//                                                                                                // 存储到文件中去
////                                                                                                MyLog.writeLogToNotifyFile(s2);
//                                                                                                ab.add(s2);
//                                                                                                Log.e("test", ab.toString());
//                                                                                                addText(output, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
//
////                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
////                                                        writeTxtToFile(s, filePath, fileName);
//
//
////                                                                                    }
//                                                                                            }
//                                                                                        });
//                                                                                    }
//                                                                                });
//                                                                    }
//                                                                },2000);
//                                                            }
//
//                                                            @Override
//                                                            public void onWriteFailure(BleException exception) {
//
//                                                            }
//                                                        });
//                                            }
//                                        },1000);
                                    }

                                    @Override
                                    public void onWriteFailure(BleException exception) {

                                    }
                                });

                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new String(getNameheader + p1.getText().toString() + getNameBottom).getBytes(),
                                        new BleWriteCallback() {

                                            @Override
                                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                                                addText(output, "write success, current: " + current
                                                        + " total: " + total
                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));

                                                // 写入成功获取数据
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        BleManager.getInstance().read(
                                                                bleDevice,
                                                                characteristic.getService().getUuid().toString(),
                                                                characteristic.getUuid().toString(),
                                                                new BleReadCallback() {
                                                                    //Ble读回调
                                                                    @Override
                                                                    public void onReadSuccess(final byte[] data) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                Date date = new Date();
                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                                                String dateString = formatter.format(date);
                                                                                String s = HexUtil.formatHexString(characteristic.getValue(), true);
                                                                                String s1 = "时间:" + dateString + ",数值:" + s;
                                                                                ac.add(s1);
                                                                                Log.e("test", ac.toString());
                                                                                addText(txt, s1);

                                                                                // 将数据写到对应sd卡下的蓝牙文件夹下
                                                                                // MyLog.writeLogToReadFile(s1);
//                                                                              addText(txt, HexUtil.formatHexString(data, true));
                                                                            }

                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onReadFailure(final BleException exception) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                addText(txt, exception.toString());
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    }
                                                },500);

//                                        new Handler().postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                BleManager.getInstance().write(bleDevice, characteristic.getService().getUuid().toString(),
//                                                        characteristic.getUuid().toString(),
//                                                        n,
//                                                        new BleWriteCallback() {
//
//                                                            @Override
//                                                            public void onWriteSuccess(int current, int total, byte[] justWrite) {
//                                                                addText(output, "write success, current: " + current
//                                                                        + " total: " + total
//                                                                        + " justWrite: " + HexUtil.formatHexString(justWrite, true));
//                                                                // 获取设备名称
//                                                                new Handler().postDelayed(new Runnable() {
//                                                                    @Override
//                                                                    public void run() {
//                                                                        BleManager.getInstance().notify(
//                                                                                bleDevice,
//                                                                                characteristicRead.getService().getUuid().toString(),
//                                                                                characteristicRead.getUuid().toString(),
//                                                                                new BleNotifyCallback() {
//
//                                                                                    @Override
//                                                                                    public void onNotifySuccess() {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
//                                                                                    addText(output, "notify success通知成功");
////                                                                                    addText(txt,"正在读取数据......");
//
//                                                                                            }
//                                                                                        });
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onNotifyFailure(final BleException exception) {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
//                                                                                                addText(output, exception.toString());
//                                                                                            }
//                                                                                        });
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onCharacteristicChanged(final byte[] data) {
//                                                                                        runOnUiThread(new Runnable() {
//                                                                                            @Override
//                                                                                            public void run() {
////                                                                                    if(false) {
////                                                                                        isStart = true;
////                                                                                    }else {
//
//
//                                                                                                System.out.println("历史数据函数");
//                                                                                                Date date = new Date();
//                                                                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                                                                                String dateString = formatter.format(date);
//                                                                                                String s1 = HexUtil.formatHexString(characteristicRead.getValue());
//                                                                                                String s2 = "时间:" + dateString + ",数值:" + s1;
//                                                                                                // 存储到文件中去
////                                                                                                MyLog.writeLogToNotifyFile(s2);
//                                                                                                ab.add(s2);
//                                                                                                Log.e("test", ab.toString());
//                                                                                                addText(output, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristicRead.getValue(), true));
//
////                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
////                                                        writeTxtToFile(s, filePath, fileName);
//
//
////                                                                                    }
//                                                                                            }
//                                                                                        });
//                                                                                    }
//                                                                                });
//                                                                    }
//                                                                },2000);
//                                                            }
//
//                                                            @Override
//                                                            public void onWriteFailure(BleException exception) {
//
//                                                            }
//                                                        });
//                                            }
//                                        },1000);
                                            }

                                            @Override
                                            public void onWriteFailure(BleException exception) {

                                            }
                                        });
                            }
                        });
                        layout_add.addView(view_add);
                    }

                    else {
                        View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);

                        final EditText et = (EditText) view_add.findViewById(R.id.et);
                        Button btn = (Button) view_add.findViewById(R.id.btn);
                        btn.setText(getActivity().getString(R.string.write));
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String hex = et.getText().toString();
                                if (TextUtils.isEmpty(hex)) {
                                    return;
                                }
                                BleManager.getInstance().write(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        // 调用HexUtil进行格式转化
                                        HexUtil.hexStringToBytes(hex),
                                        new BleWriteCallback() {

                                            @Override
                                            public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "write success, current: " + current
                                                                + " total: " + total
                                                                + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onWriteFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }
                                        });
                            }
                        });
                        layout_add.addView(view_add);
                    }
                }
                break;

                case PROPERTY_WRITE_NO_RESPONSE: {//属性写无响应
                    System.out.println("PROPERTY_WRITE_NO_RESPONSE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_et, null);
                    final EditText et = (EditText) view_add.findViewById(R.id.et);
                    Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.write));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String hex = et.getText().toString();
                            if (TextUtils.isEmpty(hex)) {
                                return;
                            }
                            BleManager.getInstance().write(
                                    bleDevice,
                                    characteristic.getService().getUuid().toString(),
                                    characteristic.getUuid().toString(),
                                    HexUtil.hexStringToBytes(hex),
                                    new BleWriteCallback() {

                                        @Override
                                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, "write success, current: " + current
                                                            + " total: " + total
                                                            + " justWrite: " + HexUtil.formatHexString(justWrite, true));
                                                }
                                            });
                                        }

                                        @Override
                                        public void onWriteFailure(final BleException exception) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    addText(txt, exception.toString());
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;

                case PROPERTY_NOTIFY: {//物业通知
                    System.out.println("PROPERTY_NOTIFY!!!!!!!!!!!!!!!!!!!!!!!!");
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.open_notification));

                    View view_add2 = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button2, null);
                    final Button btn2 = (Button) view_add2.findViewById(R.id.btn2);
                    btn2.setText(getActivity().getString(R.string.open_notification_only));


                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 如果检测到 只读 正在运行，手动关闭它
                            if (btn2.getText().toString().equals(getActivity().getString(R.string.close_notification_only))) {
                                System.out.println("11.............................");
                                btn2.setText(getActivity().getString(R.string.open_notification_only));
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }

                            try {
                                // 做个线程延迟
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (btn.getText().toString().equals(getActivity().getString(R.string.open_notification))) {
                                System.out.println("12.............................");
                                btn.setText(getActivity().getString(R.string.close_notification));
                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleNotifyCallback() {

                                            @Override
                                            public void onNotifySuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "notify success通知成功");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
                                                        String s1 = HexUtil.formatHexString(characteristic.getValue());
                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                        MyLog.writeLogToNotifyFile(s2);
                                                        ab.add(s2);
                                                        Log.e("test",ab.toString());
                                                        addText(txt, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristic.getValue(), true));
//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                btn.setText(getActivity().getString(R.string.open_notification));
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });

                    btn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 如果检测到 边读边写 正在运行，手动关闭它
                            if(btn.getText().toString().equals(getActivity().getString(R.string.close_notification))) {
                                System.out.println("21.............................");
                                btn.setText(R.string.open_notification);
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                            try {
                                // 做个线程延迟
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (btn2.getText().toString().equals(getActivity().getString(R.string.open_notification_only))) {
                                System.out.println("22.............................");
                                btn2.setText(getActivity().getString(R.string.close_notification_only));
                                BleManager.getInstance().notify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleNotifyCallback() {

                                            @Override
                                            public void onNotifySuccess() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "notify success通知成功");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onNotifyFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCharacteristicChanged(final byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Date date = new Date();
                                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                                        String dateString = formatter.format(date);
                                                        String s1 = HexUtil.formatHexString(characteristic.getValue());
                                                        String s2 = "时间:" + dateString + ",数值:" + s1;
                                                        // MyLog.writeLogToNotifyFile(s2);
                                                        ab.add(s2);
                                                        Log.e("test",ab.toString());
                                                        addText(txt, "时间：" + dateString + "，数值：" + HexUtil.formatHexString(characteristic.getValue(), true));
//                                                        String s = HexUtil.formatHexString(characteristic.getValue(), true);
//                                                        writeTxtToFile(s, filePath, fileName);
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                btn2.setText(getActivity().getString(R.string.open_notification_only));
                                BleManager.getInstance().stopNotify(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });

                    layout_add.addView(view_add);
                    layout_add.addView(view_add2);
                }
                break;

                case PROPERTY_INDICATE: { // 属性显示
                    System.out.println("PROPERTY_INDICATE!!!!!!!!!!!!!!!!!!!!!!!!");
                    View view_add = LayoutInflater.from(getActivity()).inflate(R.layout.layout_characteric_operation_button, null);//布局特征操作按钮
                    final Button btn = (Button) view_add.findViewById(R.id.btn);
                    btn.setText(getActivity().getString(R.string.open_notification));
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (btn.getText().toString().equals(getActivity().getString(R.string.open_notification))) {
                                btn.setText(getActivity().getString(R.string.close_notification));
                                BleManager.getInstance().indicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString(),
                                        new BleIndicateCallback() {
                                            @Override
                                            public void onIndicateSuccess() {
                                                //
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, "indicate success：执行成功");
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onIndicateFailure(final BleException exception) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, exception.toString());
                                                    }
                                                });
                                            }

                                            @Override
                                            //在特征改变
                                            public void onCharacteristicChanged(byte[] data) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addText(txt, HexUtil.formatHexString(characteristic.getValue(), true));
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                btn.setText(getActivity().getString(R.string.open_notification));
                                BleManager.getInstance().stopIndicate(
                                        bleDevice,
                                        characteristic.getService().getUuid().toString(),
                                        characteristic.getUuid().toString());
                            }
                        }
                    });
                    layout_add.addView(view_add);
                }
                break;
            }
            layout_container.addView(view);
        }

    }

    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

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


    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }

    private void addText(TextView textView, String content) {
        textView.append(content);
        textView.append("\n");
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > textView.getHeight()) {
            textView.scrollTo(0, offset - textView.getHeight());
        }
    }


}

