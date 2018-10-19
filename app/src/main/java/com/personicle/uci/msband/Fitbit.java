package com.personicle.uci.msband;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class Fitbit extends AppCompatActivity{
    private Button consent;
    private Button start;
    private RadioButton high, low;
    private int oneSec = 1000*60;

    private EditText foodText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fitbit);
        consent = findViewById(R.id.consent);
        start = findViewById(R.id.start);

        low = findViewById(R.id.low);
        high = findViewById(R.id.high);

        foodText = findViewById(R.id.foodname);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime dt = new DateTime();
                String starttime = "";

                android.text.format.DateFormat df = new android.text.format.DateFormat();
                String formated = df.format("MMdd_yyyy", new java.util.Date()).toString();

                int startminute = dt.getMinuteOfDay();
                DataSender ds = new DataSender("fitbit-band");
                int sugarlevel = 0;
                if(high.isChecked())
                    sugarlevel = 1;
                String foodname = foodText.getText().toString().toLowerCase();
                ds.sendFitbitData(formated, startminute, sugarlevel, foodname);
                Toast.makeText(getApplicationContext(), "Start time:" + formated + " " + starttime, Toast.LENGTH_LONG).show();
            }
        });


    }
}
