package com.byyd.pybridge;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Administrator on 2020-08-30.
 */
public class PyFuncsCaller {
    private static final String TAG_BYYD = "BYYD Funcs";
    private String pythonPath;

    private volatile static PyFuncsCaller instance;

    private PyFuncsCaller() {
    }

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

    public void init(Context context) {
        initAssets(context);
        loadLibs(context);
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
        Log.i(TAG_BYYD, "PyBridge Init Start: " + pythonPath);

        // Extract python files from assets
        PyAssetsExtractor pyAssetExtractor = new PyAssetsExtractor(context);
        pyAssetExtractor.removeAssets("python");
        pyAssetExtractor.copyAssets("python");

        // Get the extracted assets directory
        Log.i(TAG_BYYD, "Python Path in assets");
        pythonPath = pyAssetExtractor.getAssetsDataDir() + "python";

        Log.i(TAG_BYYD, "PyBridge Inited: " + pythonPath);
    }

    public void start() {
        // Start the Python interpreter
        Log.i(TAG_BYYD, "PyBridge Start: " + pythonPath);
        PyBridge.start(pythonPath);
    }

    public void stop() {
        // Stop the interpreter
        Log.i(TAG_BYYD, "PyBridge Stop: " + pythonPath);
        PyBridge.stop();
    }

    public JSONObject call(String function, JSONObject json) {
        JSONObject result = null;

        try {
            start();

            Log.i(TAG_BYYD, "Call: " + function);
            json.put("function", function);
            String resultStr = PyBridge.call(json);
            Log.i(TAG_BYYD, "Result: " + resultStr);

            result = new JSONObject(resultStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
