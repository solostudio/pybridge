package com.byyd.pycvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byyd.pybridge.PyFuncs;
import com.byyd.pybridge.PyFuncsCaller;

import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_BYYD = "BYYD";

    private EditText editTextUrl;
    private TextView textView;
    private Button btnTest;
    private Button btnFunc001;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        Log.i(TAG_BYYD, "Init UI");

        // ============================================================
        // Start ZMQ service
        new Thread(() -> {
            try (ZContext context = new ZContext()) {
                // Socket to talk to clients
                ZMQ.Socket socket = context.createSocket(SocketType.REP);
                socket.bind("tcp://*:6666");

                while (!Thread.currentThread().isInterrupted()) {
                    // Block until a message is received
                    byte[] data = socket.recv(0);
                    String str = new String(data, ZMQ.CHARSET);

                    // Print the message
                    Log.i(TAG_BYYD, "Received: " + str);
                    showResult(Func_Msg, str);

                    // Send a response
                    socket.send("true".getBytes(ZMQ.CHARSET), 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // PyFuncs Start
        PyFuncsCaller.instance().start(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // PyFuncs Stop
        PyFuncsCaller.instance().stop();
    }

    private void initUI() {
        textView = findViewById(R.id.textView);
        editTextUrl = findViewById(R.id.editTextUrl);

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                showResult(Func_Msg, "Python测试，请等待测试结果...");

                new Thread(() -> {
                    try {
                        JSONObject params = new JSONObject();
                        params.put("link", editTextUrl.getText().toString());
                        params.put("p1", "p1 for test");
                        params.put("p2", "p2 for test");
                        JSONObject result = PyFuncsCaller.instance().call(PyFuncs.TEST, params, false);
                        String answer = result.getString("result");

                        showResult(Func_Msg, answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        btnFunc001 = findViewById(R.id.btnFunc001);
        btnFunc001.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                String msg = "开始滴水监控！滴水事件在python中触发，消息将通过zmq返回";

                // Sub Thread
                new Thread(() -> {
                    showResult(Func_Msg, msg);

                    try {
                        JSONObject params = new JSONObject();
                        params.put("link", editTextUrl.getText().toString());
                        JSONObject result = PyFuncsCaller.instance().call(PyFuncs.FUNC_001, params, true);
                        String answer = result.getString("result");

                        showResult(Func_Msg, answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    // Show Result
    private final byte Func_Msg = 0x01;

    private void showResult(byte func, String result) {
        Message msg = handler.obtainMessage();
        msg.what = func;
        msg.obj = result;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Func_Msg:
                    textView.append("\n");
                    textView.append(msg.obj.toString());
                    break;
                default:
                    break;
            }
            return false;
        }
    });
}
