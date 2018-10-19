package com.personicle.uci.msband;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class DataSender {
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;

    public DataSender(String baseName) {
        database = FirebaseDatabase.getInstance();
        databaseRef = database.getReference(baseName);
    }

    public void sendData(List<Integer> heartrates, int sugarLevel, String foodname){
        HashMap<String, Object> hrRow = new HashMap<String , Object>();
        hrRow.put("heartrates", heartrates);
        hrRow.put("sugarlevel", sugarLevel);
        hrRow.put("foodname", foodname);
        databaseRef.push().setValue(hrRow);
    }


    public void sendFitbitData(String date, int startminute, int sugarLevel, String foodname){
        HashMap<String, Object> hrRow = new HashMap<String , Object>();
        hrRow.put("date", date);
        hrRow.put("startMinute", startminute);
        hrRow.put("endMinute", startminute + 20);
        hrRow.put("sugarlevel", sugarLevel);
        hrRow.put("foodname", foodname);
        databaseRef.push().setValue(hrRow);
    }
}
