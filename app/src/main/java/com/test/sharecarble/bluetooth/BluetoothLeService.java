/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.sharecarble.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.test.sharecarble.util.DataUtil;

import java.util.List;


/**
 * Service for managing connection and object communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private BluetoothGattCharacteristic mControlCharacteristic;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static int mConnectionState = STATE_DISCONNECTED;
    public static boolean IS_RECONNECTED = true;

    public final static String ACTION_GATT_CONNECTED = "com.test.sharecarble.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.test.sharecarble.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.test.sharecarble.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.test.sharecarble.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE_SUCCESS = "com.test.sharecarble.bluetooth.le.ACTION_DATA_WRITE_SUCCESS";
    public final static String EXTRA_DATA = "ccom.test.sharecarble.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_DATA_BYTE = "com.test.sharecarble.bluetooth.le.EXTRA_DATA_BYTE";
    public final static String EXTRA_UUID = "ccom.test.sharecarble.bluetooth.le.EXTRA_UUID";
    public final static String EXTRA_RSSI = "ccom.test.sharecarble.bluetooth.le.EXTRA_RSSI";

    public static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * GATT 连接状态改变时回调
         * @param gatt gatt实例
         * @param status 旧的状态
         * @param newState 新的状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
//			System.out.println("=======code:" + code);
            Log.e(TAG, "onConnectionStateChange: =======code:" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {//gatt已连接
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                //发广播，告诉activity或者某某某已连接成功
                broadcastUpdate(intentAction);
                Log.e(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.e(TAG, "Attempting to start service discovery:");
                //开始搜索蓝牙硬件设备的service
                mBluetoothGatt.discoverServices();

                mBluetoothDeviceAddress = gatt.getDevice().getAddress();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//gatt断开了
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.e(TAG, "Disconnected from GATT server.");
                //发广播，告诉activity或者某某某已和硬件设备断开了
                broadcastUpdate(intentAction);
                if (status == 133) {//老司机告诉你，遇到133就关闭吧。
                    close();
                }
                if (IS_RECONNECTED) {
                    if (mBluetoothGatt != null) {
                        Log.e(TAG, "GATT try to reconnect");
                        mBluetoothGatt.connect();
                    } else {
                        connect(mBluetoothDeviceAddress);
                    }
                } else {
                    disconnect();
                    close();
                }
            }
        }

        /**
         * 发现了service的回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {//成功发现了service
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService bluetoothGattService : services) {
                    Log.e(TAG, " server:"
                            + bluetoothGattService.getUuid().toString());

                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService
                            .getCharacteristics();
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristics) {
                        Log.e(TAG, " charac:"
                                + bluetoothGattCharacteristic.getUuid()
                                .toString().toUpperCase());
                        if (bluetoothGattCharacteristic.getUuid().toString().toUpperCase()
                                .equals(GattAttributes.CONTROL_CHARACTERISTIC)) {
//                            //回调在onCharacteristicChanged
                            mControlCharacteristic = bluetoothGattCharacteristic;
                            setCharacteristicNotification(mControlCharacteristic, true);
                            Log.e(TAG, "onServicesDiscovered: " + "notify了");

                        } else if (bluetoothGattCharacteristic.getUuid().toString().toUpperCase()
                                .equals(GattAttributes.CONTROL_CHARACTERISTIC_2)) {
                            setCharacteristicNotification(bluetoothGattCharacteristic, true);
                            Log.e(TAG, "onServicesDiscovered: " + "notify了");

                        }
                    }
                }
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {//没有找到service
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /**
         * 读取硬件数据的时候回调（手机去读取硬件的数据）
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
//            System.out.println("onCharacteristicRead");
            Log.e(TAG, "onCharacteristicRead: ");
            if (status == BluetoothGatt.GATT_SUCCESS) {//数据读取成功
                //发广播，把特征值传给Activity
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {

            System.out.println("onDescriptorWriteonDescriptorWrite = " + status
                    + ", descriptor =" + descriptor.getUuid().toString());
        }

        /**
         * 硬件有数据传过来的时候回调（硬件主动发数据过来）(setNotification以后)
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            if (characteristic.getValue() != null) {
//                Log.e(TAG, "onCharacteristicChanged: "+characteristic.getValue().length );
//                System.out.println(characteristic.getStringValue(0));
                Log.e(TAG, "onCharacteristicChanged: " + characteristic.getUuid() + "\n" + DataUtil.bytes2HexString(characteristic.getValue()));
            } else {
                Log.e(TAG, "onCharacteristicChanged: " + characteristic.getUuid() + "\n" + "数据为空");

            }
//            System.out.println("--------onCharacteristicChanged-----");
        }

        /**
         * 读取硬件设备的型号强度
         * @param gatt
         * @param rssi 信号强度 负数越小  信号越强
         * @param status
         */
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            System.out.println("rssi = " + rssi);
            Log.e(TAG, "onReadRemoteRssi: rssi" + rssi);
            Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_RSSI, rssi);
            sendBroadcast(intent);
        }

        /**
         * 向硬件发送数据的时候回调
         * @param gatt
         * @param characteristic
         * @param status
         */
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
//            System.out.println("--------write success----- code:" + code);
//            Log.e(TAG, "onCharacteristicWrite: --------write success----- code:" + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                String str = DataUtil.bytes2HexString(characteristic.getValue());
                Log.e(TAG, "onCharacteristicWrite--------write success-----" + str);

            }
            broadcastUpdate(ACTION_DATA_WRITE_SUCCESS);
        }


    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the object formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(
//                        object.length);
//                for (byte byteChar : object)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//
//                System.out.println("ppp" + new String(object) + "\n"
//                        + stringBuilder.toString());
//                intent.putExtra(EXTRA_DATA, new String(object) + "\n"
//                        + stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, DataUtil.bytes2HexString(data));
            intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
            intent.putExtra(EXTRA_DATA_BYTE, data);
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        return mBinder;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;//打不死的小强
//    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
//		close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }

        }

        if (!mBluetoothAdapter.enable()) {
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.e(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.e(TAG,
                    "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are mine the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.e(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public synchronized boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        return mBluetoothGatt.writeCharacteristic(characteristic);

    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.e(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (isEnableNotification) {
            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
            if (descriptorList != null && descriptorList.size() > 0) {
                for (BluetoothGattDescriptor descriptor : descriptorList) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }

//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
//                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//        if (descriptor != null) {
//            System.out.println("write descriptor");
//            descriptor
//                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
        /*
         * // This is specific to Heart Rate Measurement. if
		 * (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
		 * System
		 * .out.println("characteristic.getUuid() == "+characteristic.getUuid
		 * ()+", "); BluetoothGattDescriptor descriptor =
		 * characteristic.getDescriptor
		 * (UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
		 * descriptor
		 * .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		 * mBluetoothGatt.writeDescriptor(descriptor); }
		 */
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    /**
     * Read the RSSI for a connected remote device.
     */
    public boolean getRssiVal() {
        if (mBluetoothGatt == null)
            return false;

        return mBluetoothGatt.readRemoteRssi();
    }

    public BluetoothGattCharacteristic getControlCharacteristic() {
        return mControlCharacteristic;
    }

    public String getBluetoothDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * 发现服务
     */
    public void discoverServices() {
        if (mBluetoothGatt != null) {
            //开始搜索蓝牙硬件设备的service
            mBluetoothGatt.discoverServices();
        }
    }

    @Override
    public void onDestroy() {
        disconnect();
        close();
        super.onDestroy();
    }
}
