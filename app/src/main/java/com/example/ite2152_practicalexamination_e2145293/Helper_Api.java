package com.example.ite2152_practicalexamination_e2145293;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Helper_Api {
    private static Helper_Api instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    private Helper_Api(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized Helper_Api getInstance(Context context) {
        if (instance == null) {
            instance = new Helper_Api(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}

