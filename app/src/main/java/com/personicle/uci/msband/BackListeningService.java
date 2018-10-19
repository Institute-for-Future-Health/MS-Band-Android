package com.personicle.uci.msband;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class BackListeningService extends Service {
    private static final long START_TIME_IN_MILLIS = 600000;
    private static final long SEND_DATA_INTERVAL = 15;
    private static final int TIME_WINDOW_SIZE = 5;
    private static final int DAY_MINUTE = 60*24;
    CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private int countMin = 0;


    private BandClient client;

    private Button start, consent;
    private TextView heartrate;

    private int accHeartrate = 0;
    private int aveHeartrate = 0;
    private int minuteCounter = 0;
    private int[] minuteWindow = new int[TIME_WINDOW_SIZE];

    private HashMap<String, Integer> heartRateData = new HashMap<>();
    private List<Integer> windowHeartRate = new ArrayList<Integer>();
    private String macAddress = "";
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final WeakReference<Service> reference = new WeakReference<Service>(this);

        new HeartRateSubscriptionTask().execute();
        macAddress = intent.getExtras().getString("mac");
        initDatabse();

        return super.onStartCommand(intent, flags, startId);
    }

    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                if(getConnectBand()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(bandHeartRateEventListener);
                        client.getSensorManager().registerPedometerEventListener(bandPedometerEventListener);
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

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
            }
            
            @Override
            public void onFinish() {
                aveHeartrate = accHeartrate /60;
                minuteWindow[minuteCounter++] = aveHeartrate;
                if (minuteCounter == TIME_WINDOW_SIZE - 1){
                    minuteCounter = 0;

                }
                startTimer();
            }
        }.start();

    }


    private void initDatabse(){
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference("ms-band");


    }



    private void sendData(HashMap<Integer, Integer []> windows){
        final String firstKey = macAddress;
        final String secondKey = getFormatDate();
        String thirdKey = "wearable";
        String fourthKey = "timely";

        if(databaseRef == null){
            initDatabse();
        }

        HashMap<Integer, Object> timelyHash = new HashMap<>();
        HashMap<String, Object> dateHash = new HashMap<>();
        HashMap<String, Object> wearableHash = new HashMap<>();
        final HashMap<String, Object> personalHash = new HashMap<>();

        for (Integer i : windows.keySet()){
            Integer[] subset = windows.get(i);
            for (int j = 1; j <= subset.length; i++){

            }
        }

        windowHeartRate.clear();
        wearableHash.put(fourthKey, timelyHash);
        dateHash.put(thirdKey, wearableHash);

        personalHash.put(secondKey, dateHash);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(firstKey)){
                    databaseRef.updateChildren(personalHash);
                }else{
                    databaseRef.child(firstKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(secondKey)){

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getFormatDate(){
        long timestamp = System.currentTimeMillis();
        String date = "";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String formated = df.format("yyyy-MM-dd", new java.util.Date()).toString();
        Log.v("Date", formated);
        String [] array =  formated.split("-");
        date = array[1] + "" + "" + array[2] + "_" + array[0];
        return date;
    }

    private BandHeartRateEventListener bandHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent != null){
                Log.v("HR", bandHeartRateEvent.getHeartRate()+"");
                accHeartrate += bandHeartRateEvent.getHeartRate();
            }
        }
    };

    private BandPedometerEventListener bandPedometerEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
            if(bandPedometerEvent != null){
                Log.v("Steps", bandPedometerEvent.getTotalSteps()+ "");
            }
        }
    };



}

