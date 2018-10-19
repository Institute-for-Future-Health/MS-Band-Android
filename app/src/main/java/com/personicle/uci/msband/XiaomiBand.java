package com.personicle.uci.msband;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.MiBand;
import com.zhaoxiaodan.miband.listeners.HeartRateNotifyListener;
import com.zhaoxiaodan.miband.listeners.NotifyListener;
import com.zhaoxiaodan.miband.listeners.RealtimeStepsNotifyListener;
import com.zhaoxiaodan.miband.model.BatteryInfo;
import com.zhaoxiaodan.miband.model.UserInfo;
import com.zhaoxiaodan.miband.model.VibrationMode;

import java.util.ArrayList;
import java.util.List;

public class XiaomiBand extends AppCompatActivity{
    private MiBand miBand;
    private String targetDevice = "Mi Band 3";
    private Button start, info;
    private TextView rate, timecount;
    String TAG = "Band";
    ScanCallback scanCallback = null;

    private List<Integer> heartrates;

    private List<Integer> partialrates;
    private Handler handler;

    private EditText foodname;
    private RadioButton high, low;
    private int sugarlevel = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xiaomiband);
        miBand = new MiBand(getApplicationContext());
        info = findViewById(R.id.info);
        rate = findViewById(R.id.rate);
        start = findViewById(R.id.start);
        heartrates = new ArrayList<Integer>();
        partialrates = new ArrayList<Integer>();
        timecount = findViewById(R.id.timecount);
        high = findViewById(R.id.high);
        low = findViewById(R.id.low);
        foodname = findViewById(R.id.foodname);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                rate.setText(msg.arg1 + "");
            }
        };


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                scan();
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
            scan();
        }



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(high.isChecked())
                    sugarlevel = 1;
                TimeCounter tc = new TimeCounter(timecount, Parameters.timelengh){
                    @Override
                    public void minuteChange() {
                        super.minuteChange();
                        miBand.startHeartRateScan();

                        int s = partialrates.size();
                        if(s > 0){
                            int ave = 0;
                            for(int i : partialrates){
                                ave += i;
                            }
                            ave = ave/s;
                            heartrates.add(s);
                            partialrates.clear();
                            Log.d(TAG, "Minute Changed, Heart rate:" + ave);
                            Message msg = new Message();
                            msg.arg1 = ave;
                            handler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void afterwards() {
                        super.afterwards();
                        DataSender ds = new DataSender("ms-band");
                        ds.sendData(heartrates, sugarlevel, foodname.getText().toString().toLowerCase());
                    }
                };
                tc.MSstart();
                miBand.startHeartRateScan();
                Toast.makeText(getApplicationContext(), "Started Heartrate: " + miBand.getDevice().getName(), Toast.LENGTH_SHORT).show();
                miBand.setRealtimeStepsNotifyListener(new RealtimeStepsNotifyListener() {
                    @Override
                    public void onNotify(int steps) {
                        Log.v(TAG, "Steps: " + steps);
                    }
                });

                miBand.enableRealtimeStepsNotify();
            }
        });




        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MiBand.info(scanCallback);
//                Toast.makeText(getApplicationContext(), "Stopped Scan", Toast.LENGTH_SHORT).show();

                UserInfo userInfo = new UserInfo(1, 1, 21, 180, 55, "胖梁", 0);
                miBand.setUserInfo(userInfo);

                miBand.setHeartRateScanListener(new HeartRateNotifyListener()
                {
                    @Override
                    public void onNotify(final int heartRate)
                    {
                        if(heartRate > 0){
                            partialrates.add(heartRate);
                        }
                    }
                });
            }
        });


        miBand.setDisconnectedListener(new NotifyListener()
        {
            @Override
            public void onNotify(byte[] data)
            {
                Log.d("Band","Disconntected!!!");
            }
        });
    }


    private void scan(){
        scanCallback = new ScanCallback()
        {
            @Override
            public void onScanResult(int callbackType, ScanResult result)
            {
                BluetoothDevice device = result.getDevice();
                Log.d("Found Device", "name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());
                // 根据情况展示

                if(device.getName() != null&&device.getName().equals(targetDevice)){
                    Toast.makeText(getApplicationContext(),"Found it ! " + device.getName() + " " + device.getAddress(), Toast.LENGTH_LONG).show();
                    miBand.connect(device, new ActionCallback() {

                        @Override
                        public void onSuccess(Object data)
                        {
                            UserInfo userInfo = new UserInfo(322222, 1, 32, 180, 55, "胖梁", 1);
                            miBand.setUserInfo(userInfo);
                            MiBand.stopScan(scanCallback);
                            Log.d(TAG,"connect success, Stopped Scan");

                            Log.d(TAG, data.toString());
                        }

                        @Override
                        public void onFail(int errorCode, String msg)
                        {
                            Toast.makeText(getApplicationContext(),"connect fail" + errorCode, Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        };

        MiBand.startScan(scanCallback);
    }
}
