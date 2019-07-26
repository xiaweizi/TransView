package com.xiaweizi.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "ClientActivity::";
    private static final String HOST = "10.0.2.2";
    private static final int MSG = 1;
    private static final int DURATION = 60;
    public static final int PORT = 5000;
    private PrintWriter printWriter;
    private BufferedReader in;
    private ExecutorService mExecutorService = null;
    private TransferProgressView mProgressView;
    private ListView mListView;
    private MyAdapter mAdapter;
    private MyHandler mHandler;
    private int mProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        initView();
        mHandler = new MyHandler(this);
        mExecutorService = Executors.newCachedThreadPool();
    }

    private void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.transfer_send_color));
        mProgressView = findViewById(R.id.pv_client);
        mProgressView.initMode(true);
        mListView = findViewById(R.id.lv_client);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    private void addData(final String msg) {
        if (mAdapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.addData(msg);
                    mListView.smoothScrollToPosition(mAdapter.getCount() - 1);
                }
            });
        }
    }

    public void connect(View view) {
        if (mExecutorService != null) {
            mExecutorService.execute(new connectService());  //在一个新的线程中请求 Socket 连接
        }
    }

    public void disconnect(View view) {
        sendMessage(Constants.DISCONNECT);
    }

    public void start(View view) {
        sendMessage(Constants.START);
        if (!mHandler.hasMessages(MSG)) {
            mProgress = 0;
            mHandler.sendEmptyMessage(MSG);
        }
        mProgressView.start();
    }

    public void pause(View view) {
        sendMessage(Constants.PAUSE);
        if (mHandler.hasMessages(MSG)) {
            mHandler.removeMessages(MSG);
        }
        mProgressView.pause();
    }

    public void resume(View view) {
        sendMessage(Constants.RESUME);
        if (!mHandler.hasMessages(MSG)) {
            mHandler.sendEmptyMessage(MSG);
        }
        mProgressView.resume();
    }

    public void recover(View view) {
        sendMessage(Constants.RECOVER);
        if (!mHandler.hasMessages(MSG)) {
            mHandler.sendEmptyMessage(MSG);
        }
        mProgressView.recover();
    }

    public void interrupt(View view) {
        sendMessage(Constants.INTERRUPT);
        if (mHandler.hasMessages(MSG)) {
            mHandler.removeMessages(MSG);
        }
        mProgressView.interrupt();
    }

    private void sendMessage(int msg) {
        if (mExecutorService != null) {
            mExecutorService.execute(new sendService(msg));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressView.release();
    }

    private class sendService implements Runnable {
        private int msg;

        sendService(int msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (printWriter != null) {
                printWriter.println(this.msg);
            } else {
                addData("已断开连接");
            }
        }
    }

    private class connectService implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(HOST, PORT);
                socket.setSoTimeout(100000);
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                receiveMsg();
            } catch (Exception e) {
                addData(e.getMessage());
            }
        }
    }

    private void receiveMsg() {
        try {
            while (true) {                                      //步骤三
                String receiveMsg;
                if ((receiveMsg = in.readLine()) != null) {
                    Log.d(TAG, "receiveMsg:" + receiveMsg);
                    addData("receive msg: " + receiveMsg);
                }
            }
        } catch (IOException e) {
            addData(e.getMessage());
        }
    }

    static class MyHandler extends Handler {
        WeakReference<ClientActivity> mActivity;

        MyHandler(ClientActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ClientActivity theActivity = mActivity.get();
            if (theActivity == null || theActivity.isFinishing()) {
                return;
            }
            if (theActivity.mProgress >= 100) {
                theActivity.mProgressView.complete();
                removeMessages(MSG);
                theActivity.sendMessage(Constants.COMPLETE);
            } else {
                theActivity.mProgress += 1;
                sendEmptyMessageDelayed(MSG, DURATION);
            }
            theActivity.sendMessage(theActivity.mProgress);
            theActivity.mProgressView.setProgress(theActivity.mProgress);
        }
    }

}
