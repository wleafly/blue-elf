package com.clj.blesample.tab;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.activity.AlarmDeviceActivity;
import com.clj.blesample.activity.DeviceAboutActivity;
import com.clj.blesample.activity.DeviceFunctionActivity;
import com.clj.blesample.activity.DeviceNameSetActivity;
import com.clj.blesample.activity.DeviceOrderSet2Activity;
import com.clj.blesample.activity.DeviceOrderSetActivity;
import com.clj.blesample.activity.DeviceTimeSetActivity;
import com.clj.blesample.activity.HistoryDataActivity;
import com.clj.blesample.activity.HistoryDataDeleteActivity;
import com.clj.blesample.activity.LanguageSetActivity;
import com.clj.blesample.activity.SystemSettingActivity;
import com.clj.blesample.activity.TroubleShootingActivity;
import com.clj.blesample.adapter.DeviceAdapter;
import com.clj.blesample.application.MyApplication;

    public class DeviceSettingFragment extends Fragment {

    private DeviceAdapter mDeviceAdapter;
    private DeviceConnectFragment deviceConnectFragment;
    private View view;
    private View v;

    private View setting; // 主页面
    private View deviceName; // 修改设备名称
    private View deviceHistoryData; // 历史数据
    private View deviceHistoryDataDelete; // 清除历史数据
    private View deviceTimeSet; // 时间校准
    private View deviceOrderSet; // 数据校准
    private View alarmDevice; // 报警设备
    private View aboutFunction; // 功能介绍
    private View aboutApp; // 关于软件
    private View deviceOrderSet2; // 数据校准2
    private View languageSet; // 语言选项
    private View systemSetting; // 系统设置
    private View troubleShooting;// 故障排查

    private Boolean isVisible = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_notifications, null);

            // 判断此时有没有连接
            System.out.println("DeviceSettingFragment中的onCreateView()方法");

            v = view;
        }
        setting = v.findViewById(R.id.setting);
        deviceName = v.findViewById(R.id.device_name);
        deviceHistoryData = v.findViewById(R.id.device_history_data);
        deviceHistoryDataDelete = v.findViewById(R.id.device_history_data_delete);
        deviceTimeSet = v.findViewById(R.id.device_time_set);
        deviceOrderSet = v.findViewById(R.id.device_order_set);
        aboutFunction = v.findViewById(R.id.about_function);
        aboutApp = v.findViewById(R.id.about_app);
        alarmDevice = v.findViewById(R.id.alarm_device);
        deviceOrderSet2 = v.findViewById(R.id.device_order_set2);
        troubleShooting = v.findViewById(R.id.trouble_shooting);
        languageSet = v.findViewById(R.id.language_set);
        systemSetting = v.findViewById(R.id.system_setting);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        System.out.println("DeviceSettingFragment调用onResume()方法");

        if(isVisible) {
            deviceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入修改设备名称的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceNameSetActivity.class);
                    startActivity(intent);

                }
            });
            deviceHistoryData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入查看历史数据的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), HistoryDataActivity.class);
                    startActivity(intent);
                }
            });
            deviceHistoryDataDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入删除历史数据的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), HistoryDataDeleteActivity.class);
                    startActivity(intent);
                }
            });
            deviceTimeSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入时间校准的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceTimeSetActivity.class);
                    startActivity(intent);
                }
            });
            deviceOrderSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入指令校准的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceOrderSetActivity.class);
                    startActivity(intent);
                }
            });
            alarmDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入指令校准的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), AlarmDeviceActivity.class);
                    startActivity(intent);
                }
            });
            aboutFunction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入功能描述的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceFunctionActivity.class);
                    startActivity(intent);
                }
            });
            aboutApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入软件描述的activity
//                    if(checkDeviceConnect()) {
//                        return;
//                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceAboutActivity.class);
                    startActivity(intent);
                }
            });
            deviceOrderSet2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入指令校准的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), DeviceOrderSet2Activity.class);
                    startActivity(intent);
                }
            });
            languageSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入语言的activity
//                    if(checkDeviceConnect()) {
//                        return;
//                    }

                    Intent intent = new Intent((MainActivity)getActivity(), LanguageSetActivity.class);
                    startActivity(intent);
                }
            });
            systemSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 进入语言的activity
//                    if(checkDeviceConnect()) {
//                        return;
//                    }

                    Intent intent = new Intent((MainActivity)getActivity(), SystemSettingActivity.class);
                    startActivity(intent);
                }
            });
            troubleShooting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 进入故障排查的activity
                    if(checkDeviceConnect()) {
                        return;
                    }

                    Intent intent = new Intent((MainActivity)getActivity(), TroubleShootingActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("DeviceSettingFragment调用onPause()方法");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("DeviceSettingFragment调用onStop()方法");
        System.out.println(getFragmentManager().getFragments().size());
    }

    public boolean checkDeviceConnect() {
//        if(true){
//            return false;
//        }
        if(!((MyApplication)getActivity().getApplication()).getBasicDeviceIsConnect()) {
            new AlertDialog.Builder(setting.getContext()).setMessage(getString(R.string.please_connect_device)).setPositiveButton(getString(R.string.ensure),null).show();
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
    }



}
