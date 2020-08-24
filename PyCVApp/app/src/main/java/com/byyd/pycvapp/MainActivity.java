package com.byyd.pycvapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byyd.pybridge.AssetExtractor;
import com.byyd.pybridge.PyBridge;
import com.byyd.pybridge.PythonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private TextView textViewUrl;
    private TextView textView;
    private Button btnTest;
    private Button btnFunc001;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        Log.i("BYYD", "Init UI");

        // ============================================================
        // Extract python files from assets
        AssetExtractor assetExtractor = new AssetExtractor(this);
        assetExtractor.removeAssets("python");
        assetExtractor.copyAssets("python");

        // Get the extracted assets directory
        Log.i("BYYD", "Python Path in assets");
        String pythonPath = assetExtractor.getAssetsDataDir() + "python";

        // Load Libs
        String app_root = getFilesDir().getAbsolutePath() + "/app";
        Log.i("BYYD", "PyBridge app_root: " + app_root);
        File app_root_file = new File(app_root);
        PythonUtil.loadLibraries(app_root_file,
                new File(getApplicationInfo().nativeLibraryDir));

        // Start the Python interpreter
        Log.i("BYYD", "PyBridge Start: " + pythonPath);
        PyBridge.start(pythonPath);

        // Stop the interpreter
        //PyBridge.stop();
        // ============================================================
    }

    private void initUI() {
        textView = findViewById(R.id.textView);
        textViewUrl = findViewById(R.id.textViewUrl);

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                textView.setText("Python 图形计算库 测试，请等待测试结果...");

                new Thread(() -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("function", "test");
                        json.put("link", textViewUrl.getText().toString());
                        json.put("p1", "p1 for test");
                        json.put("p2", "p2 for test");
                        JSONObject result = PyBridge.call(json);
                        String answer = result.getString("result");

                        showResult(Func_Test, answer);
                    } catch (JSONException e) {
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
                textView.setText("开始滴水监控！滴水事件在python中触发，请检查后台日志；该事件可以通过zmq返回，Zmq已测试可用，请自行处理...任务完成！");

                new Thread(() -> {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("function", "start_func_001");
                        json.put("link", textViewUrl.getText().toString());
                        JSONObject result = PyBridge.call(json);
                        String answer = result.getString("result");

                        showResult(PING_001, answer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }


    private final byte Func_Test = 0x01;
    private final byte PING_001 = 0x02;

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
                case Func_Test:
                    textView.setText(msg.obj.toString());
                    break;
                case PING_001:
                    textView.setText(msg.obj.toString());
                    break;
            }
            return false;
        }
    });
}
