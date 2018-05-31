package com.test.sharecarble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.test.sharecarble.app.AppApplication;
import com.test.sharecarble.bluetooth.BluetoothLeService;
import com.test.sharecarble.bluetooth.GattAttributes;
import com.test.sharecarble.util.CommandUtil;
import com.test.sharecarble.util.DataUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by z on 2017/9/8 0008.
 */

public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView mLv;
    private Button mBtnCheck;
    private Button mBtnCloseClock;
    private Button mBtnOpenClock;
    private Button mBtnBooking;
    private Button mBtnUnBooking;
    private Button mBtnLooking;
    private Button mBtnSearch;
    private Button mBtnSetIp;
    private Button mBtnSetPort;
    private Button mBtnSetId;
    private TextView mTvMac;
    private TextView mTvDeviceInfo;
    private EditText mEt;

    private List<String> mData;
    private ArrayAdapter<String> mAdapter;

    private BluetoothLeService mBluetoothLeService;


    private String mMac;//设备发过来的mac

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        mBtnCheck = (Button) findViewById(R.id.btn_check);
        mBtnCloseClock = (Button) findViewById(R.id.btn_close_clock);
        mBtnOpenClock = (Button) findViewById(R.id.btn_open_clock);
        mBtnBooking = (Button) findViewById(R.id.btn_booking);
        mBtnLooking = (Button) findViewById(R.id.btn_looking);
        mBtnUnBooking = (Button) findViewById(R.id.btn_unbooking);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSetIp = (Button) findViewById(R.id.btn_set_ip);
        mBtnSetPort = (Button) findViewById(R.id.btn_set_port);
        mBtnSetId = (Button) findViewById(R.id.btn_set_id);
        mEt = (EditText) findViewById(R.id.et);
        mBtnCheck.setOnClickListener(this);
        mBtnCloseClock.setOnClickListener(this);
        mBtnOpenClock.setOnClickListener(this);
        mBtnBooking.setOnClickListener(this);
        mBtnUnBooking.setOnClickListener(this);
        mBtnSearch.setOnClickListener(this);
        mBtnLooking.setOnClickListener(this);
        mBtnSetId.setOnClickListener(this);
        mBtnSetPort.setOnClickListener(this);
        mBtnSetIp.setOnClickListener(this);
        mLv = (ListView) findViewById(R.id.lv);
        mTvDeviceInfo = (TextView) findViewById(R.id.tv_device_info);
        mTvMac = (TextView) findViewById(R.id.tv_mac);

        mMac = getIntent().getStringExtra("mac");
        mTvMac.setText("MAC地址: " + mMac);
        CommandUtil.setMacData(mMac);
//        CommandUtil.setMacData("cd:52:ef:8c:67:1f".toUpperCase());

        mData = new ArrayList<>();
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mData);
        mLv.setAdapter(mAdapter);

        BluetoothLeService.IS_RECONNECTED = false;
        mBluetoothLeService = ((AppApplication) getApplication()).getBluetoothLeService();
        //注册广播
        registerReceiver(mGattUpdateReceiver, BluetoothLeService.makeGattUpdateIntentFilter());
    }

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("action = " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//GATT连成了

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {//断开了
                Toast.makeText(DeviceControlActivity.this, "断开连接了", Toast.LENGTH_LONG).show();
                finish();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {//找到Service了
//               mBluetoothLeService.getSupportedGattServices();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//硬件有数据传过来

                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                byte[] byteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA_BYTE);
//                Log.e("test", "onReceive: " + data);
                String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
                if (uuid != null && uuid.equalsIgnoreCase(GattAttributes.CONTROL_CHARACTERISTIC_2)) {

                }
                if (byteData != null) {
                    mData.add(0, "收到" + byteData.length + "字节: " + data);
                    mAdapter.notifyDataSetChanged();
                } else {
                    mData.add(0, "收到" + 0 + "字节: " + "数据空");
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_check://设备校验
//                try {
//                    if (mMac == null || "".equals(mMac)) {
//                        Toast.makeText(this, "未获取到mac地址", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
                sendCmd(CommandUtil.deviceCheck());
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                break;
            case R.id.btn_close_clock://上锁
                sendCmd(CommandUtil.closeClock());
                break;
            case R.id.btn_open_clock://开锁
                sendCmd(CommandUtil.openClock());
                break;
            case R.id.btn_booking://预约
                sendCmd(CommandUtil.booking());
                break;
            case R.id.btn_unbooking://取消预约
                sendCmd(CommandUtil.unBooking());
                break;
            case R.id.btn_looking://找车锁
                sendCmd(CommandUtil.looking());
                break;
            case R.id.btn_search://查询
                sendCmd(CommandUtil.status());
                break;
            case R.id.btn_set_id:
                String id = mEt.getText().toString().trim();
                try {
                    byte[] ids = id.getBytes("UTF-8");
                    if (ids.length < 1) {
                        Toast.makeText(this, "ID为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (ids.length > 10) {
                        Toast.makeText(this, "超过了10个字节", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendCmd(CommandUtil.setId(id));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.btn_set_ip:
                String ip = mEt.getText().toString().trim();
                if (!ipCheck(ip)) {
                    Toast.makeText(this, "IP地址格式错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendCmd(CommandUtil.setIp(ip));

                break;
            case R.id.btn_set_port:
                String port = mEt.getText().toString().trim();
                if (port == null || port.length() == 0 || port.length() > 5 || !isNumeric(port)) {
                    Toast.makeText(this, "端口号格式错误，最多5位数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendCmd(CommandUtil.setPort(port));
                break;
        }
    }

    /**
     * 判断是否为数字
     *
     * @param str
     * @return
     */
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断IP地址的合法性，这里采用了正则表达式的方法来判断
     * return true，合法
     */
    public boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." +
                    "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }

    /**
     * 发送指令
     *
     * @param cmd
     */
    private void sendCmd(byte[] cmd) {
        if (mBluetoothLeService != null && mBluetoothLeService.getBluetoothAdapter().isEnabled()) {
            if (mBluetoothLeService.getControlCharacteristic() != null) {
                mBluetoothLeService.getControlCharacteristic().setValue(cmd);
                mBluetoothLeService.writeCharacteristic(mBluetoothLeService.getControlCharacteristic());
                mData.add(0, "发送" + cmd.length + "字节: " + DataUtil.bytes2HexString(cmd));
            } else {
                mBluetoothLeService.discoverServices();
                mData.add(0, "发送失败，原因：未发现通讯通道");
            }
            mAdapter.notifyDataSetChanged();

        }
    }


    private long mLastTimePressed = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastTimePressed < 1000) {
            setResult(RESULT_OK);
            finish();
        } else {
            mLastTimePressed = System.currentTimeMillis();
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
        }
    }
}
