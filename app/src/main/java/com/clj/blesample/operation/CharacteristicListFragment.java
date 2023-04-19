package com.clj.blesample.operation;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.clj.blesample.R;
import com.clj.fastble.BleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CharacteristicListFragment extends Fragment {

    private ResultAdapter mResultAdapter;
    private BluetoothGattCharacteristic characteristicRead;
    private BluetoothGattCharacteristic characteristicRead3;
    private BluetoothGattService gattService;

    // 当碎片将要第一次绘制它的用户界面时系统调用该方法。
    // 为了绘制碎片的UI，你需要从该方法中返回一个代表碎片根布局的View组件。如果该碎片不提供用户界面，直接返回null。
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_characteric_list, null);
        initView(v);
        return v;
    }

    // 服务列表的初始化布局
    private void initView(View v) {
        mResultAdapter = new ResultAdapter(getActivity());
        System.out.println("mResultAdapter的值：" + mResultAdapter);
        System.out.println("mResultAdapter大小：" + mResultAdapter.getCount());

        ListView listView_device = (ListView) v.findViewById(R.id.list_service);
        listView_device.setAdapter(mResultAdapter);
        System.out.println("此时mResultAdapter的值：" + mResultAdapter);
        System.out.println("此时mResultAdapter大小：" + mResultAdapter.getCount());
        System.out.println("此时mResultAdapter是否为空：" + mResultAdapter.isEmpty());
        if(!mResultAdapter.isEmpty()) {
            System.out.println("此时mResultAdapter[0]：" + mResultAdapter.getItem(0));
        }
        // 列表当中添加点击事件
        // 点击特性以后，会出现该特性的属性
        listView_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // parent:容器指针，通过强制类型转化得到的list对象调用getAdapter()方法获得适配对象，通过适配对象就可以获得所展示的每一项的对象model
                // view:点击的“这个”view的句柄，通过view来获得“这个”控件的ID，使用view.findViewById()方法来获取所点击item中的控件
                // position:是“这个”在适配器中的位置（生成listview后，适配器一个一个的做item，然后按顺序放到listview中，position是序号）
                // id:是所点击项在listsview里的第几行位置，大部分时候position和id的值是一样的，

                System.out.println("parent值：" + parent);
                System.out.println("position值：" + position);
                System.out.println("id值：" + id);

                //
                final BluetoothGattCharacteristic characteristic = mResultAdapter.getItem(position);
                System.out.println("characteristic的uuid值" + characteristic.getUuid());
                System.out.println("characteristic的prperties值" + characteristic.getProperties());

                // propList：用来存储特征操作片段的值
                final List<Integer> propList = new ArrayList<>();
                // propNameList：根据操作片段的值，匹配对应的操作文字
                List<String> propNameList = new ArrayList<>();
                // charaProp：特征的Properties值，可根据值判断characteristic
                int charaProp = characteristic.getProperties();


                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    propList.add(CharacteristicOperationFragment.PROPERTY_READ);
                    propNameList.add("Read 读");
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    propList.clear();
                    propList.add(CharacteristicOperationFragment.PROPERTY_WRITE);
                    propNameList.add("Write 写");
                }
//                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
//                    propList.add(CharacteristicOperationFragment.PROPERTY_WRITE_NO_RESPONSE);
//                    propNameList.add("Write No Response");
//                }
//                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                    propList.add(CharacteristicOperationFragment.PROPERTY_NOTIFY);
//                    propNameList.add("Notify 通告");
//                }
//                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                    propList.add(CharacteristicOperationFragment.PROPERTY_INDICATE);
//                    propNameList.add("Indicate");
//                }


                // 测试：这里添加一个只读的类型
                // if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ_ONLY) > 0) {
                //    propList.add(CharacteristicOperationFragment.PROPERTY_READ_ONLY);
                //    propNameList.add("Indicate");
                // }

                // 如果可操作的内容大于1，就弹出对话框，让用户自己选择执行哪种操作
                if (propList.size() > 1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getString(R.string.select_operation_type))
                            .setItems(propNameList.toArray(new String[propNameList.size()]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((OperationActivity) getActivity()).setCharacteristic(characteristic);
                                    ((OperationActivity) getActivity()).setCharaProp(propList.get(which));
                                    ((OperationActivity) getActivity()).changePage(2);
                                }
                            })
                            .show();
                } else if (propList.size() > 0) {
                    // 如果特征的操只有一种
                    ((OperationActivity) getActivity()).setCharacteristicSign(position);
                    ((OperationActivity) getActivity()).setCharacteristic(characteristic);
                    ((OperationActivity) getActivity()).setCharacteristicRead(characteristicRead);
                    ((OperationActivity) getActivity()).setCharacteristicRead3(characteristicRead3);
                    ((OperationActivity) getActivity()).setCharaProp(propList.get(0));
                    ((OperationActivity) getActivity()).changePage(2);
                    System.out.println("进入特征内容");
                }
            }
        });
    }

    // 选择服务后，进入特性列表，就会执行这种方法
    public void showData() {
        // 获取到点击的这个服务
        BluetoothGattService service = ((OperationActivity) getActivity()).getBluetoothGattService();
        gattService = service;
        System.out.println("showData方法......");
        System.out.println("service的UUID值:" + service.getUuid());
        mResultAdapter.clear();
        System.out.println("数目：" +mResultAdapter.getCount());
        int bo = 0;
        // 遍历该服务下的特征值，并将每个特征属性加入到mResultAdapter
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            // uuid:0000fff3-0000-1000-8000-00805f9b34fb  可以根据这个值来判断
            String uid = characteristic.getUuid().toString().split("-")[0];
//            System.out.println("特征值值内容：" + uid);
//            System.out.println("将特征属性加入到mResultAdapter:" + characteristic.getProperties());
//            if("0000fff1".equals(uid)) {
            if("0000fff1".equals(uid)) {
                characteristicRead = characteristic;
            }else if("0000fff2".equals(uid) && bo == 0) {
                for(int i = 0; i < 7; i ++) {
                    mResultAdapter.addResult(characteristic);
                }
                bo = 1;
            }else if("0000fff3".equals(uid)) {
                mResultAdapter.addResult(characteristic);
                characteristicRead3 = characteristic;
            }
            // 也可以通过characteristic.getProperties()的值来判断
//            if(characteristic.getProperties() == 10 && bo == 0) {
//                for(int i = 0; i < 7; i ++) {
//                    mResultAdapter.addResult(characteristic);
//                }
//                bo = 1;
//            }else if(characteristic.getProperties() == 18) {
//                characteristicRead = characteristic;
//            }else if(characteristic.getProperties() == 10 && bo == 1) {
//                mResultAdapter.addResult(characteristic);
//            }
        }

        System.out.println("数目：" +mResultAdapter.getCount());
        // notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容
        // 刷新RecycleView，显示更改后的数据
        mResultAdapter.notifyDataSetChanged();
    }

    public BluetoothGattCharacteristic getData(){
        System.out.println("获取最新characteristic函数");
        BluetoothGattService service = ((OperationActivity) getActivity()).getBluetoothGattService();
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            if(characteristic.getProperties() == 18) {
                return characteristic;
            }
        }
        // mResultAdapter.notifyDataSetChanged();
        return null;
    }


    // 基于BaseAdapter适配器，添加功能形成ResultAdapter适配器
    private class ResultAdapter extends BaseAdapter {

        private Context context;
        private List<BluetoothGattCharacteristic> characteristicList;

        ResultAdapter(Context context) {
            this.context = context;
            characteristicList = new ArrayList<>();
        }

        void addResult(BluetoothGattCharacteristic characteristic) {
            characteristicList.add(characteristic);
        }

        void addResult(int i ,BluetoothGattCharacteristic characteristic) {
            characteristicList.add(i, characteristic);
        }


        void clear() {
            characteristicList.clear();
        }

        @Override
        public int getCount() {
            return characteristicList.size();
        }

        @Override
        public BluetoothGattCharacteristic getItem(int position) {
            if (position > characteristicList.size())
                return null;
            return characteristicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // 重写getView方法，显示列表中有多少个特性
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("显示getView方法中的属性.......position=" + position);

            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(context, R.layout.adapter_service, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
                holder.txt_uuid = (TextView) convertView.findViewById(R.id.txt_uuid);
                holder.txt_type = (TextView) convertView.findViewById(R.id.txt_type);
                holder.img_next = (ImageView) convertView.findViewById(R.id.img_next);
            }

            // 根据对应position，获取对应的特征列表
            BluetoothGattCharacteristic characteristic = characteristicList.get(position);
            System.out.println("position===============" + position);
            // uuid的值
            String uuid = characteristic.getUuid().toString();
            System.out.println("这里检测一下");

            // 这里是特性的描述
            holder.txt_title.setText(String.valueOf(getActivity().getString(R.string.characteristic) + "（" + position + ")"));
            holder.txt_uuid.setText(uuid);
            StringBuilder property = new StringBuilder();
            int charaProp = characteristic.getProperties();
            switch (position){
                case 0:{
//                    characteristicRead = characteristic;
//                    holder.txt_title.setText(String.valueOf("读取数据" + "（" + position + ")"));
                    holder.txt_title.setText(String.valueOf("单次读取"));
                    property.append("Read读, ");

                }break;
                case 1:{
//                    characteristicRead = characteristic;
                    holder.txt_title.setText(String.valueOf("实时读取"));
                    property.append("Read读, ");
                }break;
                case 2:{
                    holder.txt_title.setText(String.valueOf("历史数据"));
                    property.append("Read读, ");
                }break;
                case 3:{
                    holder.txt_title.setText(String.valueOf("时间修改"));
                    property.append("Write写, ");
                }break;
                case 4:{
                    holder.txt_title.setText(String.valueOf("清除数据"));
                    property.append("Write写, ");
                }break;
                case 5:{
                    holder.txt_title.setText(String.valueOf("校准指令"));
                    property.append("Write写, ");
                }break;
                case 6:{
                    holder.txt_title.setText(String.valueOf("地址读取"));
                    property.append("Read读, ");
                }break;
                case 7:{
                    holder.txt_title.setText(String.valueOf("设备名修改"));
                    property.append("Write写, ");
                }break;
                case 8:{
                    holder.txt_title.setText(String.valueOf("未定义1") + "(" + position + ")");
                    property.append("Write写, ");
                }break;
                default:
                    holder.txt_title.setText(String.valueOf("未定义2" + "(" + position + ")"));
            }


            // charaProp值：2：read读   10:read读 write写   18：read读 notify通告   32：indicate
            System.out.println("charaProp的值：" + charaProp);
//            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                // PROPERTY_READ == 2
//                property.append("Read 读");
//                property.append(" , ");
//            }
//            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
//                // PROPERTY_WRITE == 8
//                property.append("Write 写");
//                property.append(" , ");
//            }
//            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
//                // PROPERTY_WRITE_NO_RESPONSE == 4
//                property.append("Write No Response");
//                property.append(" , ");
//            }
//            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                // PROPERTY_NOTIFY == 16
//                property.append("Notify 通告");
//                property.append(" , ");
//            }
//            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                // PROPERTY_INDICATE == 32
//                property.append("Indicate");
//                property.append(" , ");
//            }

            // 在这里把所有的后缀逗号删除
            if (property.length() > 1) {
                // delete方法：start，end
                property.delete(property.length() - 2, property.length() - 1);
            }
            // 下面特性的描述
            if (property.length() > 0) {
                holder.txt_type.setText(String.valueOf(getActivity().getString(R.string.characteristic) + "( " + property.toString() + ")"));
                holder.img_next.setVisibility(View.VISIBLE);
            } else {
                holder.img_next.setVisibility(View.INVISIBLE);
            }


            return convertView;
        }

        class ViewHolder {
            TextView txt_title;
            TextView txt_uuid;
            TextView txt_type;
            ImageView img_next;
        }
    }
}
