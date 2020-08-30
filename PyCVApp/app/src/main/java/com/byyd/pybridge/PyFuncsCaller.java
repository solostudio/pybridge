package com.byyd.pybridge;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Administrator on 2020-08-30.
 */
public class PyFuncsCaller {
    private static final String TAG_BYYD = "BYYD Funcs";

    private String pythonPath;
    private Context context;

    private final Thread video_thread;
    private ConcurrentLinkedQueue<JSONObject> video_call_queue = new ConcurrentLinkedQueue();
    private boolean isStopped = false;

    private volatile static PyFuncsCaller instance;
    public static PyFuncsCaller instance() {
        if (instance == null) {
            synchronized (PyFuncsCaller.class) {
                if (instance == null) {
                    instance = new PyFuncsCaller();
                }
            }
        }
        return instance;
    }
    private PyFuncsCaller() {
        video_thread = new Thread(() -> {
            // Python Init
            // python的线程机制不一样，必须在同一个线程处理视屏处理，否则运行会有不可预知的情况
            pyInitialize(context);

            // 处理视屏监控处理请求
            while (!isStopped){
                try {
                    JSONObject json = video_call_queue.poll();
                    if(json != null){
                        String resultStr = PyBridge.call(json);
                        Log.i(TAG_BYYD, "Async Call Result: " + resultStr);
                    }
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public JSONObject call(String function, JSONObject json, boolean isSilentMode) {
        JSONObject result = null;

        try {
            Log.i(TAG_BYYD, "Call: " + function);
            json.put("function", function);

            String resultStr = null;
            if(isSilentMode){
                this.video_call_queue.offer(json);
                resultStr = "{\"status\": \"ok\", \"result\": {\"msg\": \"算法在后台运行模式，消息将通过ZMQ返回\"}}";
                Log.i(TAG_BYYD, "Run in silent mode");
            }else{
                resultStr = PyBridge.call(json);
                Log.i(TAG_BYYD, "Result: " + resultStr);
            }
            result = new JSONObject(resultStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void start(Context context) {
        this.context = context;

        // video thread start
        video_thread.start();
    }

    public void stop() {
        // 停止线程
        isStopped = true;

        // 释放所有python对象
        pyFinalize();
    }

    // python的线程机制不一样，pyInitialize必须在同一个线程处理视屏处理，否则运行会有不可预知的情况
    public void pyInitialize(Context context) {
        initAssets(context);
        loadLibs(context);
        Log.i(TAG_BYYD, "PyBridge Inited");

        // Start the Python interpreter
        Log.i(TAG_BYYD, "PyBridge Start: " + pythonPath);
        PyBridge.start(pythonPath);
    }

    public void pyFinalize() {
        // Stop the interpreter
        Log.i(TAG_BYYD, "PyBridge Stop: " + pythonPath);
        PyBridge.stop();
    }

    private void loadLibs(Context context) {
        Log.i(TAG_BYYD, "PyBridge Load Libs");

        // Load Libs
        String app_root = context.getFilesDir().getAbsolutePath() + "/app";
        Log.i(TAG_BYYD, "PyBridge app_root: " + app_root);
        File app_root_file = new File(app_root);
        PyLibsLoader.loadLibraries(app_root_file,
                new File(context.getApplicationInfo().nativeLibraryDir));
    }

    public void initAssets(Context context) {
        Log.i(TAG_BYYD, "PyBridge Init Start");

        // Extract python files from assets
        PyAssetsExtractor pyAssetExtractor = new PyAssetsExtractor(context);
        pyAssetExtractor.removeAssets("python");
        pyAssetExtractor.copyAssets("python");

        // Get the extracted assets directory
        pythonPath = pyAssetExtractor.getAssetsDataDir() + "python";
        Log.i(TAG_BYYD, "Python assets extracted to " + pythonPath);

        Log.i(TAG_BYYD, "PyBridge Inited: " + pythonPath);
    }

}
