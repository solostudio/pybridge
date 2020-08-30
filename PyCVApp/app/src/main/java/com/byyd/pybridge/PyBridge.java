package com.byyd.pybridge;

import org.json.JSONObject;


class PyBridge {

    /**
     * Initializes the Python interpreter.
     *
     * @param datapath the location of the extracted python files
     * @return error code
     */
    public static native int start(String datapath);

    /**
     * Stops the Python interpreter.
     *
     * @return error code
     */
    public static native int stop();

    /**
     * Sends a string payload to the Python interpreter.
     *
     * @param payload the payload string
     * @return a string with the result
     */
    public static native String call(String payload);

    /**
     * Sends a JSON payload to the Python interpreter.
     *
     * @param payload JSON payload
     * @return JSON response
     */
    public static String call(JSONObject payload) {
        return call(payload.toString());
    }
}
