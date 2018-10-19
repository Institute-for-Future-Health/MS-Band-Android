package Util;

import android.util.Log;

import java.util.Calendar;

public class Timeconverter {

    public int fiveMin = 60000*5;

    public String getDatekey(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String formated = df.format("yyyy-MM-dd", new java.util.Date()).toString();
        Log.v("Date", formated);
        String [] array =  formated.split("-");

        return array[1] + "" + "" + array[2] + "_" + array[0];
    }



    public String getTimewindow(long timestamp){
        int res = 0;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        int totalminute = c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE);
        Log.v("time", totalminute+"");
        res = (int) (totalminute/5);

        return res+"";
    }

    public String getDateFormat(long timeStamp){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String formated = df.format("yyyyMMdd_hh:mm:ss", new java.util.Date()).toString();
        return formated;
    }
}
