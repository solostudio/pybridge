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
        // PyFuncs Caller Init
        PyFuncsCaller.instance().init(this);

    }

    private void initUI() {
        textView = findViewById(R.id.textView);
        editTextUrl = findViewById(R.id.editTextUrl);
        editTextUrl.setText("rtmp://47.100.8.76:5656/live/demo");

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                textView.setText("Python 图形计算库 测试，请等待测试结果...");

                new Thread(() -> {
                    try {
                        JSONObject params = new JSONObject();
                        params.put("link", editTextUrl.getText().toString());
                        params.put("p1", "p1 for test");
                        params.put("p2", "p2 for test");
                        JSONObject result = PyFuncsCaller.instance().call(PyFuncs.TEST, params);
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
                String msg = "开始滴水监控！滴水事件在python中触发，请检查后台日志；该事件可以通过zmq返回，Zmq已测试可用，请自行处理...任务完成！";

                // Sub Thread
                new Thread(() -> {
                    showResult(Func_Msg, msg);

                    try {
                        JSONObject params = new JSONObject();
                        params.put("link", editTextUrl.getText().toString());
                        JSONObject result = PyFuncsCaller.instance().call(PyFuncs.FUNC_001, params);
                        String answer = result.getString("result");

                        showResult(Func_Msg, answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                // UI Thread
                runOnUiThread(() -> {

                });
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
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Func_Msg:
                    textView.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
            return false;
        }
    });
}
