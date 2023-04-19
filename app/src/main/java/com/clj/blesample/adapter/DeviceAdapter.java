package com.clj.blesample.adapter;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.blesample.MainActivity;
import com.clj.blesample.R;
import com.clj.blesample.application.MyApplication;
import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备适配器
 * 存储BleDevice的适配器
 */
public class DeviceAdapter extends BaseAdapter {

    private Context context; // context上下文对象
    private List<BleDevice> bleDeviceList;  // 所有的设备数据

    private View v_setting;

    public DeviceAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        // 如果有设备名，就放在最上方
        if(bleDevice.getName() != null) {
//            bleDeviceList.add(0,bleDevice);
            bleDeviceList.add(bleDevice);
        }else{
//            bleDeviceList.add(bleDevice);
        }
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    // 移除正在连接的这个设备，以便于新的客户连接它
    public void clearConnectedDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    //
    public void clearScanDevice() {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size())
            return null;
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    /**
     *
     * @param position 显示屏中，要显示的一行搜索结果（一个item.xml形成的视图），其在搜索结果中的位置
     * @param convertView item的，每一行的视图
     * @param parent 每个item的视图，被放在了parent中，listeview要显示新出现的一行的视图时，把其取出来，放到parent中
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder对应adapter_device.xml视图中的所有数据，封装
        ViewHolder holder;
        DeviceListCell cell;

        if (convertView != null) {
            // 如果不为空，说明有缓存中的view，直接取出ViewHolder对象
            holder = (ViewHolder) convertView.getTag();
        } else {
            // 如果为空，我们得到控件中的所有对象
            convertView = View.inflate(context, R.layout.adapter_device, null);
            holder = new ViewHolder();

            // 绑定ViewHolder对象
            convertView.setTag(holder);
            holder.img_blue = (ImageView) convertView.findViewById(R.id.img_blue); // 蓝牙图标
            holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name); // 设备名字
            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac); // mac地址
            holder.txt_rssi = (TextView) convertView.findViewById(R.id.txt_rssi); // 信号值
            holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle); // 管理未连接状态下的layout

            holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.layout_connected); // 管理连接状态后的layout
            holder.btn_disconnect = (Button) convertView.findViewById(R.id.btn_disconnect); // 断开连接
            holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect); // 连接（还未连接状态的时候显示）
            holder.btn_detail = (Button) convertView.findViewById(R.id.btn_detail); // 进入操作
            holder.device_layout = convertView.findViewById(R.id.device_layout); //

            holder.device_layout1 = convertView.findViewById(R.id.device_layout1);
            holder.device_layout2 = convertView.findViewById(R.id.device_layout2);
            holder.device_layout3 = convertView.findViewById(R.id.device_layout3);
            holder.txt_name1 = (TextView) convertView.findViewById(R.id.txt_name1);
            holder.txt_name2 = (TextView) convertView.findViewById(R.id.txt_name2);
            holder.txt_name3 = (TextView) convertView.findViewById(R.id.txt_name3);


        }

        convertView = View.inflate(context, R.layout.device_list_up, null);
        cell = new DeviceListCell();

        // 绑定ViewHolder对象
        convertView.setTag(holder);
        cell.device_layout1 = convertView.findViewById(R.id.device_layout1);
        cell.txt_name1 = convertView.findViewById(R.id.txt_name1);
        cell.img_blue1 = convertView.findViewById(R.id.img_blue1);

        // 获取到该位置下的设备信息
        final BleDevice bleDevice = getItem(position);

        // 如果此处设备信息不为空，检查设备的连接态，把设备信息设置到对应的控件上
        if (bleDevice != null) {
            // 如果设备是连接的状态
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            int rssi = bleDevice.getRssi();

            holder.txt_name.setText(name); // 设备名
            holder.txt_mac.setText(mac); // MAC地址
            holder.txt_rssi.setText(String.valueOf(rssi)); // 信号值


            cell.txt_name1.setText(name);
            cell.img_blue1.setImageResource(R.mipmap.ic_blue_remote);

            // 如果是连接状态的，此设备信息可见（显示在列表上）
//            if (isConnected) {
//                // 切换成连接的状态
//                holder.img_blue.setImageResource(R.mipmap.ic_blue_connected);
////                0xFF1DE9B6
//                holder.txt_name.setTextColor(0xFF2DA4FF);
//                holder.txt_mac.setTextColor(0xFF2DA4FF);
//                holder.layout_idle.setVisibility(View.GONE);
//                holder.layout_connected.setVisibility(View.VISIBLE);
//
//
//            } else {
//                holder.img_blue.setImageResource(R.mipmap.ic_blue_remote);
//                holder.txt_name.setTextColor(0xFF000000);
//                holder.txt_mac.setTextColor(0xFF000000);
//                holder.layout_idle.setVisibility(View.VISIBLE);
//                holder.layout_connected.setVisibility(View.GONE);
//            }
        }
        holder.device_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // 调用监听器，与此设备
                    mListener.onConnect(bleDevice);
                }
            }
        });

        cell.device_layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // 调用监听器，与此设备
                    mListener.onConnect(bleDevice);
                }
            }
        });

        // 触发“连接”按钮的点击事件
        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    // 调用监听器，与此设备
                    mListener.onConnect(bleDevice);
                }
            }
        });

        // 触发 断开连接 点击事件
        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDisConnect(bleDevice);
                }
            }
        });

        // 触发 进入操作 点击事件
        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onDetail(bleDevice);

                }
            }
        });




        return convertView;
    }

    // 对应adapter_device.xml
    class ViewHolder {
        ImageView img_blue;
        TextView txt_name;
        TextView txt_mac;
        TextView txt_rssi;
        LinearLayout layout_idle;
        LinearLayout layout_connected;
        Button btn_disconnect;
        Button btn_connect;
        Button btn_detail;
        View device_layout;

        View device_layout1;
        View device_layout2;
        View device_layout3;

        TextView txt_name1;
        TextView txt_name2;
        TextView txt_name3;
    }

    class DeviceListCell {
        LinearLayout device_layout1;
        ImageView img_blue1;
        TextView txt_name1;
    }

    // 在设备上单击监听器
    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);

        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }
    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
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
}
