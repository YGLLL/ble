package com.test.sharecarble.app;

import android.app.Application;

import com.test.sharecarble.bluetooth.BluetoothLeService;


/**
 * Created by z on 2017/3/22 0022.
 */

public class AppApplication extends Application {
    private BluetoothLeService mBluetoothLeService;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    public BluetoothLeService getBluetoothLeService() {
        return mBluetoothLeService;
    }

    public void setBluetoothLeService(BluetoothLeService bluetoothLeService) {
        this.mBluetoothLeService = bluetoothLeService;
    }
}
