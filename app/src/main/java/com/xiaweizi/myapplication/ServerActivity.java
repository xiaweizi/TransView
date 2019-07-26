package com.xiaweizi.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerActivity extends AppCompatActivity {

    private static final String TAG = "ServerActivity::";
    private TransferProgressView mProgressView;
    private MyAdapter mAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        initView();
        ExecutorService executorService = Executors.newCachedThreadPool();
        try {
            ServerSocket server = new ServerSocket(ClientActivity.PORT);
            executorService.execute(new Service(server));
        } catch (Exception e) {
            Log.i(TAG, "onCreate: " + e);
        }
    }

    private void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.transfer_receiver_color));
        mProgressView = findViewById(R.id.pv_server);
        mProgressView.initMode(false);
        mListView = findViewById(R.id.lv_server);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressView.release();
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

    private void updateProgress(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int progress = Constants.DEFAULT;
                try {
                    progress = Integer.parseInt(msg);
                } catch (Exception e) {
                    addData(e.getMessage());
                }
                switch (progress) {
                    case Constants.START:
                        mProgressView.start();
                        break;
                    case Constants.COMPLETE:
                        mProgressView.complete();
                        break;
                    case Constants.RESUME:
                        mProgressView.resume();
                        break;
                    case Constants.PAUSE:
                        mProgressView.pause();
                        break;
                    case Constants.RECOVER:
                        mProgressView.recover();
                        break;
                    case Constants.INTERRUPT:
                        mProgressView.interrupt();
                        break;
                    case Constants.DEFAULT:
                        break;
                    default:
                        mProgressView.setProgress(progress);
                        break;
                }
            }
        });
    }

    class Service implements Runnable {
        private ServerSocket socket;
        private BufferedReader in = null;
        private PrintWriter printWriter = null;

        Service(ServerSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                try {
                    Socket client = socket.accept();
                    printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)), true);
                    in = new BufferedReader(new InputStreamReader(
                            client.getInputStream(), StandardCharsets.UTF_8));
                    printWriter.println("成功连接服务器 from server");
                    addData("成功连接服务器");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {                                   //循环接收、读取 Client 端发送过来的信息
                    String receiveMsg;
                    if ((receiveMsg = in.readLine()) != null) {
                        updateProgress(receiveMsg);
                        addData("receive msg: " + receiveMsg);
                        if (receiveMsg.equals(Constants.DISCONNECT+"")) {
                            addData("客户端请求断开连接");
                            printWriter.println("服务端断开连接 from server");
                            in.close();
                            socket.close();
                            break;
                        } else {
                            String sendMsg = "我已接收：" + receiveMsg + " from server";
                            printWriter.println(sendMsg);           //向 Client 端反馈、发送信息
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
