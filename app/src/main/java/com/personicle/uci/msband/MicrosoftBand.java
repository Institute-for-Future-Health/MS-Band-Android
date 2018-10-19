package com.personicle.uci.msband;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MicrosoftBand extends AppCompatActivity{
    private BandClient client;
    private Button consent;
    private Button start;

    private List<Integer> heartrates;

    private int time;
    private int hrInMinute = 0;
    private int secCount = 0;

    private int sugarLevel = 0;

    private RadioButton low, high;
    private MicrosoftBand.HeartRateSubscriptionTask mHeartRateSubscriptionTask;
    private TextView timecount;
    private TimeCounter tc;
    private EditText foodText;
    private String foodname = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.microsoft);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);

        consent = findViewById(R.id.consent);
        start = findViewById(R.id.start);

        low = findViewById(R.id.low);
        high = findViewById(R.id.high);
        foodText = findViewById(R.id.foodname);


        timecount = findViewById(R.id.timecount);
        consent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MicrosoftBand.HeartRateConsentTask().execute(reference);
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heartrates = new ArrayList<Integer>();
                if(low.isChecked()){
                    sugarLevel = 0;
                }else{
                    sugarLevel = 1;
                }
                foodname = foodText.getText().toString();
                tc = new TimeCounter(timecount, Parameters.timelengh);
                tc.MSstart();
                mHeartRateSubscriptionTask = new MicrosoftBand.HeartRateSubscriptionTask();
                mHeartRateSubscriptionTask.execute();
            }
        });

    }

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if(getConnectBand()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(bandHeartRateEventListener);
                    }
                }
            } catch (BandException e) {
                e.printStackTrace();
                return null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private boolean getConnectBand() throws BandException, InterruptedException {
        if(client == null){
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0){
//                Toast.makeText(getApplicationContext(), "No device detected", Toast.LENGTH_LONG).show();
                return false;
            }
            client = BandClientManager.getInstance().create(getApplicationContext(), devices[0]);
        }else if(ConnectionState.CONNECTED == client.getConnectionState()){
            return true;
        }

        return ConnectionState.CONNECTED == client.connect().await();
    }


    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {

        @Override
        protected Void doInBackground(WeakReference<Activity>... weakReferences) {

            try{
                if(getConnectBand()){
                    if(weakReferences[0].get() != null){
                        client.getSensorManager().requestHeartRateConsent(weakReferences[0].get(), new HeartRateConsentListener() {
                            @Override
                            public void userAccepted(boolean b) {

                            }
                        });
                    }
                }else {

                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BandException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private BandHeartRateEventListener bandHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent != null){
                Log.v("HR", bandHeartRateEvent.getHeartRate()+"");
                if (secCount < 60){
                    hrInMinute += bandHeartRateEvent.getHeartRate();
                    secCount += 1;
                }else{
                    heartrates.add(hrInMinute/secCount);
                    secCount = 0;
                    hrInMinute = 0;
                    if(heartrates.size() == Parameters.timelengh){
                        DataSender ds = new DataSender("ms-band");
                        ds.sendData(heartrates, sugarLevel, foodname);
                        Log.v("Finish Msg","Data Collected");
                        tc.stop();
                        mHeartRateSubscriptionTask.cancel(true);
                    }
                }
            }
        }
    };

//    private BandPedometerEventListener bandPedometerEventListener = new BandPedometerEventListener() {
//        @Override
//        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
//            if(bandPedometerEvent != null){
//                Log.v("Steps", bandPedometerEvent.getTotalSteps()+ "");
//            }
//        }
//    };

//    private void startTimer(){
//
//    }
}
