package com.xiaweizi.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private MyHandler mHandler;
    private int mCurrentProgress = 0;
    private TransferProgressView mProgressView;
    private SearchRipplesView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressView = findViewById(R.id.progress_view);
        mSearchView = findViewById(R.id.search_view);
        mHandler = new MyHandler(this);
        mHandler.sendEmptyMessage(1);
        mProgressView.start();
        mProgressView.initMode(false);
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mProgressView.resume();
//                if (!mHandler.hasMessages(1)) {
//                    mHandler.sendEmptyMessage(1);
//                }
                mSearchView.resume();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mProgressView.pause();
//                if (mHandler.hasMessages(1)) {
//                    mHandler.removeMessages(1);
//                }
                mSearchView.pause();
            }
        });

        findViewById(R.id.interrupt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressView.interrupt();
                if (mHandler.hasMessages(1)) {
                    mHandler.removeMessages(1);
                }
            }
        });
        findViewById(R.id.recover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHandler.hasMessages(1)) {
                    mHandler.sendEmptyMessage(1);
                }
                mProgressView.recover();
            }
        });

        findViewById(R.id.check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressView.initMode(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            theActivity.mProgressView.setProgress(theActivity.mCurrentProgress);
            sendEmptyMessageDelayed(1, 80);

//            theActivity.mBar.setCurrentProgress(50);

        }
    }
}
