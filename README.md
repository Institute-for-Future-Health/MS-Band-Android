# MS-Band-Android

#Microsoft Band API For Anrdoid

##Preparation

To make sure the Microsoft Band is connected to your phone, download the [Microsoft Band](https://play.google.com/store/apps/details?id=com.microsoft.kapp) and connect it in the app, also keep the bluetooth on.


##Code

###Connect

```Java
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

```

###Consent

Function for consenting reading heartrate.

```Java
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
```

In your onclick() event, call HeartRateConsentTask.execute().


```Java
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.microsoft);
        final WeakReference<Activity> reference = new WeakReference<Activity>(this);

        consent = findViewById(R.id.consent);
        consent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MicrosoftBand.HeartRateConsentTask().execute(reference);
            }
        });
    }
      
```

There will be a pop window asking for Yes or No, click yes to consent.

###Reading Heartrate

```Java
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
```

And in your event for start listening, call HeartRateSubscriptionTask:

```Java
private HeartRateSubscriptionTask mHeartRateSubscriptionTask;

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeartRateSubscriptionTask = new MicrosoftBand.HeartRateSubscriptionTask();
                mHeartRateSubscriptionTask.execute();
            }
        });
```

Define a BandHeartRateEventListener to keep reading the heartrate values.

```Java
private BandHeartRateEventListener bandHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
            if(bandHeartRateEvent != null){
                Log.v("HeartRate", bandHeartRateEvent.getHeartRate()+"");
            }
        }
    };
```
