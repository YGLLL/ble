package com.test.sharecarble;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.test.sharecarble.app.AppApplication;
import com.test.sharecarble.bluetooth.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_FINISH = 11110;
    private ListView mLv;
    private Button mBtn;

    private ProgressDialog mLoading;

    private List<BluetoothDevice> mDeviceData;
    private BluetoothLeService mBluetoothLeService;
    private DeviceAdapter mDeviceAdapter;
    String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLv = (ListView) findViewById(R.id.lv);

        mLoading = new ProgressDialog(this);
        mLoading.setMessage("连接中...");

        mDeviceData = new ArrayList<>();
        mDeviceAdapter = new DeviceAdapter(this, mDeviceData);
        mLv.setAdapter(mDeviceAdapter);
        initListener();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //service的混合使用  先启动再bind
//                startService(gattServiceIntent);
        boolean bll = bindService(gattServiceIntent, mServiceConnection,
                BIND_AUTO_CREATE);


        mBtn = (Button) findViewById(R.id.btn_search);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("开始搜索".equals(mBtn.getText().toString())) {
					//坑，服务启动以后才可以调用
                    //防止连接中，又点了开始搜索的按钮。要先断开连接，才能搜索
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.disconnect();
                        mBluetoothLeService.close();
                    }

                    mDeviceData.clear();
                    mDeviceAdapter.notifyDataSetChanged();

                    mBtn.setText("搜索中");

                    //判断蓝牙是否已经打开
                    if (mBluetoothLeService.getBluetoothAdapter().enable()) {
                        requiresLocationPermission();
                    } else {
                        Toast.makeText(MainActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
                        mBtn.setText("开始搜索");

                    }
                }
            }
        });
    }

    /**
     * 蓝牙service绑定的生命周期回调
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        /**
         * Service bind 成功后的回调
         * @param componentName
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            ((AppApplication) getApplication()).setBluetoothLeService(mBluetoothLeService);
//            // Automatically connects to the device upon successful start-up
//            // initialization.
//            mBluetoothLeService.connect(mDeviceAddress);

//            //判断蓝牙是否已经打开
//            if (mBluetoothLeService.getBluetoothAdapter().enable()) {
//                requiresLocationPermission();
//            } else {
//                Toast.makeText(MainActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mIsSee = true;
        BluetoothLeService.IS_RECONNECTED = true;

        //注册广播
        registerReceiver(mGattUpdateReceiver, BluetoothLeService.makeGattUpdateIntentFilter());


    }

    private int mCurrentClickPosition;

    private void initListener() {
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mCurrentClickPosition = position;
                if (mBluetoothLeService != null && mBluetoothLeService.initialize()) {
                    BluetoothLeService.IS_RECONNECTED = true;
                    scanLeDevice(false);
                    mIsGo = false;
                    mBluetoothLeService.connect(mDeviceData.get(mCurrentClickPosition).getAddress());
                    mLoading.show();
                }
            }
        });
    }

    private void gotoControlActivity() {
        Intent intent = new Intent(this, DeviceControlActivity.class);
//        intent.putExtra("deviceName", mDeviceData.get(mCurrentClickPosition).getName());
        intent.putExtra("mac",mDeviceData.get(mCurrentClickPosition).getAddress());
        startActivityForResult(intent, RC_FINISH);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RC_FINISH:
                finish();
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    Log.e(TAG, "onLeScan: " + rssi);
                    Log.e(TAG, "onLeScan: mac=" + device.getAddress());
                    Log.e(TAG, "onLeScan: " + device.getName());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            device.connectGatt(MainActivity.this,true,new );

                            if (!mDeviceData.contains(device) && device.getName() != null) {
                                mDeviceData.add(device);
                                mDeviceAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            };


    private boolean mIsGo;


    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("action = " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//GATT连成了
                if (!mIsGo) {
                    mLoading.dismiss();
                    gotoControlActivity();
                    mIsGo = true;
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {//断开了
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {//找到Service了
//               mBluetoothLeService.getSupportedGattServices();
            }
        }
    };


    private void scanDevice() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanLeDevice(true);
            }
        }, 3000L);//蓝牙开发中，延迟操作是一个很重要的技巧。

        //扫描很费电，官方建议扫描一段时间后停止扫描
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsSee) {
                    return;
                }
                scanLeDevice(false);
            }
        }, 30000L);
    }

    /**
     * 扫描蓝牙设备
     * <p>扫描很费电，官方建议扫描一段时间后停止扫描
     *
     * @param isSearch
     */
    private void scanLeDevice(boolean isSearch) {
        if (isSearch) {
            mBtn.setText("搜索中");

            Log.e("test", "开始扫描");
            //下面这句是通过UUID过滤设备，如果有此需求的话.
//            mBluetoothLeService.getBluetoothAdapter().startLeScan(new UUID[]{UUID.fromString(GattAttributes.SERVER_CHARACTERISTIC)}, mLeScanCallback);
            mBluetoothLeService.getBluetoothAdapter().startLeScan(mLeScanCallback);
        } else {
            Log.e("test", "停止扫描");
            mBtn.setText("开始搜索");

            mBluetoothLeService.getBluetoothAdapter().stopLeScan(mLeScanCallback);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //把权限处理转移给EasyPermissions
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private final int RC_LOCATION_PERMISSION = 101;

    @AfterPermissionGranted(RC_LOCATION_PERMISSION)
    private void requiresLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {

            if (!checkPermissionAgain()) return;


            // Already have permission, do the thing
            scanDevice();

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "获取周围的设备需要定位权限",
                    RC_LOCATION_PERMISSION, perms);
            mBtn.setText("开始搜索");

        }
    }


    /**
     * 权限允许
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (!checkPermissionAgain()) return;


        switch (requestCode) {
            case RC_LOCATION_PERMISSION:
                scanDevice();
                break;

        }
    }

    private boolean checkPermissionAgain() {
        //神坑MIUI，点击拒绝定位后，检测运行时权限依然返回true。(目前只发现了MIUI的定位权限有bug)
        // 补坑：MIUI应该把运行时权限和原生权限管理分离了。所以还需进行下一步AppOps的权限管理判断。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            AppOpsManager appOpsManager = (AppOpsManager) getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
            int checkOp1 = appOpsManager.checkOp(AppOpsManager.OPSTR_FINE_LOCATION, Process.myUid(), getPackageName());
            int checkOp2 = appOpsManager.checkOp(AppOpsManager.OPSTR_COARSE_LOCATION, Process.myUid(), getPackageName());
            if (checkOp1 == AppOpsManager.MODE_IGNORED || checkOp2 == AppOpsManager.MODE_IGNORED) {
                // 权限被拒绝了
                new AppSettingsDialog.Builder(this).setTitle("未开启定位权限").
                        setRationale("获取周围的设备需要定位权限")
                        .setPositiveButton("确定").setNegativeButton("取消").build().show();
                mBtn.setText("开始搜索");
                return false;
            }
        }
        return true;
    }

    /**
     * 权限拒绝
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
// (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).setTitle("未开启定位权限").
                    setRationale("获取周围的设备需要定位权限")
                    .setPositiveButton("确定").setNegativeButton("取消").build().show();
        }
        mBtn.setText("开始搜索");

    }

    private boolean mIsSee;

    @Override
    protected void onStop() {
        unregisterReceiver(mGattUpdateReceiver);
        mIsSee = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }


    private long mLastTimePressed = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastTimePressed < 1000) {
            scanLeDevice(false);
            finish();
        } else {
            mLastTimePressed = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }
}
