package com.ta.stb_03;

import android.app.Activity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;

    private RequestQueue mRequestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send"; // Use HTTPS
    private final String fcmServerKey = "AAAAzNPruCw:APA91bF3fW1yoUMnToCgqm9RgugLinJhCIFi3JN_rY-em6MDrGdtTs22woBP-ai92YOyMWoP6WPRHmhQ9_Ch2I4Wooczdp0dwmoHXjPmGOW2UeoHJHEhyggvNxsKg1tC8t4FJ8aU4M9F"; // Replace with your FCM server key

    public FcmNotificationsSender(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
        mRequestQueue = Volley.newRequestQueue(mContext);
    }

    public void sendNotification() {
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("ic_launcher_foreground", "ic_launcher_foreground"); // Replace with your icon name

            mainObj.put("notification", notiObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Handle success response
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error response
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=" + fcmServerKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            mRequestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
