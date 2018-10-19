package com.personicle.uci.msband;

import android.os.CountDownTimer;
import android.os.Trace;
import android.util.Log;
import android.widget.TextView;

public class TimeCounter {
    private static final long START_TIME_IN_MILLIS = 600000;

    CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private int[] curTime;
    private TextView textView;

    private boolean isEnd = false;

    public TimeCounter(TextView text, int time) {
        this.textView = text;
        this.curTime = new int[2];
        this.curTime[0] = time;
        this.curTime[1] = 0;
        this.isEnd = false;
    }

    public void MSstart(){
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (curTime[1] == 0){
                    curTime[1] = 59;
                    curTime[0] -= 1;
                    if (curTime[0]==0)
                        isEnd = true;

                    minuteChange();

                }else {
                    curTime[1] -= 1;
                }
                String timeText = curTime[0] + ":" + curTime[1];
                Log.v("timer", timeText);
                textView.setText(timeText);

            }
            @Override
            public void onFinish() {
                afterwards();
            }
        }.start();
    }

    public void afterwards(){

    }

    public void stop(){
        mCountDownTimer.cancel();
    }

    public void minuteChange(){
    }

}
