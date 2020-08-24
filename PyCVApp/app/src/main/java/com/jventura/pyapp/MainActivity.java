package com.jventura.pyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jventura.pybridge.AssetExtractor;
import com.jventura.pybridge.PyBridge;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private Button btnAdd;
    private Button btnVersion;
    private Button btnNumpy;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        Log.i("BYYD", "Init UI");

        // Extract python files from assets
        AssetExtractor assetExtractor = new AssetExtractor(this);
        assetExtractor.removeAssets("python");
        assetExtractor.copyAssets("python");

        // Get the extracted assets directory
        Log.i("BYYD", "Path");
        String pythonPath = assetExtractor.getAssetsDataDir() + "python";

        // Start the Python interpreter
        Log.i("BYYD", "PyBridge Start: " + pythonPath);
        PyBridge.start(pythonPath);

        // Stop the interpreter
        //PyBridge.stop();
    }

    private void initUI() {
        textView = findViewById(R.id.textView);

        btnVersion = findViewById(R.id.btnVersion);
        btnVersion.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("function", "greet");
                    json.put("name", "Python 3.x");
                    JSONObject result = PyBridge.call(json);
                    String answer = result.getString("result");
                    textView.setText(answer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("function", "add");
                    json.put("a", 12);
                    json.put("b", 28);
                    JSONObject result = PyBridge.call(json);
                    String answer = result.getString("result");
                    textView.setText("12 + 28 = " + answer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        btnNumpy = findViewById(R.id.btnNumpy);
        btnNumpy.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("function", "test_import");
                    JSONObject result = PyBridge.call(json);
                    String answer = result.getString("result");
                    textView.setText(answer);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
