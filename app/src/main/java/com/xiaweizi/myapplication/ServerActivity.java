package com.xiaweizi.myapplication;

import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        ExecutorService mExecutorService = Executors.newCachedThreadPool();
        try {
            ServerSocket server = new ServerSocket(ClientActivity.PORT);
            mExecutorService.execute(new Service(server));
        } catch (Exception e) {
            Log.i(TAG, "onCreate: " + e);
        }
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
                    printWriter.println("成功连接服务器" + "（服务器发送）");
                    Log.i(TAG, "成功连接服务器");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {                                   //循环接收、读取 Client 端发送过来的信息
                    String receiveMsg;
                    if ((receiveMsg = in.readLine()) != null) {
                        Log.i(TAG, "receiveMsg:" + receiveMsg);
                        if (receiveMsg.equals("0")) {
                            Log.i(TAG, "客户端请求断开连接");
                            printWriter.println("服务端断开连接" + "（服务器发送）");
                            in.close();
                            socket.close();
                            break;
                        } else {
                            String sendMsg = "我已接收：" + receiveMsg + "（服务器发送）";
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
