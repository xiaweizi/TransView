package com.xiaweizi.progressview;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class TestActivity extends AppCompatActivity {

    private static final int MSG = 1;
    private MyHandler mHandler;
    private int mCurrentProgress = 0;
    private TransferProgressView mProgressView;
    private SearchRipplesView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mProgressView = findViewById(R.id.progress_view);
        mSearchView = findViewById(R.id.search_view);
        mHandler = new MyHandler(this);
        mProgressView.initMode(false);

        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHandler.hasMessages(MSG)) {
                    mHandler.sendEmptyMessage(MSG);
                }
                mProgressView.start();
            }
        });
        findViewById(R.id.bt_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler.hasMessages(MSG)) {
                    mHandler.removeMessages(MSG);
                }
                mProgressView.complete();
            }
        });

        findViewById(R.id.bt_resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHandler.hasMessages(MSG)) {
                    mHandler.sendEmptyMessage(MSG);
                }
                mProgressView.resume();
            }
        });
        findViewById(R.id.bt_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler.hasMessages(MSG)) {
                    mHandler.removeMessages(MSG);
                }
                mProgressView.getPaddingStart();
            }
        });

        findViewById(R.id.bt_interrupt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHandler.hasMessages(MSG)) {
                    mHandler.removeMessages(MSG);
                }
                mProgressView.interrupt();
            }
        });

        findViewById(R.id.bt_recover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHandler.hasMessages(MSG)) {
                    mHandler.sendEmptyMessage(MSG);
                }
                mProgressView.recover();
            }
        });

        findViewById(R.id.bt_start_ripples).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.start();
            }
        });

        findViewById(R.id.bt_stop_ripples).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    static class MyHandler extends Handler {
        WeakReference<TestActivity> mActivity;

        MyHandler(TestActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TestActivity theActivity = mActivity.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            theActivity.mCurrentProgress ++;
            if (theActivity.mCurrentProgress >= 100) {
                theActivity.mCurrentProgress = 0;
            }
            theActivity.mProgressView.setProgress(theActivity.mCurrentProgress);
            sendEmptyMessageDelayed(MSG, 80);
        }
    }
}
