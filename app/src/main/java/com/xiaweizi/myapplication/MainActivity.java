package com.xiaweizi.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private CircleProgressBar mBar;
    private MyHandler mHandler;
    private SeekBar mSeekBar;
    private int mCurrentProgress = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBar = findViewById(R.id.progress_bar);
        mSeekBar = findViewById(R.id.search_bar);
        mHandler = new MyHandler(this);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBar.setCurrentProgress(progress);
                if (mHandler.hasMessages(1)) {
                    mHandler.removeMessages(1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(1);
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler.hasMessages(1)) {
                    mHandler.removeMessages(1);
                }
            }
        });
    }

    static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            theActivity.mCurrentProgress ++;
            if (theActivity.mCurrentProgress >= 100) {
                theActivity.mCurrentProgress = 0;
            }
            theActivity.mBar.setCurrentProgress(theActivity.mCurrentProgress);
            sendEmptyMessageDelayed(1, 80);

//            theActivity.mBar.setCurrentProgress(50);

        }
    }
}
