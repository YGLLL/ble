package com.test.sharecarble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by z on 2017/9/8 0008.
 */

public class DeviceAdapter extends BaseAdapter {
    private Context mContext;
    private List<BluetoothDevice> mDeviceData;

    public DeviceAdapter(Context context, List<BluetoothDevice> deviceData) {
        this.mContext = context;
        this.mDeviceData = deviceData;
    }

    @Override
    public int getCount() {
        return mDeviceData.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(view==null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_device_list,null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }


        viewHolder.mTvName.setText(mDeviceData.get(i).getName());
        viewHolder.mTvMac.setText(mDeviceData.get(i).getAddress());

        return view;
    }

    public class ViewHolder {
        private TextView mTvName;
        private TextView mTvMac;

        public ViewHolder(View view) {
            mTvName = (TextView) view.findViewById(R.id.tv_name);
            mTvMac = (TextView) view.findViewById(R.id.tv_mac);
        }
    }

}
